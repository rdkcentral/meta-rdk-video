SUMMARY = "ENTServices Monitor plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=be650d9617f9f9d24bcaccf78a97b28b"

PV = "1.1.0"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRCREV = "fdaaa727c8d8c272fc76d610b10a36410fdf7cd8"
SRC_URI = "${CMF_GITHUB_ROOT}/entservices-monitor;protocol=${CMF_GITHUB_PROTOCOL};branch=main"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

DEPENDS += "wpeframework wpeframework-tools-native entservices-apis"
RDEPENDS:${PN} += "wpeframework"

CXXFLAGS += " -Wall -Werror "

# ----------------------------------------------------------------------------

PACKAGECONFIG ?= "telemetrysupport"

PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"

# ----------------------------------------------------------------------------

MONITOR_PLUGIN_ARGS                ?= " \
                                       -DPLUGIN_MONITOR_WEBKITBROWSER_MEMORYLIMIT=614400 \
                                       -DPLUGIN_MONITOR_WEBKITBROWSER_YOUTUBE_MEMORYLIMIT=614400 \
                                       -DPLUGIN_MONITOR_NETFLIX_MEMORYLIMIT=307200 \
                                       -DPLUGIN_MONITOR_CLONED_APPS=ON -DPLUGIN_MONITOR_CLONED_APP_MEMORYLIMIT=657408 \
                                       -DPLUGIN_MONITOR_SEARCH_AND_DISCOVERY_MEMORYLIMIT=888832 \
                                       -DPLUGIN_MONITOR_NETFLIX_APP_MEMORYLIMIT=1048576 \
"

# Can be extended from bbappend - space-separated list of plugins to monitor, as: callsign,memory-interval,memory-limit,operational,restart-window,restart-limit
PLUGIN_MONITOR_INSTANCES_LIST = ""
# Monitor TextToSpeech
PLUGIN_MONITOR_INSTANCES_LIST += "org.rdk.TextToSpeech,0,0,1,60,3"

# Monitor DeviceSettings
PLUGIN_MONITOR_INSTANCES_LIST += "org.rdk.DeviceSettings,0,0,1,60,3"

EXTRA_OECMAKE += " \
    ${MONITOR_PLUGIN_ARGS} \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DPLUGIN_MONITOR_INSTANCES_LIST='${PLUGIN_MONITOR_INSTANCES_LIST}' \
"

do_install:append() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'true', 'false', d)} == 'true'; then
        if [ -d "${D}/etc/WPEFramework/plugins" ]; then
            find ${D}/etc/WPEFramework/plugins/ -type f | xargs sed -i -r 's/"autostart"[[:space:]]*:[[:space:]]*true/"autostart":false/g'
        fi
    fi
}

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so"
