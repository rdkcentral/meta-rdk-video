SUMMARY = "ENTServices deviceanddisplay plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=dc6e390ad71aef79d0c2caf3cde03a19"

PV ?= "3.2.5"
PR ?= "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-deviceanddisplay;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://0001-RDKTV-20749-Revert-Merge-pull-request-3336-from-npol.patch \
          "

# Release version - 3.2.9
SRCREV = "3c7be53d6ae59960314ca32ee9021ba338636ea7"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

EXTRA_OECMAKE += " -DENABLE_RFC_MANAGER=ON"
EXTRA_OECMAKE += " -DBUILD_ENABLE_THERMAL_PROTECTION=ON "
EXTRA_OECMAKE += "-DDISABLE_GEOGRAPHY_TIMEZONE=ON"
EXTRA_OECMAKE += " -DENABLE_SYSTEM_GET_STORE_DEMO_LINK=ON "
EXTRA_OECMAKE += " -DBUILD_ENABLE_DEVICE_MANUFACTURER_INFO=ON "
EXTRA_OECMAKE += " -DBUILD_ENABLE_APP_CONTROL_AUDIOPORT_INIT=ON "
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'link_localtime', ' -DBUILD_ENABLE_LINK_LOCALTIME=ON', '',d)}"

DEPENDS += "power-manager-headers wpeframework wpeframework-tools-native"
RDEPENDS:${PN} += "wpeframework"

TARGET_LDFLAGS += " -Wl,--no-as-needed -ltelemetry_msgsender -Wl,--as-needed "

CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/wdmp-c/ "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/trower-base64/ "
CXXFLAGS += " -DRFC_ENABLED "
# enable filtering for undefined interfaces and link local ip address notifications
CXXFLAGS += " -DNET_DEFINED_INTERFACES_ONLY -DNET_NO_LINK_LOCAL_ANNOUNCE "
CXXFLAGS += " -Wall -Werror "
CXXFLAGS:remove_morty = " -Wall -Werror "
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"

INCLUDE_DIRS = " \
    -I=${includedir}/rdk/halif/power-manager \
    -I=${includedir}/WPEFramework/powercontroller \
    "

CXXFLAGS += " -DPLATCO_BOOTTO_STANDBY"
CXXFLAGS += " -DOFFLINE_MAINT_REBOOT"

CFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_STB', ' -DMFR_TEMP_CLOCK_READ ', '', d)} "
CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_STB', ' -DMFR_TEMP_CLOCK_READ ', '', d)} "

# ----------------------------------------------------------------------------

PACKAGECONFIG ?= " breakpadsupport \
    telemetrysupport \
    devicediagnostics \
    displaysettings framerate \
    systemservices userpreferences warehouse powermanager \
    ${@bb.utils.contains('DISTRO_FEATURES', 'systimemgr', 'systimemgrsupport', '', d)} \
"

DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"

PACKAGECONFIG:append = " displayinfo deviceinfo systemmode"
PACKAGECONFIG:append = " erm"

PACKAGECONFIG[breakpadsupport]      = ",,breakpad-wrapper,breakpad-wrapper"
PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"
PACKAGECONFIG[devicediagnostics]    = "-DPLUGIN_DEVICEDIAGNOSTICS=ON,-DPLUGIN_DEVICEDIAGNOSTICS=OFF,curl entservices-apis,curl entservices-apis"
PACKAGECONFIG[deviceinfo]           = "-DPLUGIN_DEVICEINFO=ON,-DPLUGIN_DEVICEINFO=OFF,iarmbus iarmmgrs rfc devicesettings virtual/vendor-devicesettings-hal entservices-apis,iarmbus rfc devicesettings entservices-apis virtual/mfrlib,virtual/mfrlib""
PACKAGECONFIG[displayinfo]          = "-DPLUGIN_DISPLAYINFO=ON  -DUSE_DEVICESETTINGS=1,-DPLUGIN_DISPLAYINFO=OFF,iarmbus iarmmgrs drm entservices-apis devicesettings virtual/vendor-devicesettings-hal virtual/vendor-displayinfo-soc,iarmbus libdrm entservices-apis devicesettings virtual/vendor-displayinfo-soc"
PACKAGECONFIG[displaysettings]      = "-DPLUGIN_DISPLAYSETTINGS=ON,-DPLUGIN_DISPLAYSETTINGS=OFF,iarmbus iarmmgrs rfc devicesettings virtual/vendor-devicesettings-hal,iarmbus rfc devicesettings"
PACKAGECONFIG[erm]                  = "-DBUILD_ENABLE_ERM=ON,-DBUILD_ENABLE_ERM=OFF,essos,essos"
PACKAGECONFIG[framerate]            = "-DPLUGIN_FRAMERATE=ON,-DPLUGIN_FRAMERATE=OFF,iarmbus iarmmgrs devicesettings virtual/vendor-devicesettings-hal,iarmbus devicesettings"
PACKAGECONFIG[userpreferences]      = "-DPLUGIN_USERPREFERENCES=ON,-DPLUGIN_USERPREFERENCES=OFF,glib-2.0,glib-2.0"
PACKAGECONFIG[systemservices]       = "-DPLUGIN_SYSTEMSERVICES=ON,-DPLUGIN_SYSTEMSERVICES=OFF,iarmbus iarmmgrs rfc devicesettings virtual/vendor-devicesettings-hal curl procps entservices-apis,tzcode iarmbus rfc devicesettings curl procps entservices-apis"
PACKAGECONFIG[systimemgrsupport]    = "-DBUILD_ENABLE_SYSTIMEMGR_SUPPORT=ON,,systimemgrinetrface,"
PACKAGECONFIG[warehouse]            = "-DPLUGIN_WAREHOUSE=ON,-DPLUGIN_WAREHOUSE=OFF,iarmbus iarmmgrs rfc entservices-apis devicesettings virtual/vendor-devicesettings-hal,iarmbus rfc entservices-apis devicesettings"
PACKAGECONFIG[powermanager]         = "-DPLUGIN_POWERMANAGER=ON,-DPLUGIN_POWERMANAGER=OFF,iarmbus virtual/vendor-deepsleepmgr-hal virtual/vendor-pwrmgr-hal virtual/mfrlib,virtual/mfrlib"
PACKAGECONFIG[systemmode] = "-DPLUGIN_SYSTEMMODE=ON,-DPLUGIN_SYSTEMMODE=OFF,"

# ----------------------------------------------------------------------------

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DSECAPI_LIB=sec_api \
"

# Check if DisplayInfo backend is defined.
python () {
    machine_name = d.getVar('MACHINE')
    if 'raspberrypi4' in machine_name:
        d.appendVar('EXTRA_OECMAKE', ' -DBUILD_RPI=ON')
}

do_install:append() {
    install -d ${D}${sysconfdir}/rfcdefaults
    if ${@bb.utils.contains_any("DISTRO_FEATURES", "rdkshell_ra second_form_factor", "true", "false", d)}
    then
      install -m 0644 ${WORKDIR}/rdkservices.ini ${D}${sysconfdir}/rfcdefaults/
    fi

        install -m 0644 ${THISDIR}/files/displaysettings.ini ${D}${sysconfdir}/rfcdefaults/
    if ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'true', 'false', d)} == 'true'; then
        if [ -d "${D}/etc/WPEFramework/plugins" ]; then
            find ${D}/etc/WPEFramework/plugins/ -type f ! -name "PowerManager.json" | xargs sed -i -r 's/"autostart"[[:space:]]*:[[:space:]]*true/"autostart":false/g'
        fi
    fi
}

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/*.so ${datadir}/WPEFramework/*"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
