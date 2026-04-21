DESCRIPTION = "GStreamer registry cleanup service for post-CDL bootup"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${THISDIR}/files/Apache-2.0;md5=3b83ef96387f14655fc854ddc3c6bd57"

RDEPENDS:${PN} += " bash"

inherit systemd

SRC_URI += "file://gstreamer-cleanup.sh"
SRC_URI += "file://gstreamer-cleanup.service"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

do_install:append () {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/gstreamer-cleanup.service ${D}${systemd_unitdir}/system/gstreamer-cleanup.service
    install -d ${D}${base_libdir}/rdk
    install -m 0755 ${WORKDIR}/gstreamer-cleanup.sh ${D}${base_libdir}/rdk/gstreamer-cleanup.sh
}

SYSTEMD_SERVICE:${PN} = "gstreamer-cleanup.service"
SYSTEMD_AUTO_ENABLE = "enable"
FILES:${PN} += "${systemd_unitdir}/system/gstreamer-cleanup.service"
FILES:${PN} += "${base_libdir}/rdk/gstreamer-cleanup.sh"
