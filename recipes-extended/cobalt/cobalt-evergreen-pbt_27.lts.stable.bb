SUMMARY = "Evergreen Cobalt Core library."
HOMEPAGE = "https://cobalt.dev"

LICENSE = "BSD-3-Clause"
# See https://github.com/youtube/cobalt/blob/master/LICENSE for governing license.
# This license has been stored locally as COBALT_LICENSE
LIC_FILES_CHKSUM = "file://../COBALT_LICENSE;md5=0fca02217a5d49a14dfe2d11837bb34d"

inherit features_check
# REQUIRED_DISTRO_FEATURES = "cobalt-27"

FILESEXTRAPATHS:prepend := "${THISDIR}/evergreen:"
DEPENDS += "unzip-native breakpad-native"
OVERRIDES:append = ":${TARGET_FPU}:${@bb.utils.filter('DISTRO_FEATURES', 'cobalt-qa', d)}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

CRX_FILE:arm:hard = "cobalt_evergreen_7.3.2_arm-hardfp_sbversion-18_release_compressed_20260905002529.crx"
DBG_FILE:arm:hard = "libcobalt_7.3.2_unstripped_arm-hardfp_sbversion-18_release_ba798964bd962c39eb6a535033138fa0fddfcd1f.tar.gz"
CRX_FILE_SHA256SUM:arm:hard = "de021cc1e4bb2f851bada1541f4f5489e88a314d37a78c5469af0433d8141a81"
DBG_FILE_SHA256SUM:arm:hard = "eb91e018f2d609c32bb73c058ff15b5f5281f28f7ee20bd20a39be2a7843b4b8"

CRX_FILE:arm:hard:cobalt-qa = "cobalt_evergreen_7.3.2_arm-hardfp_sbversion-18_qa_compressed_20260905002529.crx"
DBG_FILE:arm:hard:cobalt-qa = "libcobalt_7.3.2_unstripped_arm-hardfp_sbversion-18_qa_fa6930e2c0784dd58e7e1555ebd6e98f496a40dd.tar.gz"
CRX_FILE_SHA256SUM:arm:hard:cobalt-qa = "1dbb99dbc81f86141620d0f20381fb571dd0db06def14eb2380337c03b929258"
DBG_FILE_SHA256SUM:arm:hard:cobalt-qa = "ad6b4249109d537e263e5390fcbadd1fcec03e6e871f25b0d9d2d9bafc93e91a"

CRX_FILE:aarch64 = "cobalt_evergreen_7.3.2_arm64_sbversion-18_release_compressed_20260905002529.crx"
DBG_FILE:aarch64 = "libcobalt_7.3.2_unstripped_arm64_sbversion-18_release_913700b8c4b61485d037f918f9ece60e797fecdd.tar.gz"
CRX_FILE_SHA256SUM:aarch64 = "5280d8f97cf2dfc61bb19749a54c873134de6fe42af622d43b6b823addd27908"
DBG_FILE_SHA256SUM:aarch64 = "6ae4dca7d1a9ef0a43308f1d64369c8b0226ac74be3d4d4d5e092710ccda7460"

CRX_FILE:aarch64:cobalt-qa = "cobalt_evergreen_7.3.2_arm64_sbversion-18_qa_compressed_20260905002529.crx"
DBG_FILE:aarch64:cobalt-qa = "libcobalt_7.3.2_unstripped_arm64_sbversion-18_qa_4a901ddb638aa09e06c23fef7d6a34fc57d2595c.tar.gz"
CRX_FILE_SHA256SUM:aarch64:cobalt-qa = "9ce88ceb9ded430de43b6f5ac91e6a91735bf2de0c92a92f697f4b5afb4f8866"
DBG_FILE_SHA256SUM:aarch64:cobalt-qa = "90b7ae7a121313ed048832d68666efda3a1f321ddfec959e7c9da545a1fcffd6"

PV = "7.3.2"
YT_BASE_URI = "https://github.com/youtube/cobalt/releases/download/27.lts.3"

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
