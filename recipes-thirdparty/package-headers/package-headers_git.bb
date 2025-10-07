SUMMARY = "Package Headers"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI = "${CMF_GITHUB_ROOT}/eshelpers;${CMF_GITHUB_SRC_URI_SUFFIX};name=eshelpers"

S = "${WORKDIR}/git"

do_compile[noexec] = "1"

do_install() {
    install -d ${D}${includedir}
    install -m 0644 ${S}/packager/IpackageImpl.h ${D}${includedir}
}
ALLOW_EMPTY:${PN} = "1"
