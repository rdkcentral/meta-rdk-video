DESCRIPTION = "RDK Wpa Supplicant Configurations"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${THISDIR}/files/Apache-2.0;md5=3b83ef96387f14655fc854ddc3c6bd57"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SECTION = "base"
DEPENDS = "systemd"
RDEPENDS:${PN} = "wpa-supplicant systemd"

inherit systemd

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI = "file://prepareWpaSuppConfig.sh"
SRC_URI += "file://00-wpa-supplicant.conf"
SRC_URI += "file://wpa_supplicant.logging"

do_install() {
    install -d ${D}${base_libdir}/rdk/
    install -d ${D}${sysconfdir}
    install -m 0755 ${WORKDIR}/prepareWpaSuppConfig.sh ${D}${base_libdir}/rdk
    install -m 0644 ${WORKDIR}/wpa_supplicant.logging ${D}${sysconfdir}
    install -D -m 0644 ${WORKDIR}/00-wpa-supplicant.conf ${D}${systemd_unitdir}/system/wpa_supplicant.service.d/00-wpa-supplicant.conf
}

FILES:${PN} += " ${base_libdir}/rdk/prepareWpaSuppConfig.sh"
FILES:${PN} += " ${systemd_unitdir}/system/wpa_supplicant.service.d/*"
FILES:${PN} += " ${sysconfdir}/wpa_supplicant.logging"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

