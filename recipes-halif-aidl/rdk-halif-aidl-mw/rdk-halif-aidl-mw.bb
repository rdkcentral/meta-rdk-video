SUMMARY = "RDK HAL AIDL interface libraries"
HOMEPAGE = "https://github.com/rdkcentral/rdk-halif-aidl"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

#SRC_URI = "git://github.com/rdkcentral/rdk-halif-aidl.git;protocol=https;branch=main"
SRC_URI = "${CMF_GITHUB_ROOT}/rdk-halif-aidl;${CMF_GITHUB_SRC_URI_SUFFIX}" 
SRCREV = "4a2ef3990089951fefe0739e171f1d0ce1549bc1"
S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

# The Binder SDK (libbinder/libutils + headers) is provided by the linux-binder
# recipe and staged into the recipe sysroot.
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DEPENDS = "libbinderrdk"
PROVIDES += "rdk-halif-aidl-mw"

# Default component set (every released component). A build configuration may
# subset HALIF_COMPONENTS and set HALIF_VERSIONS_FILE in its own include.
require ${THISDIR}/halif-components.inc

# The mount point is a real partition, not a label: the rootfs mounts a partition
# per layer, so the vendor layer and the middleware layer are different mounts and
# their libraries land in different places. HALIF_MOUNT_POINT IS that partition
# mount, and setting it picks the destination:
#     /vendor -> /vendor/rdk-halif-aidl/lib<comp>-v<ver>-cpp.so
#     /mw     -> /mw/rdk-halif-aidl/lib<comp>-v<ver>-cpp.so
# A platform whose mounts differ sets HALIF_MOUNT_POINT (or overrides HALIF_LIBDIR
# outright). rdk-halif-aidl is the module dir the interface libraries live in under
# the mount.
HALIF_MOUNT_POINT ??= "mw"
HALIF_LIBDIR ??= "/usr/lib/${HALIF_MOUNT_POINT}/rdk-halif-aidl"

# Headers never reach the target - they are packaged into rdk-halif-aidl-<comp>-dev
# and consumed from the build sysroot only. They still live UNDER the mount point
# (in an include/ subdir), NOT at the shared ${includedir}: vendor and mw carry
# DIFFERENT versions of the same component, so a shared staging path would make
# them collide. Rooting each mount's staging at itself keeps them apart.
HALIF_INCDIR ??= "/usr/include/${HALIF_MOUNT_POINT}"

# Stage the whole mount into a consumer's recipe-sysroot (the OE defaults only
# stage ${includedir}/${libdir}, which no longer hold our files). Because the path
# carries the mount, vendor stages to .../vendor/rdk-halif-aidl and mw to
# .../mw/rdk-halif-aidl - distinct subtrees, so their differing library versions
# never share a staging location. A consumer links -L${STAGING_DIR_HOST}${HALIF_LIBDIR}.
SYSROOT_DIRS += "/"

# Versions manifest (components: {comp: ver}), consumed directly. Defaults to the
# source's own versions_released.yaml - the released cohort - so a plain build
# produces the released versions. A configuration may point this at another
# manifest; components it does not pin build their latest snapshot. Set it empty
# to build every component at latest.
HALIF_VERSIONS_FILE ??= "${S}/versions_released.yaml"
HALIF_PKG_PREFIX ??= "${PN}"

inherit cmake

# One CMake project per component, so skip the single-project configure and drive
# cmake per component in do_compile (reusing the cmake class' cross toolchain).
do_configure[noexec] = "1"

do_compile() {
    cmake_do_generate_toolchain_file

    # An explicitly empty HALIF_COMPONENTS is a misconfiguration: halif_plan.py
    # with no arguments would plan EVERY component, but the intent here is to build
    # a chosen set - so fail fast rather than silently build everything.
    if [ -z "${HALIF_COMPONENTS}" ]; then
        bbfatal "HALIF_COMPONENTS is empty - set the components to build (default: all, from halif-components.inc)"
    fi

    # Resolve the topological build order: the named components PLUS their
    # dependency closure (a subset build pulls in what it links - hdmicec pulls
    # common - so common is never named), each dependency at its exact linked
    # version. Different versions of one component coexist. Packaging (below) reads
    # this same plan. The planner ships with this layer and is fetched with the source.
    versions=""
    [ -n "${HALIF_VERSIONS_FILE}" ] && versions="--versions ${HALIF_VERSIONS_FILE}"

    bbnote "HALIF_VERSIONS_FILE=${HALIF_VERSIONS_FILE}"
    bbnote "HALIF_COMPONENTS=${HALIF_COMPONENTS}"
    bbnote "versions=${versions}"

    "${S}/tests/yocto/meta-rdk-halif-aidl/halif_plan.py" \
    ${versions} \
    ${HALIF_COMPONENTS} \
    > "${B}/plan.txt" \
    || bbfatal "halif_plan.py failed to resolve a build order"

    # Sibling libs/headers built earlier in the plan are staged here so later
    # components link them; the Binder SDK comes from the recipe sysroot. Start
    # clean so a changed HALIF_VERSIONS_FILE can't leave stale per-version libs.
    rm -rf "${B}/staged"
    install -d "${B}/staged/lib/rdk-halif-aidl" "${B}/staged/include/rdk-halif-aidl"

    while read comp ver; do
        bbnote "rdk-halif-aidl: building ${comp}@${ver}"
        # Build dir keyed by component AND version, so an incremental rebuild
        # after HALIF_VERSIONS_FILE changes cannot hit a stale CMake cache.
        cmake -S "${S}/${comp}/${ver}" -B "${B}/obj/${comp}/${ver}" \
            -G "${OECMAKE_GENERATOR}" \
            -DCMAKE_TOOLCHAIN_FILE="${WORKDIR}/toolchain.cmake" \
            -DBINDER_SDK_DIR="${STAGING_DIR_HOST}${prefix}/mw" \
            -DBINDER_SDK_INCLUDE_DIR="${STAGING_DIR_HOST}${includedir}/mw" \
            -DHALIF_LIB_DIR="${B}/staged/lib/rdk-halif-aidl" \
            -DHALIF_INCLUDE_DIR="${B}/staged/include/rdk-halif-aidl"
        cmake --build "${B}/obj/${comp}/${ver}" -- ${PARALLEL_MAKE}
        install -m 0755 \
            "${B}/obj/${comp}/${ver}/lib${comp}-v${ver}-cpp.so" \
            "${B}/staged/lib/rdk-halif-aidl/"
        ln -sf "lib${comp}-v${ver}-cpp.so" \
            "${B}/staged/lib/rdk-halif-aidl/lib${comp}-cpp.so"
        install -d "${B}/staged/include/rdk-halif-aidl/${comp}/${ver}"
        cp -R "${S}/${comp}/${ver}/include" "${B}/staged/include/rdk-halif-aidl/${comp}/${ver}/"
    done < "${B}/plan.txt"
}


