SUMMARY = "Evergreen Cobalt loader_app library."
HOMEPAGE = "https://cobalt.dev"

LICENSE = "BSD-3-Clause & Apache-2.0-with-LLVM-exception"
LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=0fca02217a5d49a14dfe2d11837bb34d \
    file://../larboard/LICENSE;md5=a1045f140d2e71b4e089875cd5d07e42 \
"

require larboard_revision.inc
require rdke-cobalt-buildfix.inc

PATCHTOOL = "git"
TOOLCHAIN = "gcc"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI  = "git://github.com/youtube/cobalt.git;protocol=https;name=cobalt;branch=24.lts.stable"
SRC_URI += "${LARBOARD_SRC_URI};protocol=${CMF_GITHUB_PROTOCOL};destsuffix=larboard;name=larboard;branch=24.lts.stable"
SRC_URI += "file://24/0001-Include-RDK-platforms.patch"
SRC_URI += "file://24/0002-Fix-crashpad-build.patch"
SRC_URI += "file://24/0003-breakpad-add-mapping-info.patch"
SRC_URI += "file://24/0004-Build-fix-for-ARM64.patch"
SRC_URI += "file://24/0005-Use-Yocto-host-toolchain.patch"
SRC_URI += "file://24/0001-Disalbe-wayland-platform-ext-until-ess-update.patch;patchdir=../larboard"
SRC_URI += "file://24/0001-Prevent-cobalt-unloading.patch"
SRC_URI += "file://24/player_internal_v6.patch;patchdir=../larboard"

CR = "40"
PR = "r${CR}"
SRCREV_cobalt = "24.lts.${CR}"
SRCREV_larboard = "${LARBOARD_SRCREV_24}"
SRCREV_FORMAT = "cobalt_larboard"
PV .= "+git${SRCPV}"

do_fetch[vardeps] += " SRCREV_FORMAT SRCREV_cobalt SRCREV_larboard"
S = "${WORKDIR}/git"

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
    elif "rpi" in d.getVar('MACHINEOVERRIDES', True):
        return 'rdk-rpi'
    elif target_arch == 'arm':
        return 'rdk-arm'
    else:
        bb.fatal("Unsupported target architecture: {}".format(target_arch))

COBALT_PLATFORM ?= "${@get_cobalt_platform(d)}"
COBALT_BUILD_TYPE ?= "${@bb.utils.contains('DISTRO_FEATURES', 'cobalt-qa', 'qa', 'gold', d)}"

PACKAGECONFIG ?= "${COBALT_BUILD_TYPE}"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'opencdm', 'opencdm', '', d)}"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_security_disable', '', 'securityagent', d)}"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'enable_asan', 'asan', '', d)}"

PACKAGECONFIG[opencdm]       = "rdk_enable_ocdm=true,rdk_enable_ocdm=false,,"
PACKAGECONFIG[securityagent] = "rdk_enable_securityagent=true,rdk_enable_securityagent=false,,"
PACKAGECONFIG[qa]            = ",,nodejs-native,"
PACKAGECONFIG[asan]          = "use_asan=true,,gcc-sanitizers"
PACKAGECONFIG[gold]          = ""
PACKAGECONFIG[nplb]          = ""

GN_ARGS_EXTRA ?= ""
GN_ARGS_EXTRA:append = " sb_is_evergreen_compatible=true"
GN_ARGS_EXTRA:append:arm = " rdk_arm_call_convention=\"${@bb.utils.contains('TUNE_FEATURES', 'callconvention-hard', 'hardfp', 'softfp', d)}\""
GN_ARGS_EXTRA:append = " ${PACKAGECONFIG_CONFARGS}"

inherit python3native pkgconfig breakpad-wrapper

BREAKPAD_BIN = "lib*.so* nplb* crashpad_handler"

export PYTHONPATH="${S}"

do_unpack_extra() {
    bbnote "copy larboard"
    cp -ar "${WORKDIR}/larboard/src/third_party/starboard" "${S}/third_party/"
}
addtask unpack_extra after do_patch before do_configure

do_configure() {
    ${PYTHON} cobalt/build/gn.py -c ${COBALT_BUILD_TYPE} -p ${COBALT_PLATFORM} --overwrite_args
    echo "${GN_ARGS_EXTRA}" | tr ' ' '\n' >> out/${COBALT_PLATFORM}_${COBALT_BUILD_TYPE}/args.gn
}

do_compile[progress] = "percent"
do_compile() {
    export NINJA_STATUS='%p '
    ninja -C out/${COBALT_PLATFORM}_${COBALT_BUILD_TYPE} loader_app native_target/crashpad_handler
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 out/${COBALT_PLATFORM}_${COBALT_BUILD_TYPE}/native_target/crashpad_handler ${D}${bindir}

    install -d ${D}${libdir}
    install -m 0755 out/${COBALT_PLATFORM}_${COBALT_BUILD_TYPE}/libloader_app.so ${D}${libdir}

    install -d ${D}${datadir}/content
    cp -arv --no-preserve=ownership out/${COBALT_PLATFORM}_${COBALT_BUILD_TYPE}/content ${D}${datadir}/
}

# NPLB
do_configure:append() {
    if ${@bb.utils.contains('PACKAGECONFIG', 'nplb', 'true', 'false', d)}; then
        ${PYTHON} cobalt/build/gn.py -c devel  -p ${COBALT_PLATFORM} --overwrite_args
        echo "${GN_ARGS_EXTRA}" | tr ' ' '\n' >> out/${COBALT_PLATFORM}_devel/args.gn
    fi
}
do_compile:append() {
    if ${@bb.utils.contains('PACKAGECONFIG', 'nplb', 'true', 'false', d)}; then
        export PYTHONHTTPSVERIFY=0
        ninja -C out/${COBALT_PLATFORM}_devel nplb nplb_evergreen_compat_tests
    fi
}
do_install:append() {
    if ${@bb.utils.contains('PACKAGECONFIG', 'nplb', 'true', 'false', d)}; then
        install -m 0755 out/${COBALT_PLATFORM}_devel/nplb ${D}${bindir}
        install -m 0755 out/${COBALT_PLATFORM}_devel/nplb_evergreen_compat_tests ${D}${bindir}
        install -d ${D}${datadir}/content/data/test
        cp -arv --no-preserve=ownership out/${COBALT_PLATFORM}_devel/content/data/test ${D}${datadir}/content/data/
    fi
}

FILES:${PN}  = "${bindir}/*"
FILES:${PN} += "${libdir}/libloader_app.so"
FILES:${PN} += "${datadir}/content/*"

FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"

PACKAGES =+ "${PN}-nplb"
PROVIDES += "${PN}-nplb"
FILES:${PN}-nplb  = "${bindir}/nplb*"

PACKAGES =+ "${PN}-nplb-test-data"
PROVIDES += "${PN}-nplb-test-data"
FILES:${PN}-nplb-test-data = "${datadir}/content/data/test/*"
