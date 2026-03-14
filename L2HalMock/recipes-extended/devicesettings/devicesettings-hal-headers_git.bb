SUMMARY = "DeviceSettings HAL interface headers for L2HalMock"
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "af2fbac2390530012e298a3f7c135ef30ae0e091"
SRC_URI = "git://github.com/rdkcentral/rdk-halif-device_settings.git;branch=main;protocol=https \
           file://intErr.patch"

S = "${WORKDIR}/git"

do_install() {
    install -d ${D}${includedir}/rdk/ds-hal
    install -d ${D}${includedir}/rdk/halif/ds-hal
    install -m 0644 ${S}/include/*.h ${D}${includedir}/rdk/ds-hal/
    install -m 0644 ${S}/include/*.h ${D}${includedir}/rdk/halif/ds-hal/
}

FILES:${PN} += "${includedir}/rdk/ds-hal ${includedir}/rdk/halif/ds-hal"
