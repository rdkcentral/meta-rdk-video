SUMMARY = "Evergreen Cobalt Core library."
HOMEPAGE = "https://cobalt.dev"

LICENSE = "BSD-3-Clause"
# See https://github.com/youtube/cobalt/blob/master/LICENSE for governing license.
# This license has been stored locally as COBALT_LICENSE
LIC_FILES_CHKSUM = "file://../COBALT_LICENSE;md5=0fca02217a5d49a14dfe2d11837bb34d"

inherit features_check
REQUIRED_DISTRO_FEATURES = "cobalt-24"

FILESEXTRAPATHS:prepend := "${THISDIR}/evergreen:"
DEPENDS += "unzip-native breakpad-native"
OVERRIDES:append = ":${TARGET_FPU}:${@bb.utils.filter('DISTRO_FEATURES', 'cobalt-qa', d)}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

CRX_FILE:arm:hard = "cobalt_evergreen_4.40.2_arm-hardfp_sbversion-15_release_20240426165046.crx"
DBG_FILE:arm:hard = "libcobalt_4.40.2_unstripped_arm-hardfp_sbversion-15_release_1533d2241e44b8b5.tar.gz"
CRX_FILE_SHA256SUM:arm:hard = "f4c40d1d5c5049daa421a1d5bb1a91090a8c773cca779fff0d790c3f6dc80778"
DBG_FILE_SHA256SUM:arm:hard = "6a240ad94af73610b107fde11e65bf5f90913c87c423bbfbc4669ec9473fe48d"

CRX_FILE:arm:hard:cobalt-qa = "cobalt_evergreen_4.40.2_arm-hardfp_sbversion-15_qa_20240426165046.crx"
DBG_FILE:arm:hard:cobalt-qa = "libcobalt_4.40.2_unstripped_arm-hardfp_sbversion-15_qa_05d1436966471d82.tar.gz"
CRX_FILE_SHA256SUM:arm:hard:cobalt-qa = "dfb7c3d059f00423cb06c5aee7c8c53ec8620ccbb2e26e81318050f677b8e862"
DBG_FILE_SHA256SUM:arm:hard:cobalt-qa = "c38d7021a6bd4b186e8b08f8c19a3c7600c15cb4dc398db5b9a0937474ae4a95"

CRX_FILE:aarch64 = "cobalt_evergreen_4.40.2_arm64_sbversion-15_release_20240426165046.crx"
DBG_FILE:aarch64 = "libcobalt_4.40.2_unstripped_arm64_sbversion-15_release_fce9f2ea016f623a.tar.gz"
CRX_FILE_SHA256SUM:aarch64 = "a796fc4a0882b1348bdc27e34193bb4efc86c62c392c164a741665aa84a01f28"
DBG_FILE_SHA256SUM:aarch64 = "ea258902467f4a6c70b03a458a4f61ed3ed76d4fa029a3aeb6cc8af6a2a1d943"

CRX_FILE:aarch64:cobalt-qa = "cobalt_evergreen_4.40.2_arm64_sbversion-15_qa_20240426165046.crx"
DBG_FILE:aarch64:cobalt-qa = "libcobalt_4.40.2_unstripped_arm64_sbversion-15_qa_79c5656b91b57f7a.tar.gz"
CRX_FILE_SHA256SUM:aarch64:cobalt-qa = "fc7dd3aa3914550e6873ca33e63e55cb583c4f756b255fbbaf864e0a15983842"
DBG_FILE_SHA256SUM:aarch64:cobalt-qa = "291e9f46dad2193e3e9990c690cbbbe8b32bd770fbc4df9ef19237d1ca57484e"

PV = "4.40.2"
YT_BASE_URI = "https://github.com/youtube/cobalt/releases/download/24.lts.40"
SRC_URI  = "${YT_BASE_URI}/${CRX_FILE};name=cobalt"
SRC_URI += "${YT_BASE_URI}/${DBG_FILE};name=cobalt_debug;subdir=debug_syms"
SRC_URI += "file://COBALT_LICENSE"
SRC_URI[cobalt.sha256sum] = "${CRX_FILE_SHA256SUM}"
SRC_URI[cobalt_debug.sha256sum] = "${DBG_FILE_SHA256SUM}"

COBALT_APP_DIR = "/content/data/app/cobalt"

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

    # intentionally install unstripped libcobalt.so, so symbol generation step will make use of debug libcobalt.so
    install -d "${D}${datadir}${COBALT_APP_DIR}/lib/"
    install -m 0755 ${WORKDIR}/debug_syms/libcobalt.so ${D}${datadir}${COBALT_APP_DIR}/lib/
}


FILES:${PN}  = "${datadir}${COBALT_APP_DIR}/content/*"
FILES:${PN} += "${datadir}${COBALT_APP_DIR}/manifest.json"
FILES:${PN} += "${datadir}${COBALT_APP_DIR}/lib/libcobalt.so"
FILES_SOLIBSDEV = ""

INSANE_SKIP:${PN} += "dev-so "
INSANE_SKIP:${PN}-dbg += "dev-so "

PROVIDES = "virtual/cobalt-evergreen"
RPROVIDES:${PN} = "virtual/cobalt-evergreen"
