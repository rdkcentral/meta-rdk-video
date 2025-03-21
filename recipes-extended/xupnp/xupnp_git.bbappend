DEPENDS:append = " iarmbus iarmmgrs systemd "

EXTRA_OECONF:append = " --enable-iarmbus --enable-rfc "
EXTRA_OECONF:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'xcal_device', '--enable-video-support', '', d)}"

CXXFLAGS += "-DENABLE_SD_NOTIFY"
CFLAGS += "-DENABLE_SD_NOTIFY"

LDFLAGS += "-lsystemd"

RDEPENDS:${PN} = "iarm-event-sender"

inherit syslog-ng-config-gen logrotate_config
SYSLOG-NG_FILTER = "xupnp xcal-device"
SYSLOG-NG_SERVICE_xupnp = "xupnp.service"
SYSLOG-NG_DESTINATION_xupnp = "xdiscovery.log"
SYSLOG-NG_LOGRATE_xupnp = "medium"
SYSLOG-NG_SERVICE_xcal-device = "xcal-device.service"
SYSLOG-NG_DESTINATION_xcal-device = "xdevice.log"
SYSLOG-NG_LOGRATE_xcal-device = "medium"

LOGROTATE_NAME="xdevice xdiscovery"
LOGROTATE_LOGNAME_xdevice="xdevice.log"
LOGROTATE_LOGNAME_xdiscovery="xdiscovery.log"
#HDD_ENABLE
LOGROTATE_SIZE_xdevice="512000"
LOGROTATE_ROTATION_xdevice="3"
LOGROTATE_SIZE_xdiscovery="1572864"
LOGROTATE_ROTATION_xdiscovery="3"
#HDD_DISABLE
LOGROTATE_SIZE_MEM_xdevice="512000"
LOGROTATE_ROTATION_MEM_xdevice="3"
LOGROTATE_SIZE_MEM_xdiscovery="1572864"
LOGROTATE_ROTATION_MEM_xdiscovery="3"

do_install:append() {
        install -d ${D}${systemd_unitdir}/system ${D}${sysconfdir} ${D}${sysconfdir}/Xupnp
        install -m 0644 ${S}/conf/systemd/xupnp.service ${D}${systemd_unitdir}/system/xupnp.service
	install -m 0644 ${S}/conf/systemd/xupnp-certs.service ${D}${systemd_unitdir}/system/xupnp-certs.service
        install -m 0644 ${S}/conf/systemd/xdiscovery.conf ${D}${sysconfdir}
        install -m 0644 ${S}/conf/systemd/xdevice.conf ${D}${sysconfdir}
        install -m 0500 ${S}/conf/addRouteToMocaBridge.sh ${D}${sysconfdir}/Xupnp/addRouteToMocaBridge.sh
        install -d ${D}${sysconfdir} ${D}${sysconfdir}/rfcdefaults
        install -m 0644 ${S}/conf/xupnp.ini ${D}${sysconfdir}/rfcdefaults
}

do_install:append:hybrid() {
        install -m 0644 ${S}/conf/systemd/xcal-device.service ${D}${systemd_unitdir}/system/xcal-device.service
        install -m 0644 ${S}/conf/systemd/xupnp-firewall.service ${D}${systemd_unitdir}/system/xupnp-firewall.service
        install -m 0644 ${S}/conf/systemd/xcal-device.path ${D}${systemd_unitdir}/system/xcal-device.path
}

do_install:append:client() {
    install -m 0644 ${S}/conf/systemd/xcal-device-client.service ${D}${systemd_unitdir}/system/xcal-device.service

    install -m 0644 ${S}/conf/systemd/xupnp-rfc.service ${D}${systemd_unitdir}/system/xupnp-rfc.service
    install -D -m 0644 ${S}/conf/systemd/xupnp-rfc-check.conf  ${D}${systemd_unitdir}/system/xupnp.service.d/xupnp-rfc-check.conf
    install -D -m 0644 ${S}/conf/systemd/xupnp-rfc-check.conf  ${D}${systemd_unitdir}/system/xcal-device.service.d/xupnp-rfc-check.conf
}

CFLAGS:append:client = " -DUSE_XUPNP_TZ_UPDATE "

SYSTEMD_SERVICE:${PN} = "xupnp.service"
SYSTEMD_SERVICE:${PN}:append = " xcal-device.service"
SYSTEMD_SERVICE:${PN}:append = " xupnp-certs.service"
SYSTEMD_SERVICE:${PN}:append:hybrid = " xcal-device.path"
SYSTEMD_SERVICE:${PN}:append:hybrid = " xupnp-firewall.service"
FILES:${PN} += "${systemd_unitdir}/system/xupnp.service"
FILES:${PN}:append = " ${systemd_unitdir}/system/xcal-device.service"
FILES:${PN}:append = " ${systemd_unitdir}/system/xupnp-certs.service"
FILES:${PN}:append = " ${sysconfdir}/Xupnp/addRouteToMocaBridge.sh"
FILES:${PN}:append:hybrid = " ${systemd_unitdir}/system/xcal-device.path"
FILES:${PN}:append:hybrid = " ${systemd_unitdir}/system/xupnp-firewall.service"

#XUPNP RFC Check
SYSTEMD_SERVICE:${PN}:append:client = " xupnp-rfc.service"
FILES:${PN}:append:client = " ${systemd_unitdir}/system/xupnp-rfc.service"
FILES:${PN}:append:client = " ${systemd_unitdir}/system/xcal-device.service.d/xupnp-rfc-check.conf"
FILES:${PN}:append:client = " ${systemd_unitdir}/system/xupnp.service.d/xupnp-rfc-check.conf"
