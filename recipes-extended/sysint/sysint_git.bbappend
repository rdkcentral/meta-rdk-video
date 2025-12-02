SYSTEMD_VERSION = "${PREFERRED_VERSION_systemd}"
DUNFELL_BUILD = "${@bb.utils.contains('DISTRO_FEATURES', 'dunfell', 'true', 'false', d)}"
DOBBY_ENABLED = "${@bb.utils.contains('DISTRO_FEATURES', 'DOBBY_CONTAINERS','true', 'false',d)}"

SRC_URI += "file://udhcpc.vendor_specific"
SRC_URI += "file://timeZone_offset_map"
SRC_URI += "file://nm-connectivity.conf"

inherit logrotate_config 

LOGROTATE_NAME = "rtk_fw"
LOGROTATE_LOGNAME_rtk_fw="rtk_fw.log"
LOGROTATE_SIZE_rtk_fw="1572864"
LOGROTATE_ROTATION_rtk_fw="3"
LOGROTATE_SIZE_MEM_rtk_fw="1572864"
LOGROTATE_ROTATION_MEM_rtk_fw="3"

LOGROTATE_NAME = "pqstats"
LOGROTATE_LOGNAME_pqstats="pqstats.log"
LOGROTATE_SIZE_pqstats="1572864"
LOGROTATE_ROTATION_pqstats="3"
LOGROTATE_SIZE_MEM_pqstats="1572864"
LOGROTATE_ROTATION_MEM_pqstats="3"

NW_CONNECTIVITY_CHECK_URL ?= "https://nmcheck.gnome.org/check_network_status.txt"
NW_CONNECTIVITY_CHECK_RESPONSE ?= "NetworkManager is online"

do_install:append() {
    install -m 0755 ${WORKDIR}/udhcpc.vendor_specific ${D}${sysconfdir}/udhcpc.vendor_specific
    install -m 0644 ${WORKDIR}/timeZone_offset_map ${D}${sysconfdir}/timeZone_offset_map

    if [ "${DOBBY_ENABLED}" = "true" ]; then
        echo "DOBBY_ENABLED=true" >> ${D}${sysconfdir}/device-middleware.properties
    fi

    ## The systemd-timesyncd version 244(dunfell) and above creates "/run/systemd/timesync/synchronized" and the versions below 244
    ## creates "/tmp/clock-event" flag when system time is synced. Update ntp-event path unit accordingly
    if [ "${DUNFELL_BUILD}" = "true" ] && [ "${SYSTEMD_VERSION}" = "1:230%" ]; then
       sed -i -e 's|.*PathExists=.*|PathExists=/tmp/clock-event|g' ${D}${systemd_unitdir}/system/ntp-event.path
    fi
    
    if ${@bb.utils.contains_any('DISTRO_FEATURES', 'prodlog-variant prod-variant', 'true', 'false', d)}; then
       sed -i 's/BUILD_TYPE=dev/BUILD_TYPE=prod/g' ${D}${sysconfdir}/device.properties
    fi

    install -d ${D}${sysconfdir}/NetworkManager/conf.d/
    install -m 0755 ${WORKDIR}/nm-connectivity.conf ${D}${sysconfdir}/NetworkManager/conf.d/nm-connectivity.conf
    if [ -f "${D}${sysconfdir}/NetworkManager/conf.d/nm-connectivity.conf" ]; then
           if [ -n "${NW_CONNECTIVITY_CHECK_URL}" ]; then
               sed -i "s|uri=.*|uri=${NW_CONNECTIVITY_CHECK_URL}|g" ${D}${sysconfdir}/NetworkManager/conf.d/nm-connectivity.conf
           fi
           if [ -n "${NW_CONNECTIVITY_CHECK_RESPONSE}" ]; then
               sed -i "s|response=|response=${NW_CONNECTIVITY_CHECK_RESPONSE}|g" ${D}${sysconfdir}/NetworkManager/conf.d/nm-connectivity.conf
           fi
    fi
}

