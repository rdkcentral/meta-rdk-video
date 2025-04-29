DESCRIPTION = "Boot version loader for finding boot type"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${THISDIR}/files/Apache-2.0;md5=3b83ef96387f14655fc854ddc3c6bd57"

RDEPENDS:${PN} += " bash"

inherit systemd

SRC_URI += "file://bootversion-loader.sh"
SRC_URI += "file://bootversion-loader.service"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

do_install:append () {
    install -d ${D}/lib/systemd/system
    install -m 0644 ${WORKDIR}/bootversion-loader.service ${D}/lib/systemd/system/bootversion-loader.service
    install -d ${D}/lib/rdk
    install -m 0755 ${WORKDIR}/bootversion-loader.sh ${D}/lib/rdk/bootversion-loader.sh

}

SYSTEMD_SERVICE:${PN} = "bootversion-loader.service"
FILES:${PN} += "${systemd_unitdir}/system/bootversion-loader.service"
FILES:${PN} += "/lib/rdk/bootversion-loader.sh"
