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

CRX_FILE:arm:hard = "cobalt_evergreen_5.30.2_arm-hardfp_sbversion-16_release_compressed.crx"
DBG_FILE:arm:hard = "libcobalt_5.30.2_unstripped_arm-hardfp_sbversion-16_release_19a4be3b69349da0.tar.gz"
CRX_FILE_SHA256SUM:arm:hard = "708cfec0ee83b772529c2675fcdad85fa88998e7a79db8032c9de18ccddad25a"
DBG_FILE_SHA256SUM:arm:hard = "f8a5fc54feec70c9899f1abbb7cff40a980d5e603848fbb6cabe2c5978bac917"

CRX_FILE:arm:hard:cobalt-qa = "cobalt_evergreen_5.30.2_arm-hardfp_sbversion-16_qa_compressed.crx"
DBG_FILE:arm:hard:cobalt-qa = "libcobalt_5.30.2_unstripped_arm-hardfp_sbversion-16_qa_80d05cd5d4e608c6.tar.gz"
CRX_FILE_SHA256SUM:arm:hard:cobalt-qa = "71434ff0ddb4d75d476558306d434273e1bf8948674506b87d2db84cb7198701"
DBG_FILE_SHA256SUM:arm:hard:cobalt-qa = "e174047ec899ba0f9b1abc31f8ffb4e16ac296d945b9056074c541ef66e9f5ad"

CRX_FILE:aarch64 = "cobalt_evergreen_5.30.2_arm64_sbversion-16_release_compressed.crx"
DBG_FILE:aarch64 = "libcobalt_5.30.2_unstripped_arm64_sbversion-16_release_4074e859aeecf277.tar.gz"
CRX_FILE_SHA256SUM:aarch64 = "fa34dda6318aa2397efbbad33d8afc3c6829ef626636717a1159c17001d31331"
DBG_FILE_SHA256SUM:aarch64 = "214b3368dc68e403bcf5c261b2c8aacca36159d666c43d44444428ffb0309df5"

CRX_FILE:aarch64:cobalt-qa = "cobalt_evergreen_5.30.2_arm64_sbversion-16_qa_compressed.crx"
DBG_FILE:aarch64:cobalt-qa = "libcobalt_5.30.2_unstripped_arm64_sbversion-16_qa_3700514505a2d221.tar.gz"
CRX_FILE_SHA256SUM:aarch64:cobalt-qa = "cea3dc5987ed38dc6cca18e9fde2fb4c82a54e85b3aa7e79a9f3f1d161362315"
DBG_FILE_SHA256SUM:aarch64:cobalt-qa = "fefaba3c6657acab8f677691503844026e133a75e1e1bebe9d06d53cba20ce4c"

PV = "5.30.2"
YT_BASE_URI = "https://github.com/youtube/cobalt/releases/download/25.lts.30"

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
