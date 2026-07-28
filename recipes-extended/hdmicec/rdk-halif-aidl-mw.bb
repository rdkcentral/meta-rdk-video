SUMMARY = "HAL interface definitions for RDK-E (AIDL) + generated C++ headers + libhal_aidl.so"
DESCRIPTION = "Fetches rdk-halif-aidl AIDL definitions, generates C++ sources/headers using the native aidl compiler, and builds libhal_aidl.so using an integrated Makefile."
#//TODO this file will remove in once the proper rdk-halif-aidl header are povided by vendor.
HOMEPAGE = "https://github.com/rdkcentral/rdk-halif-aidl"
#
# Upstream repository is open-source (see HOMEPAGE/SRC_URI). The repo ships an
# Apache 2.0 license file, so use Apache-2.0 here rather than CLOSED.
#
LICENSE = "Apache-2.0"
#
# Track upstream license/notice texts from the fetched git working tree.
# NOTE: Checksums must match the exact upstream tag/commit being built (SRCREV/PV).
#
LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327 \
    file://NOTICE;md5=97b1cee2f4f03fc1fb4d38e8de47e1f3 \
"
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI = " \
    git://git@github.code.rdkcentral.com/rdkcentral/rdk-halif-aidl.git;nobranch=1;protocol=ssh \
    file://Makefile \
"

# Recipe version (also used to select the git tag below).
PV = "0.15.0"

# SRCREV must be a git revision; using the tag ref is a common pattern.
SRCREV = "30e87b4cb50f7c4f8851f53b4dfaa90d13152dcc"

S = "${WORKDIR}/git"

inherit cmake

PROVIDES += "rdk-halif-aidl"

# The generator runs the host 'aidl' compiler during CMake configure (via execute_process),
# so we must depend on a *native* provider of the aidl tool.
#
# Also, building libhal_aidl.so links against binder/libs from the target sysroot.
# Dependency names can vary by layer; these are consistent with existing workspace guidance.
#DEPENDS += "linux-binder-idl linux-binder-idl-native"
DEPENDS += " libbinder libbinder-native"

# Path to native aidl compiler. Override in your distro/layer if your provider uses a different path.
AIDL_BIN ?= "${STAGING_BINDIR_NATIVE}/aidl"

# AIDL interface version directory to use in rdk-halif-aidl (usually 'current').
AIDL_SRC_VERSION ?= "current"

# Modules to generate into a single consolidated output directory.
HAL_AIDL_MODULES ?= " \
    hdmicec \
    deepsleep \
    indicator \
    boot \
"

# Consolidated output for generated artifacts:
#   ${AIDL_GEN_DIR}/cpp/...  (generated .cpp)
#   ${AIDL_GEN_DIR}/h/...    (generated headers)
AIDL_GEN_DIR = "${B}/current"

# Shared-library versioning. The Makefile does not set SONAME by default, so we inject it via LDFLAGS.
AIDL_LIB_SONAME_MAJOR ?= "1"
AIDL_LIB_VERSION ?= "1.0.0"

do_configure() {
    # Ensure a clean output directory for generated files.
    rm -rf ${AIDL_GEN_DIR}
    install -d ${AIDL_GEN_DIR}

    # rdk-halif-aidl runs the 'aidl' compiler at CMake configure time (CompileAidl.cmake uses execute_process).
    # Therefore, simply configuring each module is sufficient to generate sources/headers.
    for m in ${HAL_AIDL_MODULES}; do
        cmake -S ${S} -B ${B}/cmake_${m} \
            -DAIDL_BIN=${AIDL_BIN} \
            -DAIDL_TARGET=$m \
            -DAIDL_SRC_VERSION=${AIDL_SRC_VERSION} \
            -DAIDL_GEN_DIR=${AIDL_GEN_DIR}
    done
}

do_compile() {
    # Build libhal_aidl.so from the generated C++ sources using the integrated Makefile.
    install -d ${B}/aidl_lib
    install -d ${B}/lib

    # Stage recipe-provided Makefile into a stable build directory.
    install -m 0644 ${WORKDIR}/Makefile ${B}/aidl_lib/Makefile

    oe_runmake -C ${B}/aidl_lib \
        CXX="${CXX}" \
        AIDL_GEN_DIR="${AIDL_GEN_DIR}" \
        AIDL_LIB_PATH="${B}/lib/libhal_aidl.so.${AIDL_LIB_VERSION}" \
        AIDL_LIB_INC_FLAGS="-I${AIDL_GEN_DIR}/h" \
        AIDL_LIB_CFLAGS="${CPPFLAGS} ${CXXFLAGS} -std=c++17 -fPIC -Wall -Wextra" \
        AIDL_LIB_LDFLAGS="${LDFLAGS} -L${RECIPE_SYSROOT}/usr/lib -Wl,-rpath-link=${RECIPE_SYSROOT}/usr/lib -Wl,-soname,libhal_aidl.so.${AIDL_LIB_SONAME_MAJOR}" \
        AIDL_LIB_LD_LIBS="-lbinder -lutils"
}

do_install() {
    # Install generated headers for consumers (e.g., TEVDevice expects /usr/include/hal/h/...).
    install -d ${D}${includedir}
    if [ -d ${AIDL_GEN_DIR}/h ]; then
        cp -apr ${AIDL_GEN_DIR}/h/* ${D}${includedir}
    fi

    # Optionally install the AIDL definition sources as well (useful for downstream tooling/docs).
    install -d ${D}${includedir}
    for d in ${HAL_AIDL_MODULES} common; do
        if [ -d ${S}/${d}/${AIDL_SRC_VERSION} ]; then
            tar --no-same-owner -cpf - -C ${S}/${d}/${AIDL_SRC_VERSION} . \
                | tar --no-same-owner -xpf - -C ${D}${includedir}
        fi
    done
    # Avoid shipping module CMakeLists.txt as “headers”.
    find ${D}${includedir}/hal/aidl -name CMakeLists.txt -delete || true

    # Install shared library + symlinks.
    install -d ${D}${libdir}
    install -m 0755 ${B}/lib/libhal_aidl.so.${AIDL_LIB_VERSION} ${D}${libdir}/
    ln -sf libhal_aidl.so.${AIDL_LIB_VERSION} ${D}${libdir}/libhal_aidl.so.${AIDL_LIB_SONAME_MAJOR}
    ln -sf libhal_aidl.so.${AIDL_LIB_SONAME_MAJOR} ${D}${libdir}/libhal_aidl.so
}

# Runtime package: versioned shared library
FILES:${PN} += " \
    ${libdir}/libhal_aidl.so.* \
"

# -dev package: headers + unversioned linker symlink + AIDL source definitions
FILES:${PN}-dev += " \
    ${includedir}/hal/h \
    ${includedir}/hal/aidl \
    ${libdir}/libhal_aidl.so \
"