FILES:${PN} += "${sysconfdir}/udhcpc.vendor_specific"
FILES:${PN} += "${sysconfdir}/timeZone_offset_map"

LOGROTATE_NAME = "applications"
LOGROTATE_LOGNAME_applications = "applications.log"
LOGROTATE_SIZE_MEM_applications = "512000"
LOGROTATE_ROTATION_MEM_applications = "2"
LOGROTATE_SIZE_applications = "20971520"
LOGROTATE_ROTATION_applications = "5"

LOGROTATE_NAME = "reboot-reason"
LOGROTATE_LOGNAME_reboot-reason = "rebootreason.log"
LOGROTATE_SIZE_MEM_reboot-reason = "1572864"
LOGROTATE_ROTATION_MEM_reboot-reason = "3"
LOGROTATE_SIZE_reboot-reason = "1572864"
LOGROTATE_ROTATION_reboot-reason = "3"

LOGROTATE_NAME = "system"
LOGROTATE_LOGNAME_system = "system.log"
LOGROTATE_SIZE_MEM_system = "512800"
LOGROTATE_ROTATION_MEM_system = "1"
LOGROTATE_SIZE_system = "10485760"
LOGROTATE_ROTATION_system = "5"

LOGROTATE_NAME = "iptables"
LOGROTATE_LOGNAME_iptables = "iptables.log"
LOGROTATE_SIZE_iptables = "1572864"
LOGROTATE_ROTATION_iptables = "3"
LOGROTATE_SIZE_MEM_iptables = "1572864"
LOGROTATE_ROTATION_MEM_iptables = "3"

LOGROTATE_NAME = "mount_log"
LOGROTATE_LOGNAME_mount_log = "mount_log.txt"
LOGROTATE_SIZE_MEM_mount_log = "204800"
LOGROTATE_ROTATION_MEM_mount_log = "1"
LOGROTATE_SIZE_mount_log = "10485760"
LOGROTATE_ROTATION_mount_log = "5"

LOGROTATE_NAME = "messages"
LOGROTATE_LOGNAME_messages = "messages.txt"
LOGROTATE_SIZE_MEM_messages = "409600"
LOGROTATE_ROTATION_MEM_messages = "2"
LOGROTATE_SIZE_messages = "20971520"
LOGROTATE_ROTATION_messages = "5"

LOGROTATE_NAME = "upstream_stats"
LOGROTATE_LOGNAME_upstream_stats = "upstream_stats.log"
LOGROTATE_SIZE_upstream_stats = "1572864"
LOGROTATE_ROTATION_upstream_stats = "3"
LOGROTATE_SIZE_MEM_upstream_stats = "1572864"
LOGROTATE_ROTATION_MEM_upstream_stats = "3"

LOGROTATE_NAME = "discoverV4Client"
LOGROTATE_LOGNAME_discoverV4Client = "discoverV4Client.log"
LOGROTATE_SIZE_discoverV4Client = "1572864"
LOGROTATE_ROTATION_discoverV4Client = "3"
LOGROTATE_SIZE_MEM_discoverV4Client = "1572864"
LOGROTATE_ROTATION_MEM_discoverV4Client = "3"

LOGROTATE_NAME = "ConnectionStats"
LOGROTATE_LOGNAME_ConnectionStats = "ConnectionStats.txt"
LOGROTATE_SIZE_ConnectionStats = "1572864"
LOGROTATE_ROTATION_ConnectionStats = "3"
LOGROTATE_SIZE_MEM_ConnectionStats = "1572864"
LOGROTATE_ROTATION_MEM_ConnectionStats = "3"

LOGROTATE_NAME = "syslog_fallback"
LOGROTATE_LOGNAME_syslog_fallback="syslog_fallback.log"
LOGROTATE_SIZE_syslog_fallback="1572864"
LOGROTATE_ROTATION_syslog_fallback="3"
LOGROTATE_SIZE_MEM_syslog_fallback="1572864"
LOGROTATE_ROTATION_MEM_syslog_fallback="3"

