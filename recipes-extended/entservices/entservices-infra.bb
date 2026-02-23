SUMMARY = "ENTServices Infra plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=9adde9d5cb6e9c095d3e3abf0e9500f1"

PV = "3.18.2"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRCREV = "31feb2bcfec51e088dfdb9cfc08b3e333fc97905"
SRC_URI = "${CMF_GITHUB_ROOT}/entservices-infra;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://rdkshell_post_startup.conf \
           file://rdkservices.ini \
           file://0001-RDKTV-20749-Revert-Merge-pull-request-3336-from-npol.patch \
          "

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'wpe_security_util_disable', ' -DDISABLE_SECURITY_TOKEN=ON', '', d)}"

EXTRA_OECMAKE += " -DPLUGIN_ANALYTICS_SIFT_STORE_PATH=/opt/persistent/AnalyticsSiftStore"

EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', 'prodlog-variant prod-variant', '-DRDK_APPMANAGERS_DEBUG=OFF', '-DRDK_APPMANAGERS_DEBUG=ON', d)}"

DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV', "tvsettings-hal-headers ", "", d)}"
DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV', "virtual/vendor-tvsettings-hal ", "", d)}"
DEPENDS += "wpeframework wpeframework-tools-native wpeframework-clientlibraries"
RDEPENDS:${PN} += "wpeframework"
DEPENDS += "packager-headers"

CFLAGS  += " \
    -I=${includedir}/rdk/halif/power-manager \
    -I=${includedir}/rdk/halif/deepsleep-manager \
    "
TARGET_LDFLAGS += " -Wl,--no-as-needed -ltelemetry_msgsender -Wl,--as-needed "


CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/wdmp-c/ "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/trower-base64/ "
CXXFLAGS += " -DRFC_ENABLED "
# enable filtering for undefined interfaces and link local ip address notifications
CXXFLAGS += " -DNET_DEFINED_INTERFACES_ONLY -DNET_NO_LINK_LOCAL_ANNOUNCE "
CXXFLAGS += " -Wall -Werror "
CXXFLAGS:remove_morty = " -Wall -Werror "
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"

# ----------------------------------------------------------------------------

PACKAGECONFIG ?= " resourcemanager \
    telemetrysupport \
    rdknativescript \
    javascriptcore \          
    texttospeechmonitor \    
    ${@bb.utils.contains('DISTRO_FEATURES', 'rdkshell',             'rdkshell', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'rdkshell enable_rialto', 'rdkshellrialto', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'rialto_in_dac', 'rialtodac', '', d)} \    
"
PACKAGECONFIG:append = " erm"
PACKAGECONFIG:append = " rustadapter "

inherit features_check
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'disable_security_agent', ' -DENABLE_SECURITY_AGENT=OFF ', '  ', d)}"


EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'rdkshell_disable_autostart', ' -DPLUGIN_RDKSHELL_AUTOSTART=false ', ' -DPLUGIN_RDKSHELL_AUTOSTART=true ', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', 'rdkshell_ra second_form_factor', ' -DPLUGIN_RDKSHELL_AUTOSTART=true ', ' ', d)}"

# Enable the RDKShell memcr feature support flags
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'RDKTV_APP_HIBERNATE', ' -DPLUGIN_HIBERNATESUPPORT=ON -DPLUGIN_HIBERNATE_NATIVE_APPS_ON_SUSPENDED=ON','',d)}"

# ----------------------------------------------------------------------------

