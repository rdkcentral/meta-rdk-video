SUMMARY = "aihelpers Headers"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=99e7c83e5e6f31c2cbb811e186972945"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI = "https://github.com/rdkcentral/eshelpers.git;protocol=https;branch=topic/RDKECOREMW-864"

S = "${WORKDIR}/git"

do_compile[noexec] = "1"

do_install() {
    install -d ${D}${includedir}
    install -m 0644 ${S}/packager/IpackageImpl.h ${D}${includedir}
}
ALLOW_EMPTY:${PN} = "1"
