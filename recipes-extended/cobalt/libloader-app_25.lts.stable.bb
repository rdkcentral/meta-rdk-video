SUMMARY = "Evergreen Cobalt loader_app library."
HOMEPAGE = "https://cobalt.dev"

LICENSE = "BSD-3-Clause & Apache-2.0-with-LLVM-exception"
LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=0fca02217a5d49a14dfe2d11837bb34d \
    file://../larboard/LICENSE;md5=a1045f140d2e71b4e089875cd5d07e42 \
"

inherit features_check
CONFLICT_DISTRO_FEATURES = "cobalt-24"

require larboard_revision.inc
require rdke-cobalt-buildfix.inc

PATCHTOOL = "git"
TOOLCHAIN = "gcc"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI  = "git://github.com/youtube/cobalt.git;protocol=https;name=cobalt;branch=25.lts.stable"
SRC_URI += "${LARBOARD_SRC_URI};protocol=${CMF_GITHUB_PROTOCOL};destsuffix=larboard;name=larboard;branch=feature/RDKEMW-9147_fix"
SRC_URI += "file://25/0001-Include-RDK-platforms.patch"
SRC_URI += "file://25/0002-Fix-crashpad-build.patch"
SRC_URI += "file://25/0003-breakpad-add-mapping-info.patch"
SRC_URI += "file://25/0004-Build-fix-for-ARM64.patch"
SRC_URI += "file://25/0005-Use-Yocto-host-toolchain.patch"
SRC_URI += "file://25/0006-Use-certifi-to-tell-urllib-where-to-find-CA-file-397.patch"

CR = "30"
PR = "r${CR}"
SRCREV_cobalt = "25.lts.${CR}"
SRCREV_larboard = "${LARBOARD_SRCREV_DEV}"
SRCREV_FORMAT = "cobalt_larboard"
PV .= "+git${SRCPV}"

do_fetch[vardeps] += " SRCREV_FORMAT SRCREV_cobalt SRCREV_larboard"
S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

DEPENDS += "virtual/libgles2 virtual/egl essos gstreamer1.0 gstreamer1.0-plugins-base"
DEPENDS += " wpeframework entservices-apis wpeframework-clientlibraries"
DEPENDS += " ninja-native bison-native openssl-native gn-native ccache-native"
DEPENDS += " python3-six-native python3-urllib3-native"

RDEPENDS:${PN} += "gstreamer1.0-plugins-base-app gstreamer1.0-plugins-base-playback"

TUNE_CCARGS:remove = "-fno-omit-frame-pointer -fno-optimize-sibling-calls"

def get_cobalt_platform(d):
    target_arch = d.getVar('TARGET_ARCH', True)
    if target_arch == 'aarch64':
        return 'rdk-arm64'
    elif target_arch == 'arm':
        return 'rdk-arm'
    else:
        bb.fatal("Unsupported target architecture: {}".format(target_arch))

COBALT_PLATFORM ?= "${@get_cobalt_platform(d)}"
COBALT_BUILD_TYPE ?= "${@bb.utils.contains('DISTRO_FEATURES', 'cobalt-qa', 'qa', 'gold', d)}"
COBALT_OUT_DIR = "${B}/${COBALT_PLATFORM}_${COBALT_BUILD_TYPE}"
COBALT_OUT_DEV_DIR = "${B}/${COBALT_PLATFORM}_devel"

PACKAGECONFIG ?= "${COBALT_BUILD_TYPE}"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'opencdm', 'opencdm', '', d)}"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_security_disable', '', 'securityagent', d)}"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'enable_asan', 'asan', '', d)}"

PACKAGECONFIG[opencdm]       = "rdk_enable_ocdm=true,rdk_enable_ocdm=false,,"
PACKAGECONFIG[securityagent] = "rdk_enable_securityagent=true,rdk_enable_securityagent=false,,"
PACKAGECONFIG[qa]            = ",,nodejs-native,"
PACKAGECONFIG[asan]          = "use_asan=true,,gcc-sanitizers"
PACKAGECONFIG[gold]          = ""

GN_ARGS_EXTRA ?= ""
GN_ARGS_EXTRA:append = " sb_enable_cpp20_audit=false"
GN_ARGS_EXTRA:append = " sb_is_evergreen_compatible=true"
GN_ARGS_EXTRA:append:arm = " rdk_arm_call_convention=\"${@bb.utils.contains('TUNE_FEATURES', 'callconvention-hard', 'hardfp', 'softfp', d)}\""
GN_ARGS_EXTRA:append = " ${PACKAGECONFIG_CONFARGS}"

