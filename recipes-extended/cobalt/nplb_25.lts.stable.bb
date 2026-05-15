SUMMARY = "Starboard NPLB."
HOMEPAGE = "https://cobalt.dev"

LICENSE = "BSD-3-Clause & Apache-2.0-with-LLVM-exception"
LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=0fca02217a5d49a14dfe2d11837bb34d \
"


TOOLCHAINS_DIR = "starboard-toolchains"
CLANG_BUILD_REVISION = "17-init-8029-g27f27d15-3"
CLANG_BUILD_SUBDIR = "${TOOLCHAINS_DIR}/x86_64-linux-gnu-clang-chromium-${CLANG_BUILD_REVISION}"

PATCHTOOL = "git"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI  = "git://github.com/youtube/cobalt.git;protocol=https;name=cobalt;branch=25.lts.stable"
SRC_URI += "https://commondatastorage.googleapis.com/chromium-browser-clang/Linux_x64/clang-llvmorg-${CLANG_BUILD_REVISION}.tgz;subdir=${CLANG_BUILD_SUBDIR};name=clang"
SRC_URI += "file://25/0006-Use-certifi-to-tell-urllib-where-to-find-CA-file-397.patch"
SRC_URI += "file://25/0001-Fix-NPBL-with-cast_codec_tests-config.patch"
SRC_URI += "file://25/0001-Ensure-GCS-buckets-are-publicly-accessible-mirrors-5.patch"

SRC_URI[clang.sha256sum] = "1ac590c011158940037ce9442d4bf12943dc14a7ddaab6094e75a8750b47b861"

CR = "30"
PR = "r${CR}"
SRCREV_cobalt = "25.lts.${CR}"
do_fetch[vardeps] += " SRCREV_FORMAT SRCREV_cobalt"

DEPENDS  = "ninja-native bison-native openssl-native gn-native ccache-native nodejs-native"
DEPENDS += " python3-six-native python3-urllib3-native"

S = "${WORKDIR}/git"

inherit python3native pkgconfig breakpad-wrapper ccache

BREAKPAD_BIN = "libnplb*"

SB_VERSION = "16"
COBALT_ARCH:arm = "arm-${@bb.utils.contains('TUNE_FEATURES', 'callconvention-hard', 'hardfp', 'softfp', d)}"
COBALT_ARCH:aarch64 = "arm64"
COBALT_PLATFORM ?= "evergreen-${COBALT_ARCH}"
COBALT_APP_DIR = "/content/data/app/nplb"

PACKAGECONFIG ?= "${@bb.utils.contains('DISTRO_FEATURES', 'enable-google-chromecast', 'cast_codec_tests', '', d)}"
PACKAGECONFIG[cast_codec_tests] = "sb_enable_cast_codec_tests=true,,"

GN_ARGS_EXTRA ?= ""
GN_ARGS_EXTRA:append = " sb_api_version=${SB_VERSION}"
GN_ARGS_EXTRA:append = " ${PACKAGECONFIG_CONFARGS}"

libdir = "${datadir}${COBALT_APP_DIR}/lib"

export _STARBOARD_TOOLCHAINS_DIR_KEY = "${WORKDIR}/${TOOLCHAINS_DIR}"
export NINJA_STATUS='%p '
export PYTHONPATH="${S}"
export CCACHE_COMPILERCHECK = "%compiler% -v"

python() {
    """
    Cobalt uses its own wrapper for ccache. Disable bitbake setup.
    """
    d.delVar("CCACHE")
}

do_unpack_extra() {
    bbnote "Update toolchain build revision"
    echo -n ${CLANG_BUILD_REVISION} > ${WORKDIR}/${CLANG_BUILD_SUBDIR}/cr_build_revision
}
addtask unpack_extra after do_patch before do_configure

do_configure() {
    ${PYTHON} cobalt/build/gn.py -c devel  -p ${COBALT_PLATFORM} --overwrite_args
    echo "${GN_ARGS_EXTRA}" | tr ' ' '\n' >> out/${COBALT_PLATFORM}_devel/args.gn
}

do_compile[progress] = "percent"
# To resolve jenkins build error (https://docs.yoctoproject.org/4.0.4/migration-guides/migration-4.0.html#fetching-changes)
do_compile[network] = "1"

do_compile() {
    export NINJA_STATUS='%p '
    ninja -C out/${COBALT_PLATFORM}_devel nplb
}

do_install() {
    install -d ${D}${libdir}
    install -m 0755 out/${COBALT_PLATFORM}_devel/libnplb.so ${D}${libdir}
    install -d ${D}${datadir}${COBALT_APP_DIR}/content
    cp -arv --no-preserve=ownership out/${COBALT_PLATFORM}_devel/content ${D}${datadir}${COBALT_APP_DIR}/
}

FILES:${PN}  = "${libdir}/libnplb.so"
FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"

PACKAGES =+ "${PN}-test-data"
PROVIDES += "${PN}-test-data"
FILES:${PN}-test-data = "${datadir}${COBALT_APP_DIR}/content/*"
