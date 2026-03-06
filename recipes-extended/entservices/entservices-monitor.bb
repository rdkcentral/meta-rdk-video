SUMMARY = "ENTServices Monitor plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=be650d9617f9f9d24bcaccf78a97b28b"

PV = "1.0.1"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRCREV = "8b9a2807ee49403e0f9afecfbc685b30f248b214"
SRC_URI = "${CMF_GITHUB_ROOT}/entservices-monitor;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://0001-Add-monitoring-of-cloned-callsigns.patch \
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
    telemetrysupport \
"

inherit features_check
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'disable_security_agent', ' -DENABLE_SECURITY_AGENT=OFF ', '  ', d)}"


# ----------------------------------------------------------------------------

PACKAGECONFIG[monitor]              = "-DPLUGIN_MONITOR=ON ${MONITOR_PLUGIN_ARGS},-DPLUGIN_MONITOR=OFF,entservices-apis,entservices-apis"
PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"
# ----------------------------------------------------------------------------

MONITOR_PLUGIN_ARGS                ?= " \
                                       -DPLUGIN_WEBKITBROWSER_MEMORYLIMIT=614400 \
                                       -DPLUGIN_YOUTUBE_MEMORYLIMIT=614400 \
                                       -DPLUGIN_NETFLIX_MEMORYLIMIT=307200 \
                                       -DPLUGIN_MONITOR_CLONED_APPS=ON -DPLUGIN_MONITOR_CLONED_APP_MEMORYLIMIT=657408 \
                                       -DPLUGIN_MONITOR_SEARCH_AND_DISCOVERY_MEMORYLIMIT=888832 \
                                       -DPLUGIN_MONITOR_NETFLIX_APP_MEMORYLIMIT=1048576 \
"

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
