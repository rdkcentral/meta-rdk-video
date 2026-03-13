DESCRIPTION = "Boot version loader for finding boot type"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${THISDIR}/files/Apache-2.0;md5=3b83ef96387f14655fc854ddc3c6bd57"

RDEPENDS:${PN} += " bash"

inherit systemd

SRC_URI += "file://bootversion-loader.sh"
SRC_URI += "file://bootversion-loader.service"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

do_install:append () {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/bootversion-loader.service ${D}${systemd_unitdir}/system/bootversion-loader.service
    if [ "${BOOT_FSR_PLATFORM}" = "flex" ]; then
        sed -i 's/^After=ecfs-init.service$/After=ecfs-init.service storagemgrmain.service/' ${D}${systemd_unitdir}/system/bootversion-loader.service
    fi
    install -d ${D}${base_libdir}/rdk
    install -m 0755 ${WORKDIR}/bootversion-loader.sh ${D}${base_libdir}/rdk/bootversion-loader.sh
}

SYSTEMD_SERVICE:${PN} = "bootversion-loader.service"
FILES:${PN} += "${systemd_unitdir}/system/bootversion-loader.service"
FILES:${PN} += "${base_libdir}/rdk/bootversion-loader.sh"
