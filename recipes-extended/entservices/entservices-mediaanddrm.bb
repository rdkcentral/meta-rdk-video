SUMMARY = "ENTServices Media and DRM plugins"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=16cf2209d4e903e4d5dcd75089d7dfe2"

PV = "1.4.0"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-mediaanddrm;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://index.html \
           file://thunder_acl.json \
           file://rdkshell_post_startup.conf \
           file://rdkservices.ini \
           file://0001-RDKTV-20749-Revert-Merge-pull-request-3336-from-npol.patch \
           ${@bb.utils.contains('DISTRO_FEATURES', 'wpe_r4_4','file://0003-R4.4.1-SystemAudioPlayer-compilation-error.patch','',d)} \
          "
          
# Release version - 1.4.0
SRCREV = "738fbb2943b2649661c9bf0ef101a85d33273c30"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}" 
TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

EXTRA_OECMAKE += " -DENABLE_RFC_MANAGER=ON"

DEPENDS += "wpeframework wpeframework-tools-native"
RDEPENDS:${PN} += "wpeframework"

TARGET_LDFLAGS += " -Wl,--no-as-needed -ltelemetry_msgsender -Wl,--as-needed "

RDEPENDS:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'DOBBY_CONTAINERS', "dobby systemd","", d)}"

EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', 'disable_provision_precondition_rdkm', ' -DPLUGIN_OPENCDMI_GENERIC=ON', '', d)}"

CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/wdmp-c/ "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/trower-base64/ "
CXXFLAGS += " -DRFC_ENABLED "
# enable filtering for undefined interfaces and link local ip address notifications
CXXFLAGS += " -DNET_DEFINED_INTERFACES_ONLY -DNET_NO_LINK_LOCAL_ANNOUNCE "
CXXFLAGS += " -Wall -Werror "
CXXFLAGS:remove_morty = " -Wall -Werror "
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"

# More complicated plugins are moved seperate includes
include include/ocdm.inc
include include/texttospeech.inc

# ----------------------------------------------------------------------------

PACKAGECONFIG ?= "  breakpadsupport \
    telemetrysupport \
    texttospeech \
    ${@bb.utils.contains('DISTRO_FEATURES', 'dlnasupport', ' dlna', '', d)} \
"

DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"

# enable widevine and Playready4 opencdmi libs
PACKAGECONFIG:append = " systemaudioplayer"

inherit features_check
REQUIRED_DISTRO_FEATURES = "${@bb.utils.contains('DISTRO_FEATURES', 'DAC-sec', 'DOBBY_CONTAINERS', '', d)}"

EXTRA_OECMAKE += "-DDISABLE_GEOGRAPHY_TIMEZONE=ON"

EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'disable_security_agent', ' -DENABLE_SECURITY_AGENT=OFF ', '  ', d)}"
EXTRA_OECMAKE += " -DBUILD_ENABLE_DEVICE_MANUFACTURER_INFO=ON "
EXTRA_OECMAKE += " -DBUILD_ENABLE_THERMAL_PROTECTION=ON "
EXTRA_OECMAKE += " -DENABLE_SYSTEM_GET_STORE_DEMO_LINK=ON "
EXTRA_OECMAKE += " -DBUILD_ENABLE_APP_CONTROL_AUDIOPORT_INIT=ON "
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'link_localtime', ' -DBUILD_ENABLE_LINK_LOCALTIME=ON', '',d)}"
# Enable the RDKShell memcr feature support flags
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'RDKTV_APP_HIBERNATE', ' -DPLUGIN_HIBERNATESUPPORT=ON -DPLUGIN_HIBERNATE_NATIVE_APPS_ON_SUSPENDED=ON','',d)}"
EXTRA_OECMAKE += "${@bb.utils.contains("BUILD_VARIANT", "debug", "-DPLUGIN_BUILD_TYPE=Debug", "-DPLUGIN_BUILD_TYPE=Release", d)}"

PACKAGECONFIG[breakpadsupport]      = ",,breakpad-wrapper,breakpad-wrapper"
PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"
PACKAGECONFIG[systemaudioplayer]    = "-DPLUGIN_SYSTEMAUDIOPLAYER=ON,,entservices-apis trower-base64 boost websocketpp wpeframework-clientlibraries openssl gstreamer1.0 gstreamer1.0-plugins-base gstreamer1.0-plugins-base-app,entservices-apis trower-base64 wpeframework-clientlibraries openssl gstreamer1.0 gstreamer1.0-plugins-base gstreamer1.0-plugins-base-app"
PACKAGECONFIG[unifiedcasmanagement] = "-DPLUGIN_UNIFIEDCASMANAGEMENT=ON,,rmfgeneric, rmfgeneric"

# ----------------------------------------------------------------------------

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DSECAPI_LIB=sec_api \
"

# Check if DRI_DEVICE_NAME is defined. If yes- use that as DEFAULT_DEVICE. If not, use DEFAULT_DEVICE configured from rdkservices.
python () {
    dri_device_name = d.getVar('DRI_DEVICE_NAME')
    if dri_device_name:
        d.appendVar('OECMAKE_CXX_FLAGS', ' -DDEFAULT_DEVICE=\'\\"{}\\"\' '.format(dri_device_name))
}

do_install:append() {
    install -d ${D}${sysconfdir}/rfcdefaults
    install -m 0644 ${WORKDIR}/thunder_acl.json ${D}${sysconfdir}
    if ${@bb.utils.contains_any("DISTRO_FEATURES", "rdkshell_ra second_form_factor", "true", "false", d)}
    then
      install -m 0644 ${WORKDIR}/rdkservices.ini ${D}${sysconfdir}/rfcdefaults/
    fi
    if ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'true', 'false', d)} == 'true'; then
        if [ -d "${D}/etc/WPEFramework/plugins" ]; then
            find ${D}/etc/WPEFramework/plugins/ -type f | xargs sed -i -r 's/"autostart"[[:space:]]*:[[:space:]]*true/"autostart":false/g'
        fi
    fi
}

PACKAGES =+ "${PN}-test"
FILES:${PN}-test += "${bindir}/remoteControlTestClient ${bindir}/SystemAudioPlayerAPITest ${bindir}/TTSThunderAPITest"

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/*.so ${datadir}/WPEFramework/* ${sysconfdir}/thunder_acl.json"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"


