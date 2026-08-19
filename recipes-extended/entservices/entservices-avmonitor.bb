# This software is the confidential and proprietary information of
#
# RDK Management, LLC (RDKM) ("Confidential Information").
#
# Access to RDKM’s source code is conditional on the acceptance of,
# and continued compliance with, the terms of the Software License Agreement
# and the NondisclosureAgreement between you (the “Licensee”) and RDKM.
#
# You shall not disclose this source code or such Confidential Information.

SUMMARY = "Plugin component for AVMonitor - playback observability tool"
HOMEPAGE = "https://github.com/FireboltConnectApps/ThunderPluginAVMonitor"
SECTION = "rdk"
LICENSE = "CLOSED"

inherit cmake pkgconfig

TOOLCHAIN = "gcc"

DEPENDS = "wpeframework entservices-apis wpeframework-tools-native"

SRC_URI = "file://ThunderPluginAVMonitor-main.zip"
S = "${WORKDIR}/ThunderPluginAVMonitor-main"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

PACKAGECONFIG[autostart] = "-DPLUGIN_AVMONITOR_AUTOSTART=true,-DPLUGIN_AVMONITOR_AUTOSTART=false,,"
PLUGIN_AVMONITOR_SINK_STATISTICS_FREQUENCY ?= "5"

PACKAGECONFIG ?= " \
    sharedlibs \
"
PACKAGECONFIG[debug] = "-DCMAKE_BUILD_TYPE=Debug,-DCMAKE_BUILD_TYPE=Release,"
PACKAGECONFIG[sharedlibs] = "-DBUILD_SHARED_LIBS=ON,,"

EXTRA_OECMAKE = " \
    -DCMAKE_SYSROOT=${STAGING_DIR_HOST} \
    -DBUILD_REFERENCE=${SRCREV} \
    -DPLUGIN_AVMONITOR_SINK_STATISTICS_FREQUENCY=${PLUGIN_AVMONITOR_SINK_STATISTICS_FREQUENCY} \
"

FILES_SOLIBSDEV = ""

FILES:${PN} += "${libdir}/*"
FILES:${PN} += "${datadir}/WPEFramework/*"
FILES:${PN} += "${includedir}/WPEFramework/*"
FILES:${PN}-dev += "${libdir}/cmake/*"