DESCRIPTION = "Control Manager component"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SECTION = "base"
DEPENDS = "sqlite3 curl rdkversion jansson glib-2.0 systemd iarmbus iarmmgrs breakpad util-linux devicesettings nopoll rfc libarchive safec-common-wrapper gperf-native xr-voice-sdk libsyswrapper xr-voice-sdk-headers"

DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec', " ", d)}"
RDEPENDS:${PN}:append = " devicesettings iarmbus"

PROVIDES = "ctrlm"
RPROVIDES:${PN} = "ctrlm"


inherit cmake pkgconfig ${@bb.utils.contains("DISTRO_FEATURES", "kirkstone", "python3native", "pythonnative", d)} syslog-ng-config-gen breakpad-wrapper breakpad-logmapper logrotate_config

# Breakpad processname and logfile mapping
BREAKPAD_LOGMAPPER_PROCLIST = "controlMgr"
BREAKPAD_LOGMAPPER_LOGLIST = "ctrlm_log.txt"

SYSLOG-NG_FILTER = "ctrlm"
SYSLOG-NG_SERVICE_ctrlm = "ctrlm-hal-rf4ce.service ctrlm-main.service"
SYSLOG-NG_DESTINATION_ctrlm = "ctrlm_log.txt"
SYSLOG-NG_LOGRATE_ctrlm = "medium"

PV ?= "1.0.1"
PR ?= "r0"

SRC_URI = "${CMF_GITHUB_ROOT}/control;${CMF_GITHUB_SRC_URI_SUFFIX};name=ctrlm-main"

LOGROTATE_NAME="ctrlm_log"
LOGROTATE_LOGNAME_ctrlm_log="ctrlm_log.txt"
#HDD_DISABLE
LOGROTATE_SIZE_MEM_ctrlm_log="1572864"
LOGROTATE_ROTATION_MEM_ctrlm_log="3"
#HDD_ENABLE
LOGROTATE_SIZE_ctrlm_log="20971520"
LOGROTATE_ROTATION_ctrlm_log="25"

SRC_URI:append = " file://ctrlm-main.service"
SRC_URI:append = " file://1_rf4ce.conf"
SRC_URI:append = " file://ctrlm-hal-rf4ce.service"

VERSION_TEST_TONES = "20220616"
SRC_URI:append = "${@bb.utils.contains('BUILD_FACTORY_TEST', 'true', ' ${RDK_ARTIFACTS_BASE_URL}/generic/components/yocto/ctrlm_factory/test_tones/test_tones_${VERSION_TEST_TONES}/2.1/test_tones_${VERSION_TEST_TONES}-2.1.tar.bz2;name=test_tones', '', d)}"
SRC_URI[test_tones.md5sum]    = "${@bb.utils.contains('BUILD_FACTORY_TEST', 'true', 'd9e7829785f011214ec948f417873825', '', d)}"
SRC_URI[test_tones.sha256sum] = "${@bb.utils.contains('BUILD_FACTORY_TEST', 'true', 'ef10d7174a8bc79aff71b30980cd1304a2a33cf10afc38049c13cb11d1a309cc', '', d)}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
SRCREV_FORMAT = "ctrlm-main"

S = "${WORKDIR}/git"

FILES:${PN} += "${@bb.utils.contains('BUILD_FACTORY_TEST', 'true', '${datadir}/tone_1khz.wav', '', d)}"
FILES:${PN} += "${systemd_unitdir}/system/ctrlm-main.service "
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'rf4ce', '${systemd_unitdir}/system/ctrlm-main.service.d/1_rf4ce.conf', '', d)}"

SYSTEMD_PACKAGES += " ctrlm-main"
SYSTEMD_SERVICE:ctrlm-main  = "ctrlm-main.service"
SYSTEMD_SERVICE:ctrlm-main += "${@bb.utils.contains('DISTRO_FEATURES', 'ctrlm_generic', 'ctrlm-hal-rf4ce.service', '', d)}"

ENABLE_GPERFTOOLS_HEAPCHECK_WP_DISTRO = "1"
EXTRA_OECMAKE:append = "${@bb.utils.contains('DISTRO_FEATURES_RDK', 'comcast-gperftools-heapcheck-wp', ' -DFDC_ENABLED=ON', '', d)}"
inherit ${@bb.utils.contains('DISTRO_FEATURES', 'comcast-gperftools-heapcheck-wp', 'comcast-gperftools-heapcheck-wp', '', d)}

