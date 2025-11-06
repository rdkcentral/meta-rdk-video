SUMMARY = "ENTServices Infra plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=9adde9d5cb6e9c095d3e3abf0e9500f1"

PV ?= "3.0.0"
PR ?= "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-infra;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://rdkshell_post_startup.conf \
           file://0001-Add-monitoring-of-cloned-callsigns.patch \
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

PACKAGECONFIG ?= " monitor \
    persistent_store \
    resourcemanager \
    sharedstorage \
    telemetrysupport \
    usbdevice \
    usbmass_storage \
    usersettings \
    ocicontainer \
    runtimemanager \
    messagecontrol \
    rdknativescript \
    javascriptcore \
    packagemanager \
    lifecyclemanager \
    storagemanager \
    appmanager \
    texttospeechmonitor \
    preinstallmanager \
    migration \
    appgateway \
    appnotifications \
    fbsettings \
    ${@bb.utils.contains('DISTRO_FEATURES', 'DAC-sec',              'ocicontainersec', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'rdkshell',             'rdkshell', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'rdkshell enable_rialto', 'rdkshellrialto', '', d)} \
    ${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' messagecontrol ', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'opencdm', 'opencdmi', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'rialto_in_dac', 'rialtodac', '', d)} \
    downloadmanager \
"

# TODO: As advised, 'ocicointainer' plugin has been modified to build unconditionally. It will be revisited in the upcoming sprint to control it via DISTRO_FEATURES."
#PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'DAC_SUPPORT', 'ocicontainer', '', d)}"

# enable CloudStore plugin for UK region
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_UK', ' cloudstore_eu','',d)}"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_IT', ' cloudstore_eu','',d)}"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_DE', ' cloudstore_eu','',d)}"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_AU', ' cloudstore_eu','',d)}"

# enable CloudStore plugin for US region
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_US', ' cloudstore_us','',d)}"

PACKAGECONFIG:append = " usbaccess"
PACKAGECONFIG:append = " erm"
PACKAGECONFIG:append = " rustadapter "

inherit features_check
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'disable_security_agent', ' -DENABLE_SECURITY_AGENT=OFF ', '  ', d)}"


EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'rdkshell_disable_autostart', ' -DPLUGIN_RDKSHELL_AUTOSTART=false ', ' -DPLUGIN_RDKSHELL_AUTOSTART=true ', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', 'rdkshell_ra second_form_factor', ' -DPLUGIN_RDKSHELL_AUTOSTART=true ', ' ', d)}"

# Enable the RDKShell memcr feature support flags
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'RDKTV_APP_HIBERNATE', ' -DPLUGIN_HIBERNATESUPPORT=ON -DPLUGIN_HIBERNATE_NATIVE_APPS_ON_SUSPENDED=ON','',d)}"

# ----------------------------------------------------------------------------