inherit python3native pkgconfig breakpad-wrapper ccache

BREAKPAD_BIN = "lib*.so* loader_app_bin elf_loader_sandbox_bin crashpad_handler"

export PYTHONPATH="${S}"

python() {
    """
    Cobalt uses its own wrapper for ccache. Disable bitbake setup.
    """
    d.delVar("CCACHE")
}

do_unpack_extra() {
    bbnote "copy larboard"
    ( cd "${S}/third_party/" && ln -sf ../../larboard/src/third_party/starboard . )
}
addtask unpack_extra after do_patch before do_configure

do_configure[cleandirs] = "${B}"
do_configure() {
    cd ${S}
    platform_path=$(${PYTHON} ${S}/starboard/build/platforms.py ${COBALT_PLATFORM})

    mkdir -p ${COBALT_OUT_DIR}
    cp ${platform_path}/args.gn ${COBALT_OUT_DIR}/args.gn
    echo "${GN_ARGS_EXTRA}" | tr ' ' '\n' >> ${COBALT_OUT_DIR}/args.gn
    echo "build_type=\"${COBALT_BUILD_TYPE}\"" >> ${COBALT_OUT_DIR}/args.gn
    ${PYTHON} ${S}/cobalt/build/gn.py -p ${COBALT_PLATFORM} --out_directory ${COBALT_OUT_DIR}

    mkdir -p ${COBALT_OUT_DEV_DIR}
    cp ${platform_path}/args.gn ${COBALT_OUT_DEV_DIR}/args.gn
    echo "${GN_ARGS_EXTRA}" | tr ' ' '\n' >> ${COBALT_OUT_DEV_DIR}/args.gn
    echo "build_type=\"devel\"" >> ${COBALT_OUT_DEV_DIR}/args.gn
    ${PYTHON} ${S}/cobalt/build/gn.py -p ${COBALT_PLATFORM} --out_directory ${COBALT_OUT_DEV_DIR}
}

do_compile[progress] = "percent"
do_compile() {
    export NINJA_STATUS='%p '
    ninja -C ${COBALT_OUT_DIR} loader_app loader_app_bin native_target/crashpad_handler
    ninja -C ${COBALT_OUT_DEV_DIR} elf_loader_sandbox_bin nplb_evergreen_compat_tests
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${COBALT_OUT_DIR}/native_target/crashpad_handler ${D}${bindir}

    install -d ${D}${libdir}
    install -m 0755 ${COBALT_OUT_DIR}/libloader_app.so ${D}${libdir}
    install -m 0755 ${COBALT_OUT_DIR}/loader_app_bin ${D}${bindir}
    ( cd ${D}${bindir} && ln -sf loader_app_bin loader_app )

    install -d ${D}${datadir}/content
    cp -av --no-preserve=ownership ${COBALT_OUT_DIR}/content ${D}${datadir}/

    install -m 0755 ${COBALT_OUT_DEV_DIR}/elf_loader_sandbox_bin ${D}${bindir}
    install -m 0755 ${COBALT_OUT_DEV_DIR}/libelf_loader_sandbox.so ${D}${libdir}
    ( cd ${D}${bindir} && ln -sf elf_loader_sandbox_bin elf_loader_sandbox )

    install -m 0755 ${COBALT_OUT_DEV_DIR}/nplb_evergreen_compat_tests ${D}${bindir}
}

FILES:${PN}  = "${bindir}/crashpad_handler ${bindir}/loader_app ${bindir}/loader_app_bin"
FILES:${PN} += "${libdir}/libloader_app.so ${libdir}/libelf_loader_sandbox.so"
FILES:${PN} += "${datadir}/content/*"

PACKAGES =+ "${PN}-tools"
FILES:${PN}-tools  = "${bindir}/elf_loader_sandbox_bin ${bindir}/elf_loader_sandbox"
FILES:${PN}-tools += "${libdir}/libelf_loader_sandbox.so"
FILES:${PN}-tools += "${bindir}/nplb_evergreen_compat_tests"

FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dev += "dev-so dev-elf"
INSANE_SKIP:${PN}-dbg += "dev-so"
