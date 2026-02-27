SUMMARY = "ENTServices inputoutput plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=dc6e390ad71aef79d0c2caf3cde03a19"

PV = "1.10.3"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-inputoutput;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://0001-RDKTV-20749-Revert-Merge-pull-request-3336-from-npol.patch \
          "

# Release version - 1.10.3
SRCREV = "3044567e2f2007255eeb5807adbdadff26f9ba77"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV', "tvsettings-hal-headers ", "", d)}"
DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV', "virtual/vendor-tvsettings-hal ", "", d)}"
DEPENDS += "wpeframework wpeframework-tools-native entservices-apis"
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

PACKAGECONFIG ?= " breakpadsupport \
    telemetrysupport \
    avinput \
    hdcpprofile \
    hdmicecsource \
    hdmiinput \
"

PACKAGECONFIG:append = " hdmicecsink "

PACKAGECONFIG[breakpadsupport]      = ",,breakpad-wrapper,breakpad-wrapper"
PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"
PACKAGECONFIG[avinput]              = "-DPLUGIN_AVINPUT=ON,-DPLUGIN_AVINPUT=OFF,iarmbus iarmmgrs devicesettings virtual/vendor-devicesettings-hal,iarmbus devicesettings"
PACKAGECONFIG[compositeinput]       = "-DPLUGIN_COMPOSITEINPUT=ON,-DPLUGIN_COMPOSITEINPUT=OFF,iarmbus iarmmgrs devicesettings virtual/vendor-devicesettings-hal,iarmbus devicesettings"
PACKAGECONFIG[hdcpprofile]          = "-DPLUGIN_HDCPPROFILE=ON,-DPLUGIN_HDCPPROFILE=OFF,iarmbus iarmmgrs devicesettings virtual/vendor-devicesettings-hal,iarmbus devicesettings"
PACKAGECONFIG[hdmicec2]             = "-DPLUGIN_HDMICEC2=ON,-DPLUGIN_HDMICEC2=OFF,iarmbus iarmmgrs devicesettings virtual/vendor-devicesettings-hal hdmicec hdmicecheader,iarmbus devicesettings hdmicec"
PACKAGECONFIG[hdmicecsink]          = "-DPLUGIN_HDMICECSINK=ON,-DPLUGIN_HDMICECSINK=OFF,iarmbus iarmmgrs devicesettings virtual/vendor-devicesettings-hal hdmicec hdmicecheader,iarmbus devicesettings hdmicec"
PACKAGECONFIG[hdmicecsource]        = "-DPLUGIN_HDMICECSOURCE=ON,-DPLUGIN_HDMICECSOURCE=OFF,iarmbus iarmmgrs devicesettings virtual/vendor-devicesettings-hal hdmicec hdmicecheader,iarmbus devicesettings hdmicec"
PACKAGECONFIG[hdmiinput]            = "-DPLUGIN_HDMIINPUT=ON,-DPLUGIN_HDMIINPUT=OFF,iarmbus iarmmgrs devicesettings virtual/vendor-devicesettings-hal,iarmbus devicesettings"

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

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/*.so ${datadir}/WPEFramework/*"
INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
