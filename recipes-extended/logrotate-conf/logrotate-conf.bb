
SUMMARY = "Logrotate configuration and systemd units"
DESCRIPTION = "Provides logrotate configuration and systemd service/timer"
LICENSE = "CLOSED"

SRC_URI = "file://logrotate.service \
           file://logrotate.timer \
"

inherit systemd

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "logrotate.service logrotate.timer"

do_install() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/logrotate.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/logrotate.timer ${D}${systemd_unitdir}/system

}

FILES:${PN} += "${D}${systemd_unitdir}/system"

