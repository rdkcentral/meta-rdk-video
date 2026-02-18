SUMMARY = "ENTServices appmanagers plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=9adde9d5cb6e9c095d3e3abf0e9500f1"

PV ?= "0.1.0.0"
PR ?= "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRCREV = "95c40b2ca535eea5f934cf92bb926b4cbacc26d9"

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-appmanagers;${CMF_GITHUB_SRC_URI_SUFFIX}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'wpe_security_util_disable', ' -DDISABLE_SECURITY_TOKEN=ON', '', d)}"

EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', 'prodlog-variant prod-variant', '-DRDK_APPMANAGERS_DEBUG=OFF', '-DRDK_APPMANAGERS_DEBUG=ON', d)}"

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

PACKAGECONFIG ?= " telemetrysupport \
    ocicontainer \
    rdknativescript \
    runtimemanager \
    packagemanager \
    lifecyclemanager \
    storagemanager \
    appmanager \
    preinstallmanager \
    downloadmanager \
    rdkwindowmanager \
    telemetrymetrics \
    ${@bb.utils.contains('DISTRO_FEATURES', 'DAC-sec',              'ocicontainersec', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'opencdm', 'opencdmi', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'rialto_in_dac', 'rialtodac', '', d)} \
"

inherit features_check
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'disable_security_agent', ' -DENABLE_SECURITY_AGENT=OFF ', '  ', d)}"

# Enable the RDKShell memcr feature support flags
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'RDKTV_APP_HIBERNATE', ' -DPLUGIN_HIBERNATESUPPORT=ON -DPLUGIN_HIBERNATE_NATIVE_APPS_ON_SUSPENDED=ON','',d)}"

# ----------------------------------------------------------------------------

PACKAGECONFIG[ocicontainer]         = "-DPLUGIN_OCICONTAINER=ON, -DPLUGIN_OCICONTAINER=OFF, dobby entservices-apis systemd, dobby entservices-apis systemd"
PACKAGECONFIG[ocicontainersec]      = "                        ,                          ,   omi,   omi"
PACKAGECONFIG[rialtodac]            = "-DRIALTO_IN_DAC_FEATURE=ON,-DRIALTO_IN_DAC_FEATURE=OFF,rialto,rialto-servermanager-lib"
PACKAGECONFIG[rdknativescript]      = "-DPLUGIN_NATIVEJS=ON,-DPLUGIN_NATIVEJS=OFF,rdknativescript,libuv"
PACKAGECONFIG[opencdmi]             = "-DPLUGIN_OPENCDMI=ON"
PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"
PACKAGECONFIG[telemetry]            = "-DPLUGIN_TELEMETRY=ON,,iarmbus iarmmgrs entservices-apis rfc rbus,iarmbus entservices-apis rfc rbus"
PACKAGECONFIG[runtimemanager]       = "-DPLUGIN_RUNTIME_MANAGER=ON ${RUNTIMEMANAGER_PLUGIN_ARGS},-DPLUGIN_RUNTIME_MANAGER=OFF,entservices-apis iptables,entservices-apis iptables"
PACKAGECONFIG[packagemanager]       = "-DPLUGIN_PACKAGE_MANAGER=ON ${PACKAGEMANAGER_PLUGIN_ARGS} -DLIB_PACKAGE=ON -DSYSROOT_PATH=${STAGING_DIR_TARGET},-DPLUGIN_PACKAGE_MANAGER=OFF -DLIB_PACKAGE=OFF,curl virtual/libpackage,curl virtual/libpackage"
PACKAGECONFIG[lifecyclemanager]     = "-DPLUGIN_LIFECYCLE_MANAGER=ON,-DPLUGIN_LIFECYCLE_MANAGER=OFF,websocketpp entservices-apis,entservices-apis"
PACKAGECONFIG[storagemanager]       = "-DPLUGIN_STORAGE_MANAGER=ON,-DPLUGIN_STORAGE_MANAGER=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[appmanager]           = "-DPLUGIN_APPMANAGER=ON,-DPLUGIN_APPMANAGER=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[preinstallmanager]    = "-DPLUGIN_PREINSTALL_MANAGER=ON ${PREINSTALLMANAGER_PLUGIN_ARGS},-DPLUGIN_PREINSTALL_MANAGER=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[downloadmanager]      = "-DPLUGIN_DOWNLOADMANAGER=ON -DLIB_PACKAGE=ON -DSYSROOT_PATH=${STAGING_DIR_TARGET},-DPLUGIN_DOWNLOADMANAGER=OFF -DLIB_PACKAGE=OFF,entservices-apis curl virtual/libpackage,entservices-apis curl virtual/libpackage"
PACKAGECONFIG[rdkwindowmanager]     = "-DPLUGIN_RDK_WINDOW_MANAGER=ON,-DPLUGIN_RDK_WINDOW_MANAGER=OFF,rdkwindowmanager entservices-apis,rdkwindowmanager entservices-apis"
PACKAGECONFIG[telemetrymetrics]     = "-DPLUGIN_TELEMETRYMETRICS=ON,-DPLUGIN_TELEMETRYMETRICS=OFF,entservices-apis,entservices-apis"
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

PREINSTALLMANAGER_PLUGIN_ARGS         ?= " \
                                       -DPLUGIN_PREINSTALL_MANAGER_APP_PREINSTALL_DIRECTORY=${APP_PREINSTALL_DIRECTORY} \
"
RUNTIME_APP_PORTAL ?= "com.sky.as.apps"
APP_PREINSTALL_DIRECTORY ?= "/opt/preinstall"
NATIVEJS_CLIENTIDENTIFIER ?= "wst-nativejs"

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DSECAPI_LIB=sec_api \
    -DPLUGIN_NATIVEJS=ON \
    -DPLUGIN_NATIVEJS_CLIENTIDENTIFIER="${NATIVEJS_CLIENTIDENTIFIER}" \
    -DAIMANAGERS_TELEMETRY_METRICS_SUPPORT=ON \
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
