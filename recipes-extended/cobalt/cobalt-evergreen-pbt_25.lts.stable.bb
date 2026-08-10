SUMMARY = "Evergreen Cobalt Core library."
HOMEPAGE = "https://cobalt.dev"

LICENSE = "BSD-3-Clause"
# See https://github.com/youtube/cobalt/blob/master/LICENSE for governing license.
# This license has been stored locally as COBALT_LICENSE
LIC_FILES_CHKSUM = "file://../COBALT_LICENSE;md5=0fca02217a5d49a14dfe2d11837bb34d"

inherit features_check
CONFLICT_DISTRO_FEATURES = "cobalt-24"

FILESEXTRAPATHS:prepend := "${THISDIR}/evergreen:"
DEPENDS += "unzip-native breakpad-native"
OVERRIDES:append = ":${TARGET_FPU}:${@bb.utils.filter('DISTRO_FEATURES', 'cobalt-qa', d)}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

CRX_FILE:arm:hard = "cobalt_evergreen_5.40.2_arm-hardfp_sbversion-16_release_compressed_20260323230717.crx"
DBG_FILE:arm:hard = "libcobalt_5.40.2_unstripped_arm-hardfp_sbversion-16_release_d5ff880217955882.tar.gz"
CRX_FILE_SHA256SUM:arm:hard = "c60d2a1b83d3cb7a9a788145c93f9a697eb651d561c1d61cf7665d6380bb9676"
DBG_FILE_SHA256SUM:arm:hard = "d039771bee5e2758daf27c3553bf324e51fbd52e789b15af5efbc23efd89699d"

CRX_FILE:arm:hard:cobalt-qa = "cobalt_evergreen_5.40.2_arm-hardfp_sbversion-16_qa_compressed_20260323230717.crx"
DBG_FILE:arm:hard:cobalt-qa = "libcobalt_5.40.2_unstripped_arm-hardfp_sbversion-16_qa_4a20fadc5abe3a92.tar.gz"
CRX_FILE_SHA256SUM:arm:hard:cobalt-qa = "aba2e96b9ee5f152101548dd57884250ae15f7089bfd549bbdfc59e7751b8695"
DBG_FILE_SHA256SUM:arm:hard:cobalt-qa = "5668996674def1a6914ef31a60e37683e061485adc1fcc4e57dd0ea1af114cbb"

CRX_FILE:aarch64 = "cobalt_evergreen_5.40.2_arm64_sbversion-16_release_compressed_20260323230717.crx"
DBG_FILE:aarch64 = "libcobalt_5.40.2_unstripped_arm64_sbversion-16_release_54b1f0fa9caca249.tar.gz"
CRX_FILE_SHA256SUM:aarch64 = "8c6d09d4669eb29c2aa2aaa580201ee72039dba0e32793323a5c90a084e3102c"
DBG_FILE_SHA256SUM:aarch64 = "9da13e749be9cdf3631d168dd5064966ea4724ea99a09f4cf57798c5832e41ef"

CRX_FILE:aarch64:cobalt-qa = "cobalt_evergreen_5.40.2_arm64_sbversion-16_qa_compressed_20260323230717.crx"
DBG_FILE:aarch64:cobalt-qa = "libcobalt_5.40.2_unstripped_arm64_sbversion-16_qa_3f7911fdfad96f7e.tar.gz"
CRX_FILE_SHA256SUM:aarch64:cobalt-qa = "e00f9583a696d91503f1a3f1d0c8a24663a78d6fb203ade12eef2f6625339a0c"
DBG_FILE_SHA256SUM:aarch64:cobalt-qa = "d29ac9a53e029c36c3b2183ff83dd186ff5bdc5986bf4a627b1fce58a94611ce"

PV = "5.40.2"
YT_BASE_URI = "https://github.com/youtube/cobalt/releases/download/25.lts.40"

SRC_URI  = "${YT_BASE_URI}/${CRX_FILE};name=cobalt"
SRC_URI += "${YT_BASE_URI}/${DBG_FILE};name=cobalt_debug;subdir=debug_syms"
SRC_URI += "file://COBALT_LICENSE"
SRC_URI[cobalt.sha256sum] = "${CRX_FILE_SHA256SUM}"
SRC_URI[cobalt_debug.sha256sum] = "${DBG_FILE_SHA256SUM}"

COBALT_APP_DIR = "/content/data/app/cobalt"

inherit breakpad-wrapper
breakpad_package_preprocess () {
    machine_dir="${@d.getVar('MACHINE', True)}"

    binary="$(readlink -m "${D}${datadir}${COBALT_APP_DIR}/lib/.debug/libcobalt.so")"
    bbnote "Dumping symbols from $binary -> ${TMPDIR}/deploy/breakpad_symbols/$machine_dir/libcobalt.lz4.sym"

    mkdir -p ${TMPDIR}/deploy/breakpad_symbols/$machine_dir
    dump_syms -n libcobalt.lz4 "${binary}" > "${TMPDIR}/deploy/breakpad_symbols/$machine_dir/libcobalt.lz4.sym" || echo "dump_syms finished with errorlevel $?"
}

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_preunpack_cleanup() {
    bbnote "cleanup debug syms"
    rm -rf ${WORKDIR}/debug_syms
}
addtask preunpack_cleanup after do_fetch before do_unpack

do_install() {
    install -d "${D}${datadir}${COBALT_APP_DIR}"

    err_code=0

    set +e
    unzip -q -o -d "${D}${datadir}${COBALT_APP_DIR}" "${WORKDIR}/${CRX_FILE}" || err_code=$?
    set -e

    case $err_code in
     0) bbnote "All good";;
     1) bbwarn "Ignore unzip warnings";;
     *) bbfatal "Unzip failed, exit code: $err_code"
    esac

    install -d "${D}${datadir}${COBALT_APP_DIR}/lib/.debug"
    install -m 0755 ${WORKDIR}/debug_syms/libcobalt.so ${D}${datadir}${COBALT_APP_DIR}/lib/.debug
}

FILES:${PN}  = "${datadir}${COBALT_APP_DIR}/content/*"
FILES:${PN} += "${datadir}${COBALT_APP_DIR}/manifest.json"
FILES:${PN} += "${datadir}${COBALT_APP_DIR}/lib/libcobalt.lz4"
FILES:${PN}-dbg += "${datadir}${COBALT_APP_DIR}/lib/.debug/libcobalt.so"
FILES:SOLIBSDEV = ""

INSANE_SKIP:${PN} += "dev-so "
INSANE_SKIP:${PN}-dbg += "dev-so libdir "

PROVIDES = "virtual/cobalt-evergreen"
RPROVIDES:${PN} = "virtual/cobalt-evergreen"
