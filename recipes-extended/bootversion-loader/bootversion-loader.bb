DESCRIPTION = "Boot version loader for finding boot type"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${MANIFEST_PATH_RDK_VIDEO}/licenses/Apache-2.0;md5=3b83ef96387f14655fc854ddc3c6bd57"

RDEPENDS:${PN} += " bash"

inherit systemd

SRC_URI += "file://bootversion-loader.sh"
SRC_URI += "file://bootversion-loader.service"
SRC_URI += "file://boot_FSR.sh"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

python __anonymous__() {
    if d.getVar("XUMOTV_MIGRRATION_ENTOS") == "yes":
        d.appendVar("FILES:${PN}", " /lib/rdk/bootversion-loader.sh")
        d.appendVar("FILES:${PN}", " /lib/rdk/boot_FSR.sh")
}

do_install:append () {
    install -d ${D}/lib/systemd/system
    install -m 0644 ${WORKDIR}/bootversion-loader.service ${D}/lib/systemd/system/bootversion-loader.service
    if [ "${XUMOTV_MIGRRATION_ENTOS}" = "yes" ]; then
         install -d ${D}/lib/rdk
    	 install -m 0755 ${WORKDIR}/bootversion-loader.sh ${D}/lib/rdk/bootversion-loader.sh
    	 install -m 0755 ${WORKDIR}/boot_FSR.sh ${D}/lib/rdk/boot_FSR.sh
    else
    	echo "bootversion-loader:skipping install"

}

SYSTEMD_SERVICE:${PN} = "bootversion-loader.service"
FILES:${PN} += "${systemd_unitdir}/system/bootversion-loader.service"