inherit systemd coverity

BREAKPAD_BIN = "controlMgr"

EXTRA_OECMAKE:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' -DUSE_SAFEC=ON', '', d)}"

#EXTRA_OECMAKE:append = " -DMEM_DEBUG=ON"
#EXTRA_OECMAKE:append = " -DANSI_CODES_DISABLED=ON"

# Default to HDMI / CEC discovery for IRDB
# Options: NONE / HDMI / CEC / ALL
IRDB_DISCOVERY_DEFAULT = "ALL"
IRDB_DISCOVERY ??= "${IRDB_DISCOVERY_DEFAULT}"
EXTRA_OECMAKE:append = "${@bb.utils.contains('IRDB_DISCOVERY', 'ALL', ' -DIRDB_HDMI_DISCOVERY=ON -DIRDB_CEC_DISCOVERY=ON', '', d)}"
EXTRA_OECMAKE:append = "${@bb.utils.contains('IRDB_DISCOVERY', 'HDMI', ' -DIRDB_HDMI_DISCOVERY=ON', '', d)}"
EXTRA_OECMAKE:append = "${@bb.utils.contains('IRDB_DISCOVERY', 'CEC', ' -DIRDB_CEC_DISCOVERY=ON', '', d)}"

# Thunder Dependency
THUNDER             ??= "true"
DEPENDS:append        = "${@bb.utils.contains('THUNDER', 'true', ' wpeframework', '', d)}"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE:append  = "${@bb.utils.contains('THUNDER', 'true', bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', " -DWPE_FRAMEWORK_COM_SOCKET=ON", " -DWPE_FRAMEWORK_PROTO_TRACING=ON", d), " ", d)}"
EXTRA_OECMAKE:append  = "${@bb.utils.contains('THUNDER', 'true', ' -DTHUNDER=ON', '', d)}"

THUNDER_SECURITY  ??= "${@bb.utils.contains('DISTRO_FEATURES', 'thunder_security_disable', 'false', 'true', d)}"
DEPENDS:append      = "${@bb.utils.contains('THUNDER_SECURITY', 'true', ' wpeframework-clientlibraries', '', d)}"
LDFLAGS:append      = "${@bb.utils.contains('THUNDER_SECURITY', 'true', ' -lWPEFrameworkSecurityUtil', '', d)}"
EXTRA_OECMAKE:append = "${@bb.utils.contains('THUNDER_SECURITY', 'true', ' -DTHUNDER_SECURITY=ON', '', d)}"


# Telemetry Support
TELEMETRY_SUPPORT  ??= "true"
DEPENDS:append      = "${@bb.utils.contains('TELEMETRY_SUPPORT', 'true', ' telemetry', '', d)}"
EXTRA_OECMAKE:append = "${@bb.utils.contains('TELEMETRY_SUPPORT', 'true', ' -DTELEMETRY_SUPPORT=ON', '', d)}"

##################################################
# BLE support BEGIN

RDEPENDS:${PN}:append = "${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth', ' bluez5', '', d)}"

RDEPENDS:${PN}:append = "${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth', ' bluetooth-mgr', '', d)}"
DEPENDS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth', ' bluetooth-mgr', '', d)}"
LDFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth', ' -lBTMgr', '', d)}"

EXTRA_OECMAKE:append = "${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth', ' -DBLE_ENABLED=ON', '', d)}"
SRC_URI:append = "${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth', ' file://2_bluetooth.conf', '', d)}"
FILES:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth', '${systemd_unitdir}/system/ctrlm-main.service.d/2_bluetooth.conf', '', d)}"

#100 byte ADPCM coming from BLE remote needs to be decoded to PCM before sending to endpoint
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth', ' -DAUDIO_DECODE', '', d)}"

# BLE Services Implementation
BLE_SERVICES       ??= "false"
DEPENDS:append       = "${@bb.utils.contains('BLE_SERVICES', 'true', ' ctrlm-ble-services', '', d)}"
EXTRA_OECMAKE:append = "${@bb.utils.contains('BLE_SERVICES', 'true', ' -DBLE_SERVICES=ON', '', d)}"

# BLE support END
##################################################

DEPENDS:append = " libevdev"

# Advanced Secure Binding Support
ASB ??= "false"
EXTRA_OECMAKE:append = "${@bb.utils.contains('ASB', 'true', ' -DASB=ON', '', d)}"

