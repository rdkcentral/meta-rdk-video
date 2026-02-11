SUMMARY = "ENTServices displayinfo plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

PV = "1.0.1"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-displayinfo;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://0001-RDKTV-20749-Revert-Merge-pull-request-3336-from-npol.patch \
          "

# Release version - 1.0.1
SRCREV = "2c2c0c97eee4a5a046b4bc7f2354827eb0d56067"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

EXTRA_OECMAKE += " -DENABLE_RFC_MANAGER=ON"





EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'link_localtime', ' -DBUILD_ENABLE_LINK_LOCALTIME=ON', '',d)}"

DEPENDS += "wpeframework wpeframework-tools-native"

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

# ----------------------------------------------------------------------------

PACKAGECONFIG ?= " breakpadsupport \
    telemetrysupport \
"
PACKAGECONFIG:append = "displayinfo"
PACKAGECONFIG[breakpadsupport]      = ",,breakpad-wrapper,breakpad-wrapper"
PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"
PACKAGECONFIG[displayinfo]          = "-DPLUGIN_DISPLAYINFO=ON  -DUSE_DEVICESETTINGS=1,-DPLUGIN_DISPLAYINFO=OFF,iarmbus iarmmgrs drm entservices-apis devicesettings virtual/vendor-devicesettings-hal virtual/vendor-displayinfo-soc,iarmbus libdrm entservices-apis devicesettings virtual/vendor-displayinfo-soc"

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
