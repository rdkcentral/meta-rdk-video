SUMMARY = "Sysint application"
SECTION = "console/utils"

LICENSE = "Apache-2.0 & BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=f36198fb804ffbe39b5b2c336ceef9f8"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
PV = "1.0"

SRC_URI = "${CMF_GITHUB_ROOT}/sysint;${CMF_GITHUB_SRC_URI_SUFFIX};module=.;name=sysint"
S = "${WORKDIR}/git"

inherit systemd syslog-ng-config-gen logrotate_config
SYSLOG-NG_FILTER = " systemd dropbear gstreamer-cleanup update-device-details applications vitalprocess-info iptables mount_log reboot-reason messages zram"
SYSLOG-NG_FILTER:append = " ConnectionStats systemd_timesyncd"
SYSLOG-NG_SERVICE_ConnectionStats = "network-connection-stats.service"
SYSLOG-NG_DESTINATION_ConnectionStats = "ConnectionStats.txt"
SYSLOG-NG_LOGRATE_ConnectionStats = "low"
SYSLOG-NG_SERVICE_systemd_timesyncd = "systemd-timesyncd.service"
SYSLOG-NG_DESTINATION_systemd_timesyncd = "ntp.log"
SYSLOG-NG_LOGRATE_systemd_timesyncd = "medium"
SYSLOG-NG_SERVICE_dropbear = "dropbear.service"
SYSLOG-NG_DESTINATION_dropbear = "dropbear.log"
SYSLOG-NG_LOGRATE_dropbear = "medium"
SYSLOG-NG_SERVICE_systemd = "init.scope"
SYSLOG-NG_DESTINATION_systemd = "system.log"
SYSLOG-NG_LOGRATE_systemd = "high"
SYSLOG-NG_SERVICE_gstreamer-cleanup = "gstreamer-cleanup.service"
SYSLOG-NG_DESTINATION_gstreamer-cleanup = "gst-cleanup.log"
SYSLOG-NG_LOGRATE_gstreamer-cleanup = "low"
SYSLOG-NG_SERVICE_update-device-details = "update-device-details.service"
SYSLOG-NG_DESTINATION_update-device-details = "device_details.log"
SYSLOG-NG_LOGRATE_update-device-details = "low"
SYSLOG-NG_SERVICE_iptables = "iptables.service"
SYSLOG-NG_DESTINATION_iptables = "iptables.log"
SYSLOG-NG_LOGRATE_iptables = "low"
SYSLOG-NG_SERVICE_zram = " zram.service "
SYSLOG-NG_DESTINATION_zram = "zram.log"
SYSLOG-NG_LOGRATE_zram = "low"
SYSLOG-NG_SERVICE_vitalprocess-info = "vitalprocess-info.service"
SYSLOG-NG_DESTINATION_vitalprocess-info = "top_log.txt"
SYSLOG-NG_LOGRATE_vitalprocess-info = "high"
SYSLOG-NG_SERVICE_mount_log:append:rdkstb = " disk-check.service "
SYSLOG-NG_DESTINATION_mount_log = "mount_log.txt"
SYSLOG-NG_LOGRATE_mount_log = "low"
SYSLOG-NG_SERVICE_reboot-reason = "reboot-reason-logger.service update-reboot-info.service"
SYSLOG-NG_DESTINATION_reboot-reason = "rebootreason.log"
SYSLOG-NG_LOGRATE_reboot-reason = "low"
SYSLOG-NG_FILTER += "messages"
SYSLOG-NG_DESTINATION_messages = "messages.txt"
SYSLOG-NG_LOGRATE_messages = "low"

# Get kernel logs via journal
SYSLOG-NG_PROGRAM_messages += " kernel"

do_compile[noexec] = "1"
CLEANBROKEN = "1"

DEPENDS += "crashupload"
RDEPENDS:${PN} += "bash"
RDEPENDS:${PN} += "busybox"

