SUMMARY = "ENTServices warehouse plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2a944942e1496af1886903d274dedb13"

PV = "1.0.0"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-warehouse;${CMF_GITHUB_SRC_URI_SUFFIX}"

SRCREV = "c961179f0700e75a02d973dcb26cdfb62451ee90"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

EXTRA_OECMAKE += " -DENABLE_RFC_MANAGER=ON"

DEPENDS += "wpeframework wpeframework-tools-native"
RDEPENDS:${PN} += "wpeframework"

TARGET_LDFLAGS += " -Wl,--no-as-needed -ltelemetry_msgsender -Wl,--as-needed "

CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/wdmp-c/ "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/trower-base64/ "
CXXFLAGS += " -DRFC_ENABLED "
CXXFLAGS += " -Wall -Werror "
CXXFLAGS:remove_morty = " -Wall -Werror "
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"

# ----------------------------------------------------------------------------

PACKAGECONFIG ?= " breakpadsupport \
    telemetrysupport \
    ${@bb.utils.contains('DISTRO_FEATURES', 'systimemgr', 'systimemgrsupport', '', d)} \
"

PACKAGECONFIG:append = " warehouse"

PACKAGECONFIG[breakpadsupport]      = ",,breakpad-wrapper,breakpad-wrapper"
PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"
PACKAGECONFIG[warehouse]            = "-DPLUGIN_WAREHOUSE=ON  -DUSE_DEVICESETTINGS=1,-DPLUGIN_WAREHOUSE=OFF,iarmbus iarmmgrs entservices-apis devicesettings virtual/vendor-devicesettings-hal,iarmbus entservices-apis devicesettings"

# ----------------------------------------------------------------------------

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DSECAPI_LIB=sec_api \
"

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
}curl -H 'content-type:text/plain;' --data-binary '{"jsonrpc": "2.0", "id": 42, "method": "org.rdk.DisplaySettings.getAudioFormat"}' http://127.0.0.1:9998/jsonrpccurl -H 'content-type:text/plain;' --data-binary '{"jsonrpc": "2.0", "id": 42, "method": "org.rdk.DisplaySettings.getAudioFormat"}' http://127.0.0.1:9998/jsonrpc

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/*.so ${datadir}/WPEFramework/*"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
