SUMMARY = "IARMMGRS HAL interface headers for L2HalMock"
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=83a31d934b0cc2ab2d44a329445b4366"

SRCREV = "a5781a35ac728b4d4b57a8a82e07e9b3b63c65f9"
SRC_URI = "git://github.com/rdkcentral/iarmmgrs.git;branch=main;protocol=https"

S = "${WORKDIR}/git"

do_configure() {
    :
}

do_compile() {
    :
}

do_install() {
    install -d ${D}${includedir}/rdk/iarmmgrs-hal
    install -m 0644 ${S}/hal/include/*.h ${D}${includedir}/rdk/iarmmgrs-hal/
}

FILES:${PN} += "${includedir}/rdk/iarmmgrs-hal"
