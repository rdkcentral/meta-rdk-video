DESCRIPTION = "Control Manager Headers"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SECTION = "base"
DEPENDS = ""

include ctrlm.inc

SRC_URI = "${CMF_GITHUB_ROOT}/control;${CMF_GITHUB_SRC_URI_SUFFIX};name=ctrlm-headers"
SRCREV:pn-ctrlm-headers = "${SRCREV:pn-ctrlm-main}"

S = "${WORKDIR}/git"

FILES:${PN} += "${includedir}/ctrlm_ipc.h \
                ${includedir}/ctrlm_ipc_rcu.h \
                ${includedir}/ctrlm_ipc_voice.h \
                ${includedir}/ctrlm_ipc_key_codes.h \
                ${includedir}/ctrlm_ipc_device_update.h \
                ${includedir}/ctrlm_ipc_ble.h \
                ${includedir}/ctrlm_hal.h \
                ${includedir}/ctrlm_hal_ip.h \
                ${includedir}/ctrlm_hal_ble.h \
                ${includedir}/ctrlm_hal_rf4ce.h \
               "

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}${includedir}
    install -m 644 ${S}/include/ctrlm_ipc.h ${D}${includedir}
    install -m 644 ${S}/include/ctrlm_ipc_rcu.h ${D}${includedir}
    install -m 644 ${S}/include/ctrlm_ipc_voice.h ${D}${includedir}
    install -m 644 ${S}/include/ctrlm_ipc_key_codes.h ${D}${includedir}
    install -m 644 ${S}/include/ctrlm_ipc_device_update.h ${D}${includedir}
    install -m 644 ${S}/include/ctrlm_ipc_ble.h ${D}${includedir}
    install -m 644 ${S}/include/ctrlm_hal.h ${D}${includedir}
    install -m 644 ${S}/include/ctrlm_hal_ip.h ${D}${includedir}
    install -m 644 ${S}/include/ctrlm_hal_ble.h ${D}${includedir}
    install -m 644 ${S}/include/ctrlm_hal_rf4ce.h ${D}${includedir}

    install -d ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/ctrlm_vendor_network_factory.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/ctrlm_log.h ${D}${includedir}/ctrlm_private

    # Authorization Support
    install -m 644 ${S}/src/auth/ctrlm_auth.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/factory/ctrlm_fta_platform.h ${D}${includedir}/ctrlm_private

    # Advanced Secure Binding
    install -m 644 ${S}/src/asb/ctrlm_asb.h ${D}${includedir}/ctrlm_private

    # Network Support
    install -m 644 ${S}/src/ctrlm.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/ctrlm_rcu.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/ctrlm_recovery.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/ctrlm_utils.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/ctrlm_validation.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/ctrlm_network.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/ctrlm_controller.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/attributes/ctrlm_attr.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/attributes/ctrlm_attr_general.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/attributes/ctrlm_attr_voice.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/attributes/ctrlm_version.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/database/ctrlm_database.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/database/ctrlm_db_attr.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/rfc/ctrlm_rfc.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/rfc/ctrlm_rfc_attr.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/config/ctrlm_config_attr.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/voice/ctrlm_voice_obj.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/voice/ctrlm_voice_types.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/voice/ipc/ctrlm_voice_ipc.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/voice/telemetry/ctrlm_voice_telemetry_events.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/json_config.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/ctrlm_tr181.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/telemetry/ctrlm_telemetry.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/telemetry/ctrlm_telemetry_event.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/telemetry/ctrlm_telemetry_markers.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/thunder/ctrlm_thunder_plugin.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/thunder/ctrlm_thunder_controller.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/thunder/helpers/ctrlm_thunder_helpers.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/ipc/ctrlm_rcp_ipc_iarm_thunder.h ${D}${includedir}/ctrlm_private
    install -m 644 ${S}/src/ipc/ctrlm_rcp_ipc_event.h ${D}${includedir}/ctrlm_private

    # BLE services
    install -d ${D}${includedir}/ctrlm_private/blercu
    install -m 644 ${S}/src/ble/hal/blercu/blercuerror.h           ${D}${includedir}/ctrlm_private/blercu
    install -m 644 ${S}/src/ble/hal/blercu/blegattservice.h        ${D}${includedir}/ctrlm_private/blercu
    install -m 644 ${S}/src/ble/hal/blercu/blegattcharacteristic.h ${D}${includedir}/ctrlm_private/blercu
    
    install -d ${D}${includedir}/ctrlm_private/blercu/bleservices
    install -m 644 ${S}/src/ble/hal/blercu/bleservices/blercuaudioservice.h      ${D}${includedir}/ctrlm_private/blercu/bleservices
    install -m 644 ${S}/src/ble/hal/blercu/bleservices/blercuinfraredservice.h   ${D}${includedir}/ctrlm_private/blercu/bleservices
    install -m 644 ${S}/src/ble/hal/blercu/bleservices/blercuupgradeservice.h    ${D}${includedir}/ctrlm_private/blercu/bleservices
    install -m 644 ${S}/src/ble/hal/blercu/bleservices/blercudeviceinfoservice.h ${D}${includedir}/ctrlm_private/blercu/bleservices

    install -d ${D}${includedir}/ctrlm_private/blercu/bleservices/gatt
    install -m 644 ${S}/src/ble/hal/blercu/bleservices/gatt/gatt_audiopipe.h         ${D}${includedir}/ctrlm_private/blercu/bleservices/gatt
    install -m 644 ${S}/src/ble/hal/blercu/bleservices/gatt/gatt_audioservice.h      ${D}${includedir}/ctrlm_private/blercu/bleservices/gatt
    install -m 644 ${S}/src/ble/hal/blercu/bleservices/gatt/gatt_infraredservice.h   ${D}${includedir}/ctrlm_private/blercu/bleservices/gatt
    install -m 644 ${S}/src/ble/hal/blercu/bleservices/gatt/gatt_infraredsignal.h    ${D}${includedir}/ctrlm_private/blercu/bleservices/gatt
    install -m 644 ${S}/src/ble/hal/blercu/bleservices/gatt/gatt_upgradeservice.h    ${D}${includedir}/ctrlm_private/blercu/bleservices/gatt
    install -m 644 ${S}/src/ble/hal/blercu/bleservices/gatt/gatt_deviceinfoservice.h ${D}${includedir}/ctrlm_private/blercu/bleservices/gatt
    install -m 644 ${S}/src/ble/hal/blercu/bleservices/gatt/gatt_external_services.h ${D}${includedir}/ctrlm_private/blercu/bleservices/gatt

    install -d ${D}${includedir}/ctrlm_private/ble
    install -d ${D}${includedir}/ctrlm_private/ble/hal
    install -m 644 ${S}/src/ble/hal/ctrlm_log_ble.h               ${D}${includedir}/ctrlm_private/ble/hal

    install -d ${D}${includedir}/ctrlm_private/ble/hal/dbus
    install -m 644 ${S}/src/ble/hal/dbus/dbusvariant.h            ${D}${includedir}/ctrlm_private/ble/hal/dbus
    install -m 644 ${S}/src/ble/hal/dbus/dbusabstractinterface.h  ${D}${includedir}/ctrlm_private/ble/hal/dbus
    install -m 644 ${S}/src/ble/hal/dbus/gdbusabstractinterface.h ${D}${includedir}/ctrlm_private/ble/hal/dbus

    install -d ${D}${includedir}/ctrlm_private/configsettings
    install -m 644 ${S}/src/ble/hal/configsettings/configsettings.h      ${D}${includedir}/ctrlm_private/configsettings
    install -m 644 ${S}/src/ble/hal/configsettings/configmodelsettings.h ${D}${includedir}/ctrlm_private/configsettings

    install -d ${D}${includedir}/ctrlm_private/utils
    install -m 644 ${S}/src/ble/hal/utils/slot.h             ${D}${includedir}/ctrlm_private/utils
    install -m 644 ${S}/src/ble/hal/utils/bleuuid.h          ${D}${includedir}/ctrlm_private/utils
    install -m 644 ${S}/src/ble/hal/utils/bleaddress.h       ${D}${includedir}/ctrlm_private/utils
    install -m 644 ${S}/src/ble/hal/utils/audioformat.h      ${D}${includedir}/ctrlm_private/utils
    install -m 644 ${S}/src/ble/hal/utils/pendingreply.h     ${D}${includedir}/ctrlm_private/utils
    install -m 644 ${S}/src/ble/hal/utils/filedescriptor.h   ${D}${includedir}/ctrlm_private/utils
    install -m 644 ${S}/src/ble/hal/utils/statemachine.h     ${D}${includedir}/ctrlm_private/utils
    install -m 644 ${S}/src/ble/hal/utils/futureaggregator.h ${D}${includedir}/ctrlm_private/utils
    install -m 644 ${S}/src/ble/hal/utils/fwimagefile.h      ${D}${includedir}/ctrlm_private/utils
}

ALLOW_EMPTY:${PN} = "1"
