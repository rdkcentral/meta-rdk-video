SUMMARY = "Create wlan p2p device and rename it for mediacast usage"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${THISDIR}/files/Apache-2.0;md5=3b83ef96387f14655fc854ddc3c6bd57"

DEPENDS = "wpa-supplicant"

SRC_URI = "\
        file://wlan-p2p.sh \
        file://wlan-p2p.service \
        "
S = "${WORKDIR}"

inherit systemd syslog-ng-config-gen

SYSLOG-NG_FILTER = "wlan_p2p"
SYSLOG-NG_SERVICE_wlan_p2p = "wlan-p2p.service"
SYSLOG-NG_DESTINATION_wlan_p2p = "wpa_p2p_supplicant.log"
SYSLOG-NG_LOGRATE_wlan_p2p = "medium"

RDEPENDS:${PN} += " bash"

do_install () {
    install -d ${D}${bindir}
    install -d ${D}${sbindir}
    install -d ${D}${systemd_unitdir}/system

    install -m 0755 ${S}/wlan-p2p.sh ${D}${bindir}
    install -m 0644 ${S}/wlan-p2p.service ${D}${systemd_unitdir}/system/wlan-p2p.service
    ln -sf ${sbindir}/wpa_supplicant ${D}${sbindir}/wpa_p2p_supplicant
}

SYSTEMD_SERVICE:${PN} = "wlan-p2p.service"
SYSTEMD_AUTO_ENABLE = "enable"
FILES:${PN} += "${systemd_unitdir}/system/wlan-p2p.service"
FILES:${PN} += "${sbindir}/wpa_p2p_supplicant"