do_install() {
    install -d "${D}${HALIF_LIBDIR}" "${D}${HALIF_INCDIR}"
    cp -a "${B}/staged/lib/rdk-halif-aidl/." "${D}${HALIF_LIBDIR}/"
    cp -a "${B}/staged/include/rdk-halif-aidl/." "${D}${HALIF_INCDIR}/"
    while read comp ver; do
        ln -sf "lib${comp}-v${ver}-cpp.so" \
            "${D}${HALIF_LIBDIR}/lib${comp}-cpp.so"
    done < "${B}/plan.txt"
    # do_compile runs without pseudo, so the staged tree is owned by the build
    # user; cp -a preserves that uid. Rootfs files must be root-owned - reset it
    # here (under pseudo) so do_package does not choke on an unknown uid and the
    # device gets root:root libraries.
    chown -R root:root "${D}${HALIF_LIBDIR}" "${D}${HALIF_INCDIR}"
}

# Package layout: one package per component - rdk-halif-aidl-<comp> (its versioned
# .so libraries) and rdk-halif-aidl-<comp>-dev (its headers) - plus a single
# rdk-halif-aidl-dbg for all debug symbols. The -aidl in the name keeps these
# distinct from the legacy C HAL's rdk-halif-* packages on the same rootfs.
#
# The component set is the build's dependency CLOSURE, which is only known after
# the source is fetched (a subset build pulls in the deps it links). So the split
# is driven at do_package time from the plan do_compile wrote - NOT from
# HALIF_COMPONENTS at parse time, when neither the source nor the closure exists,
# and an auto-resolved dependency would be installed but belong to no package.
# PACKAGES_DYNAMIC lets images/other recipes depend on these before parse knows them.
#
# ONE debug package: overriding PACKAGES drops bitbake's own ${PN}-dbg, whose
# .debug files would then belong to no package and fail "installed but not shipped".
# OE assigns EVERY .debug file to the FIRST -dbg package in PACKAGES order, so a
# single ${PN}-dbg, listed first, is the split.
PACKAGES = "${PN}-dbg"
PACKAGES_DYNAMIC = "^rdk-halif-aidl-mw-.*"
SUMMARY:${PN}-dbg = "RDK HAL AIDL debug symbols"
FILES:${PN}-dbg = "${HALIF_LIBDIR}/.debug"

python populate_packages:prepend () {
    import os
    libdir = d.getVar('HALIF_LIBDIR')
    incdir = d.getVar('HALIF_INCDIR')

    # The component set = the plan do_compile resolved (the dependency closure).
    # A component may appear at several versions; ONE package per component holds
    # all its versioned .so, because the version is in the .so name - so multiple
    # versions coexist in one directory and one package.
    plan = os.path.join(d.getVar('B'), 'plan.txt')
    comps = []
    with open(plan) as fh:
        for line in fh:
            parts = line.split()
            if parts and parts[0] not in comps:
                comps.append(parts[0])

    pkgs = d.getVar('PACKAGES').split()            # ['<PN>-dbg']
    for c in comps:
        main = d.getVar('HALIF_PKG_PREFIX') + '-' + c
        dev = main + '-dev'
        # -dev before the library package: bitbake assigns each file to the FIRST
        # package whose FILES matches it.
        pkgs += [dev, main]
        d.setVar('SUMMARY:' + main, 'RDK HAL AIDL interface library: %s' % c)
        d.setVar('SUMMARY:' + dev, 'RDK HAL AIDL interface headers: %s' % c)
        # The unversioned symlink is a link-time artefact only: the runtime
        # DT_NEEDED carries the versioned SONAME, so it belongs in -dev.
        d.setVar('FILES:' + dev, '%s/%s %s/lib%s-cpp.so' % (incdir, c, libdir, c))
        d.setVar('FILES:' + main, '%s/lib%s-v*-cpp.so' % (libdir, c))
        d.setVar('INSANE_SKIP:' + main, 'dev-so')
    d.setVar('PACKAGES', ' '.join(pkgs))
}
