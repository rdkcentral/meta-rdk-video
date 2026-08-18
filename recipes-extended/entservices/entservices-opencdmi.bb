SUMMARY = "ENTServices opencdmi plugins"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7a65e8e9836ac44d082594220a9a3883"

PV = "2.0.5"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-opencdmi;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://index.html \
           file://thunder_acl.json \
           file://rdkshell_post_startup.conf \
           file://rdkservices.ini \
           file://open_cdm_imp_fps_ocdm.patch \
           file://opencdmi-fps-plugin-frameworkrpc.patch \
          "
          
# Release version - 2.0.5
SRCREV = "74c11386348a4a1fb6fc18ab2bfb51bf1e72b18e"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}" 
TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

EXTRA_OECMAKE += " -DENABLE_RFC_MANAGER=ON"

DEPENDS += "wpeframework wpeframework-tools-native entservices-apis"
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

# ----------------------------------------------------------------------------

def get_cdmi_adapter(d):
    if bb.utils.contains("DISTRO_FEATURES", "rdk_svp", "true", "false", d) == "true":
        return "opencdmi_rdk_svp"
    else:
        return "opencdm_gst"

WPE_CDMI_ADAPTER_IMPL = "${@get_cdmi_adapter(d)}"

PACKAGECONFIG ?= " breakpadsupport \
    telemetrysupport \
    ${@bb.utils.contains('DISTRO_FEATURES', 'opencdm', 'opencdmi ${WPE_CDMI_ADAPTER_IMPL}', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'playready_nexus_svp',  'opencdmi_prnx_svp', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'widevine_nexus_svp',   'opencdmi_wv_svp', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'clearkey',             'opencdmi_ck', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'fairplay',             'opencdmi_fps', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'dlnasupport', ' dlna', '', d)} \
"

# enable widevine and Playready4 opencdmi libs
OPENCDM_DRMS ??= " ${@bb.utils.contains_any('DISTRO_FEATURES' , ['widevine_v14' , 'widevine_v16' , 'widevine_v18'], 'opencdmi_wv', '', d)} ${@bb.utils.contains_any('DISTRO_FEATURES' , ['playready4' , 'playready4_6'], 'opencdmi_pr4', '', d)} ${@bb.utils.contains_any('DISTRO_FEATURES' , ['fairplay'], 'opencdmi_fps', '', d)}"
PACKAGECONFIG:append = " ${OPENCDM_DRMS}"

inherit features_check
REQUIRED_DISTRO_FEATURES = "${@bb.utils.contains('DISTRO_FEATURES', 'DAC-sec', 'DOBBY_CONTAINERS', '', d)}"

EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'disable_security_agent', ' -DENABLE_SECURITY_AGENT=OFF ', '  ', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'link_localtime', ' -DBUILD_ENABLE_LINK_LOCALTIME=ON', '',d)}"
# Enable the RDKShell memcr feature support flags
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'RDKTV_APP_HIBERNATE', ' -DPLUGIN_HIBERNATESUPPORT=ON -DPLUGIN_HIBERNATE_NATIVE_APPS_ON_SUSPENDED=ON','',d)}"
EXTRA_OECMAKE += "${@bb.utils.contains("BUILD_VARIANT", "debug", "-DPLUGIN_BUILD_TYPE=Debug", "-DPLUGIN_BUILD_TYPE=Release", d)}"

PACKAGECONFIG[breakpadsupport]      = ",,breakpad-wrapper,breakpad-wrapper"
PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"

# Override include/ocdm.inc to avoid pulling wpeframework-clientlibraries; libocdm.so is now built here.
PACKAGECONFIG[opencdmi]             = "-DPLUGIN_OPENCDMI=ON -DPLUGIN_OPENCDMI_AUTOSTART=true -DPLUGIN_OPENCDMI_MODE=Local,,,"
PACKAGECONFIG[opencdm_gst]          = '-DCDMI_ADAPTER_IMPLEMENTATION="gstreamer",,gstreamer1.0'
PACKAGECONFIG[opencdmi_rdk_svp]     = '-DCDMI_ADAPTER_IMPLEMENTATION="rdk",,gst-svp-ext'

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


# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/*.so ${datadir}/WPEFramework/*"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"

