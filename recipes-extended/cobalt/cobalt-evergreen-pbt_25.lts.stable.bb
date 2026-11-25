SUMMARY = "Evergreen Cobalt Core library."
HOMEPAGE = "https://cobalt.dev"

LICENSE = "BSD-3-Clause"
# See https://github.com/youtube/cobalt/blob/master/LICENSE for governing license.
# This license has been stored locally as COBALT_LICENSE
LIC_FILES_CHKSUM = "file://../COBALT_LICENSE;md5=0fca02217a5d49a14dfe2d11837bb34d"

FILESEXTRAPATHS:prepend := "${THISDIR}/evergreen:"
DEPENDS += "unzip-native breakpad-native"
OVERRIDES:append = ":${TARGET_FPU}:${@bb.utils.filter('DISTRO_FEATURES', 'cobalt-qa', d)}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

CRX_FILE:arm:hard = "cobalt_evergreen_5.60.2_arm-hardfp_sbversion-16_release_compressed.crx"
DBG_FILE:arm:hard = "libcobalt_5.60.2_unstripped_arm-hardfp_sbversion-16_release_f73e7e357cdbd69b.tar.gz"
CRX_FILE_SHA256SUM:arm:hard = "800351a374e40e5d6a901db563e31d2ad7bac741a32ca75528fb8f541d87f396"
DBG_FILE_SHA256SUM:arm:hard = "6d16d45a792ce213c8efba139bd1f057ee2b803610038d98341e4fc691326f58"

CRX_FILE:arm:hard:cobalt-qa = "cobalt_evergreen_5.60.2_arm-hardfp_sbversion-16_qa_compressed.crx"
DBG_FILE:arm:hard:cobalt-qa = "libcobalt_5.60.2_unstripped_arm-hardfp_sbversion-16_qa_06c11e9779c33f9d.tar.gz"
CRX_FILE_SHA256SUM:arm:hard:cobalt-qa = "455803f5eff624a37cabfa980b3a0b794da952117d9db41167cbd80b7b1a9065"
DBG_FILE_SHA256SUM:arm:hard:cobalt-qa = "e10b05e5807d5327ec23f088e811fcf8c40accb9d5fe47abda0c80fd3aa8265b"

CRX_FILE:aarch64 = "cobalt_evergreen_5.60.2_arm64_sbversion-16_release_compressed.crx"
DBG_FILE:aarch64 = "libcobalt_5.60.2_unstripped_arm64_sbversion-16_release_4f4fc6db18bff560.tar.gz"
CRX_FILE_SHA256SUM:aarch64 = "15a20fc0b929bb8e5e4223cd937027c66e46f78006f709507eb9daab94d50e54"
DBG_FILE_SHA256SUM:aarch64 = "df8f0c5ad8def75e177bc169ba282d69747a52a8e187c8faebc37d5b2b257750"

CRX_FILE:aarch64:cobalt-qa = "cobalt_evergreen_5.60.2_arm64_sbversion-16_qa_compressed.crx"
DBG_FILE:aarch64:cobalt-qa = "libcobalt_5.60.2_unstripped_arm64_sbversion-16_qa_495e0cda50338d05.tar.gz"
CRX_FILE_SHA256SUM:aarch64:cobalt-qa = "e540a885e715a8bc273ba7ea25dca563ffe7293d7045dc6ee47253a0ebe62a24"
DBG_FILE_SHA256SUM:aarch64:cobalt-qa = "4c06bf40b2b7e2b1f287c9599106461553cccde6700c1da2bcd38efae3f87620"

PV = "5.60.2"
YT_BASE_URI = "https://github.com/youtube/cobalt/releases/download/25.lts.60"

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
