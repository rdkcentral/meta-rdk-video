SUMMARY = "ENTServices UserSettings plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b42edef8fa55315f34f2370b4715ca9"

PV = "1.0.1"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRCREV = "b74cb60d4e5799bc33afb74231f442f7281c4dfc"
SRC_URI = "${CMF_GITHUB_ROOT}/entservices-usersettings;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://rdkservices.ini \
           file://0001-RDKTV-20749-Revert-Merge-pull-request-3336-from-npol.patch \
          "

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'wpe_security_util_disable', ' -DDISABLE_SECURITY_TOKEN=ON', '', d)}"

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

# Enable usersettings plugin from entservices-infra with persistent_store dependency
PACKAGECONFIG ?= " usersettings \
    telemetrysupport \
"

inherit features_check
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'disable_security_agent', ' -DENABLE_SECURITY_AGENT=OFF ', '  ', d)}"

# ----------------------------------------------------------------------------
# Define plugin configurations
PACKAGECONFIG[usersettings]         = "-DPLUGIN_USERSETTINGS=ON,-DPLUGIN_USERSETTINGS=OFF,"
PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"
# ----------------------------------------------------------------------------

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DSECAPI_LIB=sec_api \
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
