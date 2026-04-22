SUMMARY = "GStreamer registry cleanup service"
DESCRIPTION = "GStreamer registry cleanup service for post-CDL bootup"
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://gstreamer-cleanup.sh;beginline=3;endline=15;md5=269c153d328c8300dccfb760a4282b08"

PV = "1.0.0"
PR = "r0"

RDEPENDS:${PN} += "bash"

inherit systemd

SRC_URI += "file://gstreamer-cleanup.sh"
SRC_URI += "file://gstreamer-cleanup.service"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

do_install:append () 
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/gstreamer-cleanup.service ${D}${systemd_unitdir}/system/gstreamer-cleanup.service
    install -d ${D}${base_libdir}/rdk
    install -m 0755 ${WORKDIR}/gstreamer-cleanup.sh ${D}${base_libdir}/rdk/gstreamer-cleanup.sh
}

SYSTEMD_SERVICE:${PN} = "gstreamer-cleanup.service"
SYSTEMD_AUTO_ENABLE = "enable"
FILES:${PN} += "${systemd_unitdir}/system/gstreamer-cleanup.service"
FILES:${PN} += "${base_libdir}/rdk/gstreamer-cleanup.sh"