PACKAGECONFIG[messagecontrol]       = "-DPLUGIN_MESSAGECONTROL=ON,-DPLUGIN_MESSAGECONTROL=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[erm]                  = "-DBUILD_ENABLE_ERM=ON,-DBUILD_ENABLE_ERM=OFF,essos,essos"
PACKAGECONFIG[monitor]              = "-DPLUGIN_MONITOR=ON ${MONITOR_PLUGIN_ARGS},-DPLUGIN_MONITOR=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[ocicontainer]         = "-DPLUGIN_OCICONTAINER=ON, -DPLUGIN_OCICONTAINER=OFF, dobby entservices-apis systemd, dobby entservices-apis systemd"
PACKAGECONFIG[ocicontainersec]      = "                        ,                          ,   omi,   omi"
PACKAGECONFIG[persistent_store]     = "-DPLUGIN_PERSISTENTSTORE=ON,-DPLUGIN_PERSISTENTSTORE=OFF,sqlite3 entservices-apis iarmbus iarmmgrs protobuf,entservices-apis iarmbus"
PACKAGECONFIG[cloudstore_us]        = "-DPLUGIN_CLOUDSTORE=ON -DPLUGIN_CLOUDSTORE_MODE=Local -DPLUGIN_CLOUDSTORE_URI=${CLOUD_STORE_URI},,entservices-apis iarmbus iarmmgrs rfc grpc grpc-native,entservices-apis iarmbus rfc"
PACKAGECONFIG[cloudstore_eu]        = "-DPLUGIN_CLOUDSTORE=ON -DPLUGIN_CLOUDSTORE_MODE=Local -DPLUGIN_CLOUDSTORE_URI=${CLOUD_STORE_URI},,entservices-apis iarmbus iarmmgrs rfc grpc grpc-native,entservices-apis iarmbus rfc"
PACKAGECONFIG[resourcemanager]      = "-DPLUGIN_RESOURCEMANAGER=ON,-DPLUGIN_RESOURCEMANAGER=OFF,"
PACKAGECONFIG[sharedstorage]        = "-DPLUGIN_SHAREDSTORAGE=ON,-DPLUGIN_SHAREDSTORAGE=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"
PACKAGECONFIG[telemetry]            = "-DPLUGIN_TELEMETRY=ON,,iarmbus iarmmgrs entservices-apis rfc rbus,iarmbus entservices-apis rfc rbus"
PACKAGECONFIG[usbaccess]            = "-DPLUGIN_USBACCESS=ON,-DPLUGIN_USBACCESS=OFF,iarmbus iarmmgrs udev,iarmbus udev"
PACKAGECONFIG[usbdevice]         = "-DPLUGIN_USBDEVICE=ON,-DPLUGIN_USBDEVICE=OFF,libusb1"
PACKAGECONFIG[usbmass_storage]         = "-DPLUGIN_USB_MASS_STORAGE=ON,-DPLUGIN_USB_MASS_STORAGE=OFF,"
PACKAGECONFIG[usersettings]         = "-DPLUGIN_USERSETTINGS=ON,-DPLUGIN_USERSETTINGS=OFF,"
PACKAGECONFIG[analytics]            = "-DPLUGIN_ANALYTICS=ON,-DPLUGIN_ANALYTICS=OFF, entservices-apis, entservices-apis"
PACKAGECONFIG[rdkshell]             = "-DPLUGIN_RDKSHELL=ON,-DPLUGIN_RDKSHELL=OFF,rdkshell entservices-apis,rdkshell entservices-apis"
PACKAGECONFIG[rialtodac]            = "-DRIALTO_IN_DAC_FEATURE=ON,-DRIALTO_IN_DAC_FEATURE=OFF,rialto,rialto-servermanager-lib"
PACKAGECONFIG[rdkshellrialto]       = "-DRIALTO_FEATURE=ON,-DRIALTO_FEATURE=OFF,rialto,rialto-servermanager-lib"
PACKAGECONFIG[rustadapter]          = "-DPLUGIN_RUSTADAPTER=OFF,,,"
PACKAGECONFIG[runtimemanager]       = "-DPLUGIN_RUNTIME_MANAGER=ON,-DPLUGIN_RUNTIME_MANAGER=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[rdknativescript]      = "-DPLUGIN_NATIVEJS=ON,-DPLUGIN_NATIVEJS=OFF,rdknativescript,libuv"
PACKAGECONFIG[packagemanager]       = "-DPLUGIN_PACKAGE_MANAGER=ON ${PACKAGEMANAGER_PLUGIN_ARGS} -DLIB_PACKAGE=ON -DSYSROOT_PATH=${STAGING_DIR_TARGET},-DPLUGIN_PACKAGE_MANAGER=OFF -DLIB_PACKAGE=OFF,curl virtual/libpackage,curl virtual/libpackage"
PACKAGECONFIG[lifecyclemanager]     = "-DPLUGIN_LIFECYCLE_MANAGER=ON,-DPLUGIN_LIFECYCLE_MANAGER=OFF,websocketpp entservices-apis,entservices-apis"
PACKAGECONFIG[storagemanager]       = "-DPLUGIN_STORAGE_MANAGER=ON,-DPLUGIN_STORAGE_MANAGER=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[appmanager]           = "-DPLUGIN_APPMANAGER=ON,-DPLUGIN_APPMANAGER=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[opencdmi]             = "-DPLUGIN_OPENCDMI=ON"
PACKAGECONFIG[texttospeechmonitor]  = "-DPLUGIN_MONITOR_TEXTTOSPEECH=ON"
PACKAGECONFIG[preinstallmanager]    = "-DPLUGIN_PREINSTALL_MANAGER=ON,-DPLUGIN_PREINSTALL_MANAGER=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[migration]            = "-DPLUGIN_MIGRATION=ON,-DPLUGIN_MIGRATION=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[appgateway]           = "-DPLUGIN_APPGATEWAY=ON,-DPLUGIN_APPGATEWAY=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[appnotifications]     = "-DPLUGIN_APPNOTIFICATIONS=ON,-DPLUGIN_APPNOTIFICATIONS=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[fbsettings]           = "-DPLUGIN_FBSETTINGS=ON,-DPLUGIN_FBSETTINGS=OFF,entservices-apis,entservices-apis networkmanager-plugin"
PACKAGECONFIG[downloadmanager]      = "-DPLUGIN_DOWNLOADMANAGER=ON -DLIB_PACKAGE=ON -DSYSROOT_PATH=${STAGING_DIR_TARGET},-DPLUGIN_DOWNLOADMANAGER=OFF -DLIB_PACKAGE=OFF,entservices-apis curl virtual/libpackage,entservices-apis curl virtual/libpackage"
# ----------------------------------------------------------------------------

MONITOR_PLUGIN_ARGS                ?= " \
                                       -DPLUGIN_WEBKITBROWSER_MEMORYLIMIT=614400 \
                                       -DPLUGIN_YOUTUBE_MEMORYLIMIT=614400 \
                                       -DPLUGIN_NETFLIX_MEMORYLIMIT=307200 \
                                       -DPLUGIN_MONITOR_CLONED_APPS=ON -DPLUGIN_MONITOR_CLONED_APP_MEMORYLIMIT=657408 \
                                       -DPLUGIN_MONITOR_SEARCH_AND_DISCOVERY_MEMORYLIMIT=888832 \
                                       -DPLUGIN_MONITOR_NETFLIX_APP_MEMORYLIMIT=1048576 \
"
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
