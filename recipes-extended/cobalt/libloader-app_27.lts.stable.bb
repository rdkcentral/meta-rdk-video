SUMMARY = "Evergreen Cobalt loader_app library."
HOMEPAGE = "https://cobalt.dev"

LICENSE = "BSD-3-Clause & Apache-2.0-with-LLVM-exception"
LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=c408a301e3407c3803499ce9290515d6 \
    file://../larboard/LICENSE;md5=a1045f140d2e71b4e089875cd5d07e42 \
"

inherit features_check
# REQUIRED_DISTRO_FEATURES = "cobalt-27"

require larboard_revision.inc
require rdke-cobalt-buildfix.inc

PATCHTOOL = "git"
TOOLCHAIN = "gcc"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
LARBOARD_SRCREV_DEV = "1bb03434e8d5450d05a68d673806b5ec55013e08"

SRC_URI  = "git://github.com/youtube/cobalt.git;destsuffix=src;protocol=https;name=cobalt;branch=27.lts"
SRC_URI += "git://chromium.googlesource.com/chromium/tools/depot_tools.git;destsuffix=depot_tools;protocol=https;name=depottools;branch=main"
SRC_URI += "${LARBOARD_SRC_URI};protocol=${CMF_GITHUB_PROTOCOL};destsuffix=larboard;name=larboard;branch=develop;nobranch=1"
SRC_URI += "file://27/0001-Add-RDK-platforms.patch"
SRC_URI += "file://27/0002-Add-an-option-to-pass-package-configuration-args-via.patch"
SRC_URI += "file://27/0003-Fix-location-of-src-folder.patch"
SRC_URI += "file://27/0004-Disable-rewriting-of-paths-returned-by-pkg-config.patch"
SRC_URI += "file://27/0005-Allow-starboard-implementation-set-rpath.patch"
SRC_URI += "file://27/0006-Pickup-strip-binary-path-from-Yocto-environment.patch"

CR = "3"
PR = "r${CR}"
# "27.lts.${CR}"
SRCREV_cobalt = "3befd332486952f6603ae3baf2a8713878d6e200"
SRCREV_depottools = "2cea9cdd785391335d448773124631cb711225e2"
SRCREV_larboard = "${LARBOARD_SRCREV_DEV}"
SRCREV_FORMAT = "cobalt_larboard"
PV .= "+git${SRCPV}"

do_fetch[vardeps] += " SRCREV_FORMAT SRCREV_cobalt SRCREV_larboard"
S = "${WORKDIR}/src"
B = "${WORKDIR}/build"

DEPENDS += "virtual/libgles2 virtual/egl essos gstreamer1.0 gstreamer1.0-plugins-base"
DEPENDS += " ninja-native bison-native openssl-native gn-native ccache-native"
DEPENDS += " python3-six-native python3-urllib3-native gperf-native"
DEPENDS += " ca-certificates-native xz-native curl-native git-replacement-native"

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
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'enable_asan', 'asan', '', d)}"
PACKAGECONFIG:append = " wpecryptography rdkservices"

OPENCDMI_PROVIDER ?= "entservices-opencdmi"

PACKAGECONFIG[opencdm]       = "rdk_enable_ocdm=true,rdk_enable_ocdm=false,${OPENCDMI_PROVIDER}"
PACKAGECONFIG[qa]            = ",,nodejs-native,"
PACKAGECONFIG[asan]          = "use_asan=true,,gcc-sanitizers"
PACKAGECONFIG[gold]          = ""
PACKAGECONFIG[firebolt]      = "rdk_enable_firebolt_api=true,,firebolt-cpp-client firebolt-cpp-transport"
PACKAGECONFIG[fb_rpc_v1]     = "rdk_enable_firebolt_legacy_rpc_v1=true,,"
PACKAGECONFIG[firebolt_lifecycle] = "rdk_enable_firebolt_lifecycle=true,,"
PACKAGECONFIG[wpecryptography] = "rdk_enable_wpecryptography=true,,wpeframework-clientlibraries"
PACKAGECONFIG[rdkservices]   = "rdk_enable_rdkservices_api=true,,wpeframework entservices-apis"