# Authorization Support
AUTH                ?= "true"
EXTRA_OECMAKE:append = "${@bb.utils.contains('AUTH', 'true', ' -DAUTH_ENABLED=ON', '', d)}"

IRDB ??= "true"

EXTRA_OECMAKE:append = "${@bb.utils.contains('IRDB', 'true', ' -DIRDB=ON', '', d)}"

RF4CE_PACKET_ANALYSIS ??= "true"
EXTRA_OECMAKE:append = "${@bb.utils.contains('RF4CE_PACKET_ANALYSIS', 'true', ' -DRF4CE_PACKET_ANALYSIS=ON', '', d)}"

VOICE_NEXTGEN_MAC ??= "true"
EXTRA_OECMAKE:append = "${@bb.utils.contains('VOICE_NEXTGEN_MAC', 'true', ' -DVOICE_NEXTGEN_MAC=ON', '', d)}"

VOICE_KEYWORD_BEEP ??= "false"
EXTRA_OECMAKE:append = "${@bb.utils.contains('VOICE_KEYWORD_BEEP', 'true', ' -DVOICE_KEYWORD_BEEP=ON', '', d)}"

# Enable breakpad
EXTRA_OECMAKE:append = " -DBREAKPAD=ON"

SUPPORT_VOICE_DEST_HTTP   ?= "false"
SUPPORT_VOICE_DEST_ALSA   ?= "false"

EXTRA_OECMAKE:append = "${@ ' -DXRSR_SDT=ON' if (d.getVar('SUPPORT_VOICE_DEST_ALSA', expand=False) == "true") else ''}"
EXTRA_OECMAKE:append = "${@ ' -DXRSR_HTTP=ON' if (d.getVar('SUPPORT_VOICE_DEST_HTTP', expand=False) == "true") else ''}"

BUILD_FACTORY_TEST ??= "true"
EXTRA_OECMAKE:append = "${@bb.utils.contains('BUILD_FACTORY_TEST', 'true', ' -DBUILD_FACTORY_TEST=ON', '', d)}"

AUDIO_CONTROL         ?= "false"
EXTRA_OECMAKE:append   = "${@bb.utils.contains('AUDIO_CONTROL', 'true', ' -DFACTORY_AUDIO_CONTROL=ON', '', d)}"

export CTRLM_UTILS_JSON_TO_HEADER  = "${RECIPE_SYSROOT}/usr/include/vsdk_json_to_header.py"
export CTRLM_UTILS_JSON_COMBINE    = "${RECIPE_SYSROOT}/usr/include/vsdk_json_combine.py"

export STAGING_BINDIR_NATIVE

CTRLM_CONFIG_VSDK     = "${PKG_CONFIG_SYSROOT_DIR}/usr/include/xrsr_config.json"
CTRLM_CONFIG_CPC      = "${PKG_CONFIG_SYSROOT_DIR}/usr/include/ctrlm_cpc_config.json"
CTRLM_CONFIG_OEM_ADD  = "${S}/../ctrlm_config_oem.add.json"
CTRLM_CONFIG_OEM_SUB  = "${S}/../ctrlm_config_oem.sub.json"
CTRLM_CONFIG_CPC_ADD  = "${S}/../ctrlm_config_cpc.add.json"
CTRLM_CONFIG_CPC_SUB  = "${S}/../ctrlm_config_cpc.sub.json"
CTRLM_CONFIG_MAIN_ADD = "${S}/../ctrlm_config_main.add.json"
CTRLM_CONFIG_MAIN_SUB = "${S}/../ctrlm_config_main.sub.json"

EXTRA_OECMAKE:append  = " -DCTRLM_UTILS_JSON_COMBINE=${CTRLM_UTILS_JSON_COMBINE} -DCTRLM_UTILS_JSON_TO_HEADER=${CTRLM_UTILS_JSON_TO_HEADER} -DCTRLM_CONFIG_JSON_VSDK=${CTRLM_CONFIG_VSDK} -DCTRLM_CONFIG_JSON_CPC=${CTRLM_CONFIG_CPC} -DCTRLM_CONFIG_JSON_OEM_SUB=${CTRLM_CONFIG_OEM_SUB} -DCTRLM_CONFIG_JSON_OEM_ADD=${CTRLM_CONFIG_OEM_ADD} -DCTRLM_CONFIG_JSON_CPC_SUB=${CTRLM_CONFIG_CPC_SUB} -DCTRLM_CONFIG_JSON_CPC_ADD=${CTRLM_CONFIG_CPC_ADD} -DCTRLM_CONFIG_JSON_MAIN_ADD=${CTRLM_CONFIG_MAIN_ADD} -DCTRLM_CONFIG_JSON_MAIN_SUB=${CTRLM_CONFIG_MAIN_SUB}"
EXTRA_OECMAKE:append = "${@ ' -DXRSR_HTTP=ON' if (d.getVar('SUPPORT_VOICE_DEST_HTTP', expand=False) == "true") else ''}"
EXTRA_OECMAKE:append = "${@ ' -DXRSR_SDT=ON' if (d.getVar('SUPPORT_VOICE_DEST_ALSA', expand=False) == "true") else ''}"