RF3CE_CTRLM_ENABLED = "${@bb.utils.contains('DISTRO_FEATURES', 'ctrlm', 'true', 'false', d)}"
STG_TYPE = "${@bb.utils.contains('DISTRO_FEATURES', 'storage_sdc','SDCARD', 'OTHERS',d)}"
MMC_TYPE = "${@bb.utils.contains('DISTRO_FEATURES', 'storage_emmc','EMMC', '',d)}"
BIND_ENABLED = "${@bb.utils.contains('DISTRO_FEATURES', 'bind', 'true', 'false', d)}"
FORCE_MTLS = "${@bb.utils.contains('DISTRO_FEATURES', 'mtls_only', 'true', 'false', d)}"
ENABLE_MAINTENANCE="${@bb.utils.contains('DISTRO_FEATURES', 'enable_maintenance_manager', 'true', 'false', d)}"
WIFI_ENABLED="${@bb.utils.contains('DISTRO_FEATURES', 'wifi', 'true', 'false', d)}"
ENABLE_SOFTWARE_OPTOUT="${@bb.utils.contains('DISTRO_FEATURES', 'enable_software_optout', 'true', 'false', d)}"
ENABLE_SYSLOGNG = "${@bb.utils.contains('DISTRO_FEATURES', 'syslog-ng', 'true', 'false', d)}"
DUNFELL_BUILD = "${@bb.utils.contains('DISTRO_FEATURES', 'dunfell', 'true', 'false', d)}"

do_install() {
	install -d ${D}${base_libdir}/rdk
	install -m 0755 ${S}/lib/rdk/* ${D}${base_libdir}/rdk

	install -d ${D}${sysconfdir}
        install -d ${D}${sysconfdir}/rfcdefaults
	install -m 0644 ${S}/etc/*.properties ${D}${sysconfdir}
	install -m 0644 ${S}/etc/*.conf ${D}${sysconfdir}
	install -m 0644 ${S}/etc/env_setup.sh ${D}${sysconfdir}
        install -m 0755 ${S}/etc/rfcdefaults/sysint-generic.ini ${D}${sysconfdir}/rfcdefaults/sysint-generic.ini

	install -d ${D}${base_bindir} ${D}/var/spool/cron
        install -d ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/log-rdk-start.service ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/previous-log-backup.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/vitalprocess-info.timer ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/vitalprocess-info.service ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/logrotate.service ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/logrotate.timer ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/dump-backup.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/coredump-upload.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/coredump-secure-upload.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/coredump-upload.path ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/coredump-secure-upload.path ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/minidump-upload.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/minidump-secure-upload.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/minidump-upload.path ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/minidump-secure-upload.path ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/disk-threshold-check.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/disk-threshold-check.timer ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/reboot-reason-logger.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/iptables.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/update-device-details.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/update-reboot-info.path ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/update-reboot-info.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/restart-parodus.path ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/restart-parodus.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/gstreamer-cleanup.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/minidump-upload.path ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/minidump-secure-upload.path ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/disk-threshold-check.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/disk-threshold-check.timer ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/reboot-reason-logger.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/iptables.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/update-device-details.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/update-reboot-info.path ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/update-reboot-info.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/restart-parodus.path ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/restart-parodus.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/gstreamer-cleanup.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/oops-dump.service ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/restart-timesyncd.service ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/restart-timesyncd.path ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/ntp-event.service ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/ntp-event.path ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/dropbear.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/network-connection-stats.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/network-connection-stats.timer ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/NM_Bootstrap.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/zram.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/systemd_units/notify-network-ready.service ${D}${systemd_unitdir}/system


	install -m 0644 ${S}/systemd_units/network-up.path ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/network-up.target ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/network-up.timer ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/ntp-time-sync.path ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/ntp-time-sync.target ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/ntp-time-sync-event.service ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/ntp-time-sync.timer ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/system-time-set.path ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/system-time-set.target ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/systemd_units/system-time-event.service ${D}${systemd_unitdir}/system

        if [ "${BIND_ENABLED}" = "true" ]; then
           echo "BIND_ENABLED=true" >> ${D}${sysconfdir}/device-middleware.properties
        fi

        if [ "${ENABLE_SYSLOGNG}" = "true" ]; then
           echo "SYSLOG_NG_ENABLED=true" >> ${D}${sysconfdir}/device-middleware.properties
           install -D -m 0644 ${S}/systemd_units/after_syslog-ng.conf ${D}${systemd_unitdir}/system/reboot-reason-logger.service.d/reboot-reason-logger.conf
        fi

        if [ "${MMC_TYPE}" = "EMMC" ]; then
           echo "SD_CARD_TYPE=EMMC" >> ${D}${sysconfdir}/device-middleware.properties
        fi

        if [ "${FORCE_MTLS}" = "true" ]; then
           echo "FORCE_MTLS=true" >> ${D}${sysconfdir}/device-middleware.properties
        fi
        # Deleting firmware download script because we ported to c code and component name rdkfwupdater
        rm -f ${D}${base_libdir}/rdk/deviceInitiatedFWDnld.sh
        rm -f ${D}${base_libdir}/rdk/swupdate_utility.sh

	# uploadDumps.sh has to be taken from a different source
	# lighttpd_utility.sh is not required
	rm -rf ${D}${base_libdir}/rdk/uploadDumps.sh
	rm -rf ${D}${base_libdir}/rdk/lighttpd_utility.sh
        rm -rf ${D}${base_libdir}/rdk/runPod.sh
        rm -rf ${D}${base_libdir}/rdk/runSnmp.sh
        rm -rf ${D}${base_libdir}/rdk/runRMFStreamer
        rm -rf ${D}${base_libdir}/rdk/runVodClientApp

	#
        # removing unused files for comcast component
        rm -rf ${D}${base_libdir}/rdk/adddefaultgateway.sh
        rm -rf ${D}${base_libdir}/rdk/pNexus.sh
        rm -rf ${D}${base_libdir}/rdk/stackCalls.sh
        rm -rf ${D}${base_libdir}/rdk/watchdog-starter
	#
	# The below scripts are installed by xre for emulator so need to
	# delete from sysint generic repo. For now, we will prevent these
	# to be installed by sysint.
	#
	ln -sf /lib/rdk/rebootSTB.sh ${D}/
	ln -sf /lib/rdk/rebootNow.sh ${D}/
	ln -sf /lib/rdk/timestamp ${D}${base_bindir}/timestamp

        # Samhain can only invoke external utilities present in trusted FHS path
        if [ -f ${S}/../lib/rdk/upload2splunk.sh ]; then
            install -d ${D}${sbindir}
            install -m 0755 ${S}/../lib/rdk/upload2splunk.sh ${D}${sbindir}
        fi
	if [ -f ${D}/${base_libdir}/rdk/upload2splunk.sh ]; then
	    rm -f ${D}${base_libdir}/rdk/upload2splunk.sh
	fi

        # zcip.script is installed in both /lib/rdk and /etc. Removing /lib/rdk/zcip.script to avoid duplicates
        # Try to use zcip.script from /etc if required
        rm -rf ${D}${base_libdir}/rdk/zcip.script



        if [ "${MMC_TYPE}" != "EMMC" ]; then
            rm -f ${D}${base_libdir}/rdk/emmc_format.sh
        fi
        if [ "${ENABLE_MAINTENANCE}" = "true" ]; then
           echo "ENABLE_MAINTENANCE=true" >> ${D}${sysconfdir}/device-middleware.properties
           # Software optout works only with Maintenance Manager
           if [ "${ENABLE_SOFTWARE_OPTOUT}" = "true" ]; then
               echo "ENABLE_SOFTWARE_OPTOUT=true" >> ${D}${sysconfdir}/device-middleware.properties
           fi
        fi

        # RDK-43346 - to clean-up HDD Status Logging script
        rm -rf ${D}${base_libdir}/rdk/hddStatusLogger.sh

        # RDK-43347 - to clean-up Telemetry & DCM related Scripts for RDKE OS
        rm -rf ${D}${base_libdir}/rdk/updateMountFailureCount.sh
        rm -rf ${D}${base_libdir}/rdk/reportFailuresToSplunk.sh

        if [ -f "${D}${base_libdir}/rdk/getDeviceId.sh" ]; then
           if [ -n "${BUILTIN_PARTNER_ID}" ]; then
               sed -i "s|defaultPartnerId=\"comcast\"|defaultPartnerId=\"${BUILTIN_PARTNER_ID}\"|g" "${D}${base_libdir}/rdk/getDeviceId.sh"
           fi
        fi

        ## The systemd-timesyncd version 244 and above doesn't have access to /tmp directory as it is launched by 
        ## an unprivileged user(systemd-timesync). So trigger ntp-event when /run/systemd/timesync/synchronized flag is created instead of /tmp/clock-event.
        if [ "${DUNFELL_BUILD}" = "true" ]; then
            sed -i -e 's|.*PathExists=.*|PathExists=/run/systemd/timesync/synchronized|g' ${D}${systemd_unitdir}/system/ntp-event.path
        fi

        if [ -f ${D}${base_libdir}/rdk/iptables_init_xi ]; then
            mv ${D}${base_libdir}/rdk/iptables_init_xi ${D}${base_libdir}/rdk/iptables_init
        fi

        # RDK-43338 - to clean-up sysint for RDKE OS - RF Status, HTML Diagnostics
        rm -rf ${D}${base_libdir}/rdk/htmlDiagPreSetup.sh
        rm -rf ${D}${base_libdir}/rdk/rfStatisticsCheck.sh

	# For NetworkManager
	install -d ${D}${sysconfdir}/NetworkManager
	install -d ${D}${sysconfdir}/NetworkManager/conf.d
	install -d ${D}${sysconfdir}/NetworkManager/dispatcher.d
	install -d ${D}${sysconfdir}/NetworkManager/dispatcher.d/pre-down.d
	install -m 0755 ${S}/lib/rdk/NM_Dispatcher.sh ${D}${sysconfdir}/NetworkManager/dispatcher.d
	install -m 0755 ${S}/lib/rdk/NM_preDown.sh ${D}${sysconfdir}/NetworkManager/dispatcher.d/pre-down.d
	install -m 0755 ${S}/etc/10-unmanaged-devices ${D}${sysconfdir}/NetworkManager/conf.d/10-unmanaged-devices.conf
        rm ${D}${base_libdir}/rdk/NM_Dispatcher.sh
        rm ${D}${base_libdir}/rdk/NM_preDown.sh
    install -d ${D}${systemd_unitdir}/system/NetworkManager.service.d
    install -m 0755 ${S}/systemd_units/NetworkManager_ecfs.conf ${D}${systemd_unitdir}/system/NetworkManager.service.d
}

do_install:append:rdkstb() {
        install -m 0755 ${S}/lib/rdk/heap-usage-stats.sh ${D}/lib/rdk/heap-usage-stats.sh
        install -m 0644 ${S}/systemd_units/usbmodule-whitelist.service ${D}${systemd_unitdir}/system
        install -m 0755 ${S}/lib/rdk/usbmodule-whitelist.sh ${D}${base_libdir}/rdk/
        install -m 0644 ${S}/systemd_units/disk-check-sdcard.service ${D}${systemd_unitdir}/system/disk-check.service
}

SYSTEMD_SERVICE:${PN}:append:rdkstb = " usbmodule-whitelist.service"
SYSTEMD_SERVICE:${PN} += "log-rdk-start.service"
SYSTEMD_SERVICE:${PN} += "previous-log-backup.service"
SYSTEMD_SERVICE:${PN} += "vitalprocess-info.timer"
SYSTEMD_SERVICE:${PN} += "logrotate.timer"
SYSTEMD_SERVICE:${PN} += "dump-backup.service"
SYSTEMD_SERVICE:${PN}:append:rdkstb = " disk-check.service "
SYSTEMD_SERVICE:${PN} += "coredump-upload.service"
SYSTEMD_SERVICE:${PN} += "coredump-secure-upload.service"
SYSTEMD_SERVICE:${PN} += "coredump-upload.path"
SYSTEMD_SERVICE:${PN} += "coredump-secure-upload.path"
SYSTEMD_SERVICE:${PN} += "minidump-upload.service"
SYSTEMD_SERVICE:${PN} += "minidump-secure-upload.service"
SYSTEMD_SERVICE:${PN} += "minidump-upload.path"
SYSTEMD_SERVICE:${PN} += "minidump-secure-upload.path"
SYSTEMD_SERVICE:${PN} += "dropbear.service"
SYSTEMD_SERVICE:${PN} += "disk-threshold-check.timer"
SYSTEMD_SERVICE:${PN} += "reboot-reason-logger.service"
SYSTEMD_SERVICE:${PN} += "iptables.service"
SYSTEMD_SERVICE:${PN} += "update-device-details.service"
SYSTEMD_SERVICE:${PN} += "oops-dump.service"
SYSTEMD_SERVICE:${PN} += "update-reboot-info.path"
SYSTEMD_SERVICE:${PN} += "update-reboot-info.service"
SYSTEMD_SERVICE:${PN} += "restart-parodus.path"
SYSTEMD_SERVICE:${PN} += "restart-parodus.service"
SYSTEMD_SERVICE:${PN} += "gstreamer-cleanup.service"
SYSTEMD_SERVICE:${PN} += "restart-timesyncd.path"
SYSTEMD_SERVICE:${PN} += "ntp-event.service"
SYSTEMD_SERVICE:${PN} += "ntp-event.path"
SYSTEMD_SERVICE:${PN} += "network-connection-stats.service"
SYSTEMD_SERVICE:${PN} += "network-connection-stats.timer"
SYSTEMD_SERVICE:${PN} += "NM_Bootstrap.service"
SYSTEMD_SERVICE:${PN} += "zram.service"
SYSTEMD_SERVICE:${PN} += "network-up.path"
SYSTEMD_SERVICE:${PN} += "network-up.timer"
SYSTEMD_SERVICE:${PN} += "ntp-time-sync.path"
SYSTEMD_SERVICE:${PN} += "ntp-time-sync-event.service"
SYSTEMD_SERVICE:${PN} += "ntp-time-sync.timer"
SYSTEMD_SERVICE:${PN} += "system-time-set.path"
SYSTEMD_SERVICE:${PN} += "system-time-event.service"
SYSTEMD_SERVICE:${PN} += "notify-network-ready.service"
FILES:${PN} += "${bindir}/*"
FILES:${PN} += "${systemd_unitdir}/system/*"
FILES:${PN} += "${base_libdir}/rdk/*"
FILES:${PN} += "${sysconfdir}/rdk/*"
FILES:${PN} += "/rebootSTB.sh"
FILES:${PN} += "/rebootNow.sh"
FILES:${PN} += "${sysconfdir}/*"
FILES:${PN} += "${base_bindir}/timestamp"
FILES:${PN} += "${sbindir}/*"
FILES:${PN} += " /HrvInitScripts/*"
FILES:${PN} += "${sysconfdir}/NetworkManager/dispatcher.d/NM_Dispatcher.sh"
FILES:${PN} += "${sysconfdir}/NetworkManager/dispatcher.d/pre-down.d/NM_preDown.sh"

SRCREV_sysint = "ed3a2c982d09184c24daf76b973b3e46b1f48dd2"