GN_ARGS_EXTRA ?= ""
GN_ARGS_EXTRA:append:arm = " rdk_arm_call_convention="${@bb.utils.contains('TUNE_FEATURES', 'callconvention-hard', 'hardfp', 'softfp', d)}""
GN_ARGS_EXTRA:append = " ${PACKAGECONFIG_CONFARGS}"

inherit python3native pkgconfig breakpad-wrapper ccache

BREAKPAD_BIN = "lib*.so* elf_loader_sandbox crashpad_handler"

export PYTHONPATH="${S}"
export CCACHE_COMPILERCHECK = "%compiler% -v"

python() {
    """
    Cobalt uses its own wrapper for ccache. Disable bitbake setup.
    """
    d.delVar("CCACHE")
}

do_setup_ca_certs_env() {
    # workaround broken ca-certificates-native
    local ca_certs_bundle=$(realpath "${STAGING_DIR_NATIVE}${sysconfdir}/ssl/certs/ca-certificates.crt")
    if printf '%s\n' "$ca_certs_bundle" | grep -q "^/usr/share/"; then
        ca_certs_bundle="${STAGING_DIR_NATIVE}$ca_certs_bundle"
    fi
    export SSL_CERT_FILE="$ca_certs_bundle"
    export CURL_CA_BUNDLE="$ca_certs_bundle"
}

do_unpack_extra[depends] += "ca-certificates-native:do_populate_sysroot git-replacement-native:do_populate_sysroot"
do_unpack_extra[network] = "1"
do_unpack_extra() {
    do_setup_ca_certs_env

    export PATH="${WORKDIR}/depot_tools:$PATH"
    export DEPOT_TOOLS_METRICS="0"

    # TODO: explore possibility to use yocto fetch to download dependencies
    bbnote "gclient sync"
    cd ${WORKDIR}
    gclient config -v --name=src $(git -C ${S} remote get-url origin)
    cd ${S}
    gclient sync -v --no-history -r ${SRCREV_cobalt}
    gclient runhooks -v

    bbnote "link larboard"
    ( cd "${S}/third_party/" && ln -sf ../../larboard/src/third_party/starboard . )
}
addtask unpack_extra after do_patch before do_configure

do_configure[cleandirs] = "${B}"
do_configure() {
    cd ${S}
    mkdir -p ${COBALT_OUT_DIR}
    ${PYTHON} ${S}/cobalt/build/gn.py --no-check --no-rbe -p ${COBALT_PLATFORM} -c ${COBALT_BUILD_TYPE} --out_directory ${COBALT_OUT_DIR} --rdkargs '${GN_ARGS_EXTRA}'

    mkdir -p ${COBALT_OUT_DEV_DIR}
    ${PYTHON} ${S}/cobalt/build/gn.py --no-check --no-rbe -p ${COBALT_PLATFORM} -c devel --out_directory ${COBALT_OUT_DEV_DIR} --rdkargs '${GN_ARGS_EXTRA}'
}

do_compile[progress] = "percent"
do_compile() {
    export NINJA_STATUS='%p '
    ninja ${PARALLEL_MAKE} -C ${COBALT_OUT_DIR} libloader_app.so native_target/crashpad_handler
    ninja ${PARALLEL_MAKE} -C ${COBALT_OUT_DEV_DIR} elf_loader_sandbox
}

do_install() {
    install -d ${D}${bindir}/native_target
    install -m 0755 ${COBALT_OUT_DIR}/native_target/crashpad_handler ${D}${bindir}/native_target

    install -d ${D}${libdir}
    install -m 0755 ${COBALT_OUT_DIR}/libloader_app.so ${D}${libdir}

    install -d ${D}${datadir}/content/data
    cp -av --no-preserve=ownership ${COBALT_OUT_DIR}/fonts ${D}${datadir}/content/data/

    install -m 0755 ${COBALT_OUT_DEV_DIR}/elf_loader_sandbox ${D}${bindir}
}

FILES:${PN}  = "${bindir}/native_target/crashpad_handler"
FILES:${PN} += "${libdir}/libloader_app.so"
FILES:${PN} += "${datadir}/content/data/fonts/*"

PACKAGES =+ "${PN}-tools"
FILES:${PN}-tools  = "${bindir}/elf_loader_sandbox"

FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dev += "dev-so dev-elf"
INSANE_SKIP:${PN}-dbg += "dev-so"
