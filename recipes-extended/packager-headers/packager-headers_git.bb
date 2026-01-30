SUMMARY = "Package Headers"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

PV = "1.0.0"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRCREV = "d61505e277eebcf5df6ac650b5812c3150eec134"
SRC_URI = "${CMF_GITHUB_ROOT}/eshelpers;${CMF_GITHUB_SRC_URI_SUFFIX};name=eshelpers"

S = "${WORKDIR}/git"

do_compile[noexec] = "1"
do_configure[noexec] = "1"

do_install() {
    install -d ${D}${includedir}
    install -m 0644 ${S}/packager/IPackageImpl.h ${D}${includedir}
}
ALLOW_EMPTY:${PN} = "1"
