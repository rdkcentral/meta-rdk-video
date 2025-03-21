inherit syslog-ng-config-gen systemd logrotate_config

SYSLOG-NG_FILTER = "webconfig"
SYSLOG-NG_SERVICE_webconfig = "webconfig.service"
SYSLOG-NG_DESTINATION_webconfig = "webconfig.log"
SYSLOG-NG_LOGRATE_webconfig = "high"

LOGROTATE_NAME = "webconfig"
LOGROTATE_LOGNAME_webconfig = "webconfig.log"
LOGROTATE_SIZE_webconfig = "512000"
LOGROTATE_ROTATION_webconfig = "2"
LOGROTATE_SIZE_MEM_webconfig = "512000"
LOGROTATE_ROTATION_MEM_webconfig = "2"

CFLAGS:append = " -DRDK_PERSISTENT_PATH_VIDEO"
CFLAGS:append = " -DRDK_USE_DEFAULT_INTERFACE"

do_install:append:hybrid() {
    if ${@bb.utils.contains("DISTRO_FEATURES", "webconfig_bin", "true", "false", d)}
    then
      install -d ${D}/etc
      install -d ${D}${systemd_unitdir}/system
      install -m 0644 ${WORKDIR}/webconfig.service ${D}${systemd_unitdir}/system
      (${PYTHON} ${WORKDIR}/metadata_parser.py ${WORKDIR}/webconfig_video_metadata.json ${D}/etc/webconfig.properties ${MACHINE})
    fi
}

do_install:append:client() {
    if ${@bb.utils.contains("DISTRO_FEATURES", "webconfig_bin", "true", "false", d)}
    then
      install -d ${D}/etc
      install -d ${D}${systemd_unitdir}/system
      install -m 0644 ${WORKDIR}/webconfig.service ${D}${systemd_unitdir}/system
      (${PYTHON} ${WORKDIR}/metadata_parser.py ${WORKDIR}/webconfig_video_metadata.json ${D}/etc/webconfig.properties ${MACHINE})
    fi
}

FILES:${PN} += "/etc/webconfig.properties"
FILES:${PN} += "${systemd_unitdir}/system/webconfig.service"
SYSTEMD_SERVICE:${PN} = "webconfig.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

