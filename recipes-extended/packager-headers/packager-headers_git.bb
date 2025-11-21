SUMMARY = "Package Headers"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRC_URI = "${CMF_GITHUB_ROOT}/eshelpers;${CMF_GITHUB_SRC_URI_SUFFIX};name=eshelpers"

S = "${WORKDIR}/git"

do_compile[noexec] = "1"
do_configure[noexec] = "1"

do_install() {
    install -d ${D}${includedir}
    install -m 0644 ${S}/packager/IPackageImpl.h ${D}${includedir}
}
ALLOW_EMPTY:${PN} = "1"

SRCREV_eshelpers = "c18a6cdf4c87fc43bf920167781a537efa225eba"