LOGROTATE_NAME = "xre"
LOGROTATE_LOGNAME_xre="xre*.log"
LOGROTATE_SIZE_xre="51200"
LOGROTATE_ROTATION_xre="3"
LOGROTATE_SIZE_MEM_xre="51200"
LOGROTATE_ROTATION_MEM_xre="3"

LOGROTATE_NAME = "hdcp"
LOGROTATE_LOGNAME_hdcp="hdcp.log"
LOGROTATE_SIZE_hdcp="1572864"
LOGROTATE_ROTATION_hdcp="3"
LOGROTATE_SIZE_MEM_hdcp="1572864"
LOGROTATE_ROTATION_MEM_hdcp="3"

LOGROTATE_NAME = "rdk_milestones"
LOGROTATE_LOGNAME_rdk_milestones="rdk_milestones.log"
LOGROTATE_SIZE_rdk_milestones="1572864"
LOGROTATE_ROTATION_rdk_milestones="3"
LOGROTATE_SIZE_MEM_rdk_milestones="1572864"
LOGROTATE_ROTATION_MEM_rdk_milestones="3"

LOGROTATE_NAME = "ping-telemetry"
LOGROTATE_LOGNAME_ping-telemetry="ping_telemetry.log"
LOGROTATE_SIZE_ping-telemetry="1572864"
LOGROTATE_ROTATION_ping-telemetry="3"
LOGROTATE_SIZE_MEM_ping-telemetry="1572864"
LOGROTATE_ROTATION_MEM_ping-telemetry="3"

LOGROTATE_NAME = "webpa_log"
LOGROTATE_LOGNAME_webpa_log = "webpa_log.txt"
LOGROTATE_SIZE_webpa_log = "128000"
LOGROTATE_ROTATION_webpa_log = "3"
LOGROTATE_SIZE_MEM_webpa_log = "128000"
LOGROTATE_ROTATION_MEM_webpa_log = "3"

LOGROTATE_NAME = "rebootInfo"
LOGROTATE_LOGNAME_rebootInfo = "rebootInfo.log"
LOGROTATE_SIZE_rebootInfo = "64000"
LOGROTATE_ROTATION_rebootInfo = "3"
LOGROTATE_SIZE_MEM_rebootInfo = "64000"
LOGROTATE_ROTATION_MEM_rebootInfo = "3"

LOGROTATE_NAME = "core_log"
LOGROTATE_LOGNAME_core_log = "core_log.txt"
LOGROTATE_SIZE_core_log = "128000"
LOGROTATE_ROTATION_core_log = "3"
LOGROTATE_SIZE_MEM_core_log = "128000"
LOGROTATE_ROTATION_MEM_core_log = "3"

LOGROTATE_NAME = "boot"
LOGROTATE_LOGNAME_boot = "bootlog"
LOGROTATE_SIZE_boot = "1572864"
LOGROTATE_ROTATION_boot = "3"
LOGROTATE_SIZE_MEM_boot = "1572864"
LOGROTATE_ROTATION_MEM_boot = "3"

LOGROTATE_NAME = "rfcscript"
LOGROTATE_LOGNAME_rfcscript="rfcscript.log"
LOGROTATE_SIZE_rfcscript="64000"
LOGROTATE_ROTATION_rfcscript="3"
LOGROTATE_SIZE_MEM_rfcscript="64000"
LOGROTATE_ROTATION_MEM_rfcscript="3"

LOGROTATE_NAME = "dcm"
LOGROTATE_LOGNAME_dcm="dcmscript.log"
LOGROTATE_SIZE_dcm="1572864"
LOGROTATE_ROTATION_dcm="1"
LOGROTATE_SIZE_MEM_dcm="512000"
LOGROTATE_ROTATION_MEM_dcm="1"

FILES:${PN} += "${sysconfdir}/NetworkManager/conf.d/nm-connectivity.conf"
