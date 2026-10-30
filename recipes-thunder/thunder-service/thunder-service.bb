SUMMARY = "systemd unit (wpeframework.service) that starts the Thunder daemon"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRC_URI = "file://wpeframework.service.in"

PV = "1.0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

S = "${WORKDIR}"

inherit systemd

THUNDER_SERVICE_EXEC ?= "/usr/bin/${THUNDER_NAMESPACE} -b"

do_compile[noexec] = "1"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/wpeframework.service.in ${D}${systemd_unitdir}/system/wpeframework.service

    sed -i "s|^ExecStart=.*|ExecStart=${THUNDER_SERVICE_EXEC}|" ${D}${systemd_unitdir}/system/wpeframework.service

    # Propagate configured keymap via parent service environment to rdkwindowmanager plugin.
    if [ -n "${WINDOWMANAGER_RCU_KEYMAP_FILE}" ]; then
        WPEFW_SERVICE="${D}${systemd_unitdir}/system/wpeframework.service"

        if [ -f "$WPEFW_SERVICE" ]; then
            if grep -Eq '^[[:space:]]*Environment="?RDK_WINDOW_MANAGER_KEYMAP_FILE=' "${WPEFW_SERVICE}"; then
                bbnote "Updating Windowmanager KEYMAP env in wpeframework.service"
                sed -i -E "s|^[[:space:]]*Environment=\"?RDK_WINDOW_MANAGER_KEYMAP_FILE=.*$|Environment=\"RDK_WINDOW_MANAGER_KEYMAP_FILE=${WINDOWMANAGER_RCU_KEYMAP_FILE}\"|" "${WPEFW_SERVICE}"
            else
                bbnote "Adding Windowmanager KEYMAP env in wpeframework.service"
                sed -i "/^\[Service\]/a Environment=\"RDK_WINDOW_MANAGER_KEYMAP_FILE=${WINDOWMANAGER_RCU_KEYMAP_FILE}\"" "${WPEFW_SERVICE}"
            fi
        fi
    fi
}

SYSTEMD_SERVICE:${PN} = "wpeframework.service"

RDEPENDS:${PN} += "${THUNDER_NAMESPACE_LC}"
