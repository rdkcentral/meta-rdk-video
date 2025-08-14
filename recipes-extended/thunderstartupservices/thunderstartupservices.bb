SUMMARY = "Thunder from systemd Initialiser Service"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${WORKDIR}/git/LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

DEPENDS = "systemd"

RDEPENDS:${PN} += " bash"

SRC_URI = "git://github.com/rdkcentral/thunder-startup-services.git;protocol=git;name=thunderstartupservices \
    ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV', 'file://0002-displaysettings-tv-deps.patch', '', d)} \
    file://deactivate_plugin.sh \
"
S = "${WORKDIR}/git/systemd/system"

THUNDER_STARTUP_SERVICES:append = "\
    wpeframework-avinput.service \
    wpeframework-bluetooth.service \
    wpeframework-cloudstore.service \
    wpeframework-cryptography.service \
    wpeframework-deviceinfo.service \
    wpeframework-displayinfo.service \
    wpeframework-displaysettings.service \
    wpeframework-frontpanel.service \
    wpeframework-hdcpprofile.service \
    wpeframework-maintenancemanager.service \
    wpeframework-monitor.service \
    wpeframework-network.service \
    wpeframework-ocdm.service \
    wpeframework-persistentstore.service \
    wpeframework-playerinfo.service \
    wpeframework-sharedstorage.service \
    wpeframework-system.service \
    wpeframework-systemaudioplayer.service \
    wpeframework-systemmode.service \
    ${@bb.utils.contains('DISTRO_FEATURES', 'rdkshell',' wpeframework-rdkshell.service', '', d)} \
    wpeframework-remotecontrol.service \
    wpeframework-telemetry.service \
    wpeframework-texttospeech.service \
    wpeframework-voicecontrol.service \
    wpeframework-wifi.service \
    wpeframework-xcast.service \
    wpeframework-analytics.service \
    wpeframework-usersettings.service \
    wpeframework-usbdevice.service \
    wpeframework-usbmassstorage.service \
    wpeframework-firmwareupdate.service \
    wpeframework-powermanager.service \
    wpeframework-networkmanager.service \
    ${@bb.utils.contains('DISTRO_FEATURES', 'DAC_SUPPORT',' wpeframework-lisa.service', '', d)} \
    wpeframework-ocicontainer.service \
    ${@bb.utils.contains('DISTRO_FEATURES', 'rdkwindowmanager',' wpeframework-rdkwindowmanager.service', '', d)} \
    wpeframework-lifecyclemanager.service \
    wpeframework-runtimemanager.service \
    wpeframework-storagemanager.service \
    wpeframework-packagemanager.service \
    wpeframework-appmanager.service \
    "

CONTROL_FILES = "\
    wpeframework-services.path \
    wpeframework-services.target \
    "

do_install() {
    install -d ${D}${systemd_system_unitdir}

    for y in ${CONTROL_FILES}; do
        install -m 0644 ${S}/${y} ${D}${systemd_system_unitdir}
        install -d ${D}${sysconfdir}/systemd/system/multi-user.target.wants
        ln -sf ${systemd_system_unitdir}/${y} ${D}${sysconfdir}/systemd/system/multi-user.target.wants/${y}
    done

    for x in ${THUNDER_STARTUP_SERVICES}; do
        install -m 0644 ${S}/${x} ${D}${systemd_system_unitdir}
        install -d ${D}${sysconfdir}/systemd/system/${x}.requires
        ln -sf ${systemd_system_unitdir}/wpeframework.service ${D}${sysconfdir}/systemd/system/${x}.requires/wpeframework.service
    done

    # Adding final THUNDER_STARTUP_SERVICES into the Requires= line of the target
    FINAL_SERVICES="$(echo "${THUNDER_STARTUP_SERVICES}" | tr '\n' ' ')"
    TARGET_FILE="${D}${systemd_system_unitdir}/wpeframework-services.target"

    if grep -q "^Requires=" "$TARGET_FILE"; then
        # Append to existing Requires= line
        sed -i "/^Requires=/ s|$| ${FINAL_SERVICES}|" "$TARGET_FILE"
    fi
}

do_install:append() {
    SERVICE="${D}${systemd_system_unitdir}/wpeframework-displaysettings.service"

    if [ -f "$SERVICE" ]; then
        # Insert the line before [Service], only if not already present
        if ! grep -q "^ConditionPathExists=/tmp/wpeframeworkstarted" "$SERVICE"; then
            sed -i '/^\[Service\]/i ConditionPathExists=/tmp/wpeframeworkstarted' "$SERVICE"
        fi
    fi

    SERVICE_DIR="${D}${systemd_system_unitdir}"

    for x in ${THUNDER_STARTUP_SERVICES}; do
        SERVICE_FILE="${SERVICE_DIR}/${x}"

        if [ -f "$SERVICE_FILE" ] && grep -q '^ExecStart=.*PluginActivator' "$SERVICE_FILE"; then
            CALLSIGN=$(sed -n -E 's/.*PluginActivator\s+([^ ]+).*/\1/p' "$SERVICE_FILE")

            if [ -n "$CALLSIGN" ]; then
                # Check if ExecStop with PluginActivator is already present
                if ! grep -q "^ExecStop=/lib/rdk/deactivate_plugin.sh $CALLSIGN" "$SERVICE_FILE"; then
                    # If ExecStop with the PluginActivator is not present, add it
                    if grep -q '^ExecStartPost=' "$SERVICE_FILE"; then
                        # If ExecStartPost exists, add ExecStop after it
                        sed -i "/^ExecStartPost=/a ExecStop=/lib/rdk/deactivate_plugin.sh $CALLSIGN" "$SERVICE_FILE"
                    else
                        # Else insert ExecStop after ExecStart
                        sed -i "/^ExecStart=.*PluginActivator/a ExecStop=/lib/rdk/deactivate_plugin.sh $CALLSIGN" "$SERVICE_FILE"
                    fi
                fi
            fi
        fi
    done

    install -d ${D}/lib/rdk
    install -m 0755 ${WORKDIR}/deactivate_plugin.sh ${D}/lib/rdk/deactivate_plugin.sh

}

FILES:${PN} += "${systemd_system_unitdir} ${sysconfdir}/systemd/system"
FILES:${PN} += "/lib/rdk/deactivate_plugin.sh"
