inherit syslog-ng-config-gen logrotate_config
SYSLOG-NG_FILTER = "telemetry2_0"
SYSLOG-NG_SERVICE_telemetry2_0 = "telemetry2_0.service"
SYSLOG-NG_DESTINATION_telemetry2_0 = "telemetry2_0.txt.0"
SYSLOG-NG_LOGRATE_telemetry2_0 = "high"

LOGROTATE_NAME="telemetry2"
LOGROTATE_LOGNAME_telemetry2="telemetry2_0.txt.0"
#HDD_ENABLE
LOGROTATE_SIZE_telemetry2="512000"
LOGROTATE_ROTATION_telemetry2="3"
#HDD_DISABLE
LOGROTATE_SIZE_MEM_telemetry2="512000"
LOGROTATE_ROTATION_MEM_telemetry2="3"

DEPENDS += " webconfig-framework  libsyswrapper "

CFLAGS += " -DENABLE_RDKV_SUPPORT -DENABLE_PS_PROCESS_SEARCH -DFEATURE_SUPPORT_WEBCONFIG -DDCMAGENT -DUSE_SERIALIZED_MANUFACTURER_NAME -DPERSIST_LOG_MON_REF -DNTP_SYNC_INDICATION"

DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec', " ", d)}"

CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec',  ' `pkg-config --cflags libsafec`', '-fPIC', d)}"
CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"
LDFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --libs libsafec`', '', d)}"

do_install:append () {

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${S}/telemetry2_0.service ${D}${systemd_unitdir}/system

    # Modify telemetry2_0.service for mTLS boot ordering
    # Per RDKEMW-15410 AC: telemetry2_0.service is hard-gated and MUST NOT start before mtls.target
    if [ -f ${D}${systemd_unitdir}/system/telemetry2_0.service ]; then
        # Add After=mtls.target to [Unit] section (no Wants= to prevent premature target activation)
        sed -i '/^\[Unit\]/a After=mtls.target' \
            ${D}${systemd_unitdir}/system/telemetry2_0.service

        # Check if [Install] section exists
        if grep -q "^\[Install\]" ${D}${systemd_unitdir}/system/telemetry2_0.service; then
            # Replace existing WantedBy
            sed -i 's/WantedBy=.*/WantedBy=mtls.target/g' \
                ${D}${systemd_unitdir}/system/telemetry2_0.service
        else
            # Add new [Install] section
            echo "" >> ${D}${systemd_unitdir}/system/telemetry2_0.service
            echo "[Install]" >> ${D}${systemd_unitdir}/system/telemetry2_0.service
            echo "WantedBy=mtls.target" >> ${D}${systemd_unitdir}/system/telemetry2_0.service
        fi
    fi
}

SYSTEMD_SERVICE:${PN} += "telemetry2_0.service"