PACKAGECONFIG[erm]                  = "-DBUILD_ENABLE_ERM=ON,-DBUILD_ENABLE_ERM=OFF,essos,essos"
PACKAGECONFIG[resourcemanager]      = "-DPLUGIN_RESOURCEMANAGER=ON,-DPLUGIN_RESOURCEMANAGER=OFF,"
PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"
PACKAGECONFIG[telemetry]            = "-DPLUGIN_TELEMETRY=ON,,iarmbus iarmmgrs entservices-apis rfc rbus,iarmbus entservices-apis rfc rbus"
PACKAGECONFIG[analytics]            = "-DPLUGIN_ANALYTICS=ON,-DPLUGIN_ANALYTICS=OFF, entservices-apis, entservices-apis"
PACKAGECONFIG[rdkshell]             = "-DPLUGIN_RDKSHELL=ON,-DPLUGIN_RDKSHELL=OFF,rdkshell entservices-apis,rdkshell entservices-apis"
PACKAGECONFIG[rialtodac]            = "-DRIALTO_IN_DAC_FEATURE=ON,-DRIALTO_IN_DAC_FEATURE=OFF,rialto,rialto-servermanager-lib"
PACKAGECONFIG[rdkshellrialto]       = "-DRIALTO_FEATURE=ON,-DRIALTO_FEATURE=OFF,rialto,rialto-servermanager-lib"
PACKAGECONFIG[rustadapter]          = "-DPLUGIN_RUSTADAPTER=OFF,,,"
PACKAGECONFIG[runtimemanager]       = "-DPLUGIN_RUNTIME_MANAGER=ON ${RUNTIMEMANAGER_PLUGIN_ARGS},-DPLUGIN_RUNTIME_MANAGER=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[rdknativescript]      = "-DPLUGIN_NATIVEJS=ON,-DPLUGIN_NATIVEJS=OFF,rdknativescript,libuv"
PACKAGECONFIG[packagemanager]       = "-DPLUGIN_PACKAGE_MANAGER=ON ${PACKAGEMANAGER_PLUGIN_ARGS} -DLIB_PACKAGE=ON -DSYSROOT_PATH=${STAGING_DIR_TARGET},-DPLUGIN_PACKAGE_MANAGER=OFF -DLIB_PACKAGE=OFF,curl virtual/libpackage,curl virtual/libpackage"
PACKAGECONFIG[lifecyclemanager]     = "-DPLUGIN_LIFECYCLE_MANAGER=ON,-DPLUGIN_LIFECYCLE_MANAGER=OFF,websocketpp entservices-apis,entservices-apis"
PACKAGECONFIG[storagemanager]       = "-DPLUGIN_STORAGE_MANAGER=ON,-DPLUGIN_STORAGE_MANAGER=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[appmanager]           = "-DPLUGIN_APPMANAGER=ON,-DPLUGIN_APPMANAGER=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[opencdmi]             = "-DPLUGIN_OPENCDMI=ON"
PACKAGECONFIG[texttospeechmonitor]  = "-DPLUGIN_MONITOR_TEXTTOSPEECH=ON"
PACKAGECONFIG[preinstallmanager]    = "-DPLUGIN_PREINSTALL_MANAGER=ON,-DPLUGIN_PREINSTALL_MANAGER=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[downloadmanager]      = "-DPLUGIN_DOWNLOADMANAGER=ON -DLIB_PACKAGE=ON -DSYSROOT_PATH=${STAGING_DIR_TARGET},-DPLUGIN_DOWNLOADMANAGER=OFF -DLIB_PACKAGE=OFF,entservices-apis curl virtual/libpackage,entservices-apis curl virtual/libpackage"
# ----------------------------------------------------------------------------


PACKAGEMANAGER_PLUGIN_ARGS         ?= " \
                                       -DADD_DAC_PARAMS=${@d.getVar('DAC_PARAMS')} \
                                       -DPLUGIN_DAC_DB_PATH=${DAC_DB_PATH} \
                                       -DPLUGIN_DAC_APP_PATH=${DAC_APP_PATH} \
                                       -DPLUGIN_DAC_DATA_PATH=${DAC_DATA_PATH} \
                                       -DPLUGIN_DAC_ANTN_FILE=${DAC_ANN_FILE} \
                                       -DPLUGIN_DAC_ANTN_REGEX=${DAC_ANN_REGEX} \
                                       -DPLUGIN_DAC_BUN_FIRM_COMP_KEY=${DAC_BUN_FIRM_COMP_KEY} \
                                       -DPLUGIN_DAC_BUN_PLATNAME_OVERRIDE=${DAC_BUN_PLATNAME_OVERRIDE} \
                                       -DPLUGIN_DAC_CONFIGURL=${DAC_CONFIGURL} \
"
RUNTIMEMANAGER_PLUGIN_ARGS         ?= " \
                                       -DPLUGIN_RUNTIME_APP_PORTAL=${RUNTIME_APP_PORTAL} \
"
RUNTIME_APP_PORTAL ?= "com.sky.as.apps"
NATIVEJS_CLIENTIDENTIFIER ?= "wst-nativejs"

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DSECAPI_LIB=sec_api \
    -DPLUGIN_NATIVEJS=ON \
    -DPLUGIN_NATIVEJS_CLIENTIDENTIFIER="${NATIVEJS_CLIENTIDENTIFIER}" \
"

# TBD - set SECAPI_LIB to hw secapi once RDK-12682 changes are available
EXTRA_OECMAKE += " \
    -DBUILD_AMLOGIC=ON \
    -DBUILD_LLAMA=ON \
"

# Check if DRI_DEVICE_NAME is defined. If yes- use that as DEFAULT_DEVICE. If not, use DEFAULT_DEVICE configured from rdkservices.
python () {
    dri_device_name = d.getVar('DRI_DEVICE_NAME')
    if dri_device_name:
        d.appendVar('OECMAKE_CXX_FLAGS', ' -DDEFAULT_DEVICE=\'\\"{}\\"\' '.format(dri_device_name))
}

do_install:append() {
    install -d ${D}${sysconfdir}/rfcdefaults
    install -m 0644 ${WORKDIR}/rdkshell_post_startup.conf ${D}${sysconfdir}
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
