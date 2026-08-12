THUNDER_53_SIDECAR_LAB ?= "1"

do_install:append() {
    if [ "${THUNDER_53_SIDECAR_LAB}" = "1" ]; then
        SERVICE_DIR="${D}${systemd_system_unitdir}"
        TARGET_FILE="${SERVICE_DIR}/wpeframework-services.target"

        for service in \
            wpeframework-remotecontrol.service \
            wpeframework-voicecontrol.service; do
            rm -f "${SERVICE_DIR}/${service}"
            rm -rf "${D}${sysconfdir}/systemd/system/${service}.requires"

            if [ -f "${TARGET_FILE}" ]; then
                sed -i "s/[[:space:]]${service}//g" "${TARGET_FILE}"
            fi
        done
    fi
}