DEPENDS:append   = "${@ ' virtual-mic' if (d.getVar('SUPPORT_VOICE_DEST_ALSA',   expand=False) == "true") else ''}"

EXTRA_OECMAKE:append = " -DCMAKE_SYSROOT=${RECIPE_SYSROOT}"
EXTRA_OECMAKE:append = " -DGIT_BRANCH=${CMF_GIT_BRANCH}"
EXTRA_OECMAKE:append = "${@bb.utils.contains('DISTRO_FEATURES', 'ctrlm_mic_tap', ' -DMIC_TAP=ON', '', d)}"

RF4CE_ENABLED = "${@bb.utils.contains('MACHINE_FEATURES', 'rf4ce' ,'true', 'false', d)}"
BLE_ENABLED   = "${@bb.utils.contains('MACHINE_FEATURES', 'ble-rcu' ,'true', 'false', d)}"

addtask ctrlm_config after do_configure before do_compile
do_ctrlm_config() {
    if [ "${RF4CE_ENABLED}" = false -a "${BLE_ENABLED}" = true ]; then
        echo '{"network_rf4ce":{}}' > ${CTRLM_CONFIG_MAIN_SUB}
    fi

    if [ "${BLE_ENABLED}" = false -a "${RF4CE_ENABLED}" = true ]; then
        echo '{"network_ble":{}}' > ${CTRLM_CONFIG_MAIN_SUB}
    fi
}

FACTORY_AUDIO_PLAYBACK ?= "false"
EXTRA_OECMAKE:append   = "${@bb.utils.contains('AUDIO_PLAYBACK', 'true', ' -DFACTORY_AUDIO_PLAYBACK=ON', '', d)}"

CUSTOM_AUDIO_ANALYSIS_LIB ?= ""
EXTRA_OECMAKE:append       = "${@ ' -DCUSTOM_AUDIO_ANALYSIS_LIB=${CUSTOM_AUDIO_ANALYSIS_LIB}' if (d.getVar('CUSTOM_AUDIO_ANALYSIS_LIB', expand=False) != "") else ''}"

CUSTOM_AUTH_LIB      ?= ""
EXTRA_OECMAKE:append  = "${@ ' -DCUSTOM_AUTH_LIB=${CUSTOM_AUTH_LIB}' if (d.getVar('CUSTOM_AUTH_LIB', expand=False) != "") else ''}"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${S}/../ctrlm-main.service ${D}${systemd_unitdir}/system/

    if ${@bb.utils.contains('EXTRA_OECMAKE', '-DRF4CE_ENABLED=ON', 'true', 'false', d)}; then
       install -d ${D}${systemd_unitdir}/system/ctrlm-main.service.d/
       install -m 0644 ${S}/../1_rf4ce.conf ${D}${systemd_unitdir}/system/ctrlm-main.service.d/
    fi

    if ${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth', 'true', 'false', d)}; then
       install -d ${D}${systemd_unitdir}/system/ctrlm-main.service.d/
       install -m 0644 ${S}/../2_bluetooth.conf ${D}${systemd_unitdir}/system/ctrlm-main.service.d/
    fi

    if [ "${CTRLM_GENERIC}" = "true" ]; then
       install -m 0644 ${S}/../ctrlm-hal-rf4ce.service ${D}${systemd_unitdir}/system/
    fi
}

addtask clean_oem_config after do_unpack before do_configure

do_clean_oem_config() {
    rm -f ${CTRLM_CONFIG_CPC_ADD} ${CTRLM_CONFIG_CPC_SUB} ${CTRLM_CONFIG_OEM_ADD} ${CTRLM_CONFIG_OEM_SUB}
}

