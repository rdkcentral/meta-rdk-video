SUMMARY = "Evergreen Cobalt Core library."
HOMEPAGE = "https://cobalt.dev"

LICENSE = "BSD-3-Clause"
# See https://github.com/youtube/cobalt/blob/master/LICENSE for governing license.
# This license has been stored locally as COBALT_LICENSE
LIC_FILES_CHKSUM = "file://../COBALT_LICENSE;md5=0fca02217a5d49a14dfe2d11837bb34d"

inherit features_check
REQUIRED_DISTRO_FEATURES = "cobalt-25"

FILESEXTRAPATHS:prepend := "${THISDIR}/evergreen:"
DEPENDS += "unzip-native breakpad-native"
OVERRIDES:append = ":${TARGET_FPU}:${@bb.utils.filter('DISTRO_FEATURES', 'cobalt-qa', d)}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

CRX_FILE:arm:hard = "cobalt_evergreen_5.20.2_arm-hardfp_sbversion-16_release_compressed_20241114054938.crx"
DBG_FILE:arm:hard = "libcobalt_5.20.2_unstripped_arm-hardfp_sbversion-16_release_9c18aea849c0a74c.tar.gz"
CRX_FILE_SHA256SUM:arm:hard = "8dd6b59e0a6518d3c16195ca25c9307606c0ab6e2d673e01a381847abf54f94b"
DBG_FILE_SHA256SUM:arm:hard = "5c0724d427f09bc3f8d7ec1b5aaef7cac8c2bce7199f674fd52b56b1f3cb10c5"

CRX_FILE:arm:hard:cobalt-qa = "cobalt_evergreen_5.20.2_arm-hardfp_sbversion-16_qa_compressed_20241114054938.crx"
DBG_FILE:arm:hard:cobalt-qa = "libcobalt_5.20.2_unstripped_arm-hardfp_sbversion-16_qa_f9e9c93a32f9f855.tar.gz"
CRX_FILE_SHA256SUM:arm:hard:cobalt-qa = "abcd6b5fdcb80cc2c2d5ae7a124a64de526ededfa9a5fb6c820b69e706979de0"
DBG_FILE_SHA256SUM:arm:hard:cobalt-qa = "5a2c587342bff95d8cb040fc39099142119a16f0671d3f922fc557ddfa6b6032"

CRX_FILE:aarch64 = "cobalt_evergreen_5.20.2_arm64_sbversion-16_release_compressed_20241114054938.crx"
DBG_FILE:aarch64 = "libcobalt_5.20.2_unstripped_arm64_sbversion-16_release_5e3b548a71263e2b.tar.gz"
CRX_FILE_SHA256SUM:aarch64 = "28987158fb7a7f527c44c238d30fcbcd6114fbe84bf89cdefaccd0e03257fc2b"
DBG_FILE_SHA256SUM:aarch64 = "db5b2949b57b5404d90ed586c5de15a8bd9d729385222bd3f45b4e1c8d13e281"

CRX_FILE:aarch64:cobalt-qa = "cobalt_evergreen_5.20.2_arm64_sbversion-16_qa_compressed_20241114054938.crx"
DBG_FILE:aarch64:cobalt-qa = "libcobalt_5.20.2_unstripped_arm64_sbversion-16_qa_e1a6d8f62011175c.tar.gz"
CRX_FILE_SHA256SUM:aarch64:cobalt-qa = "83427d7ce019a717468253d7725a8db42bc68d3450774de223adb18559908d13"
DBG_FILE_SHA256SUM:aarch64:cobalt-qa = "99d48062d42d94e15ee98d4d2589bb565718953b8e5bd77974e8cc063dc73e4f"

PV = "5.20.2"
YT_BASE_URI = "https://github.com/youtube/cobalt/releases/download/25.lts.20"

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
