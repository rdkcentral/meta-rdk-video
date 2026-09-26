SUMMARY = "ENTServices ResourceMonitor plugin - CPU/RAM usage monitoring tool"
HOMEPAGE = "https://github.com/FireboltConnectApps/ThunderPluginResourceMonitor"
SECTION = "rdk"
LICENSE = "CLOSED"

PV = "1.0"
PR = "r0"

inherit cmake pkgconfig

SRC_URI = "file://ThunderPluginResourceMonitor-main.zip"
S = "${WORKDIR}/ThunderPluginResourceMonitor-main"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"

DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

DEPENDS += "wpeframework wpeframework-tools-native entservices-apis"
RDEPENDS:${PN} += "wpeframework"

# ----------------------------------------------------------------------------

PACKAGECONFIG ?= ""

PACKAGECONFIG[autostart] = "-DPLUGIN_RESOURCEMONITOR_AUTOSTART=true,-DPLUGIN_RESOURCEMONITOR_AUTOSTART=false,,"

# ----------------------------------------------------------------------------

EXTRA_OECMAKE += " \
    -DCMAKE_SYSROOT=${STAGING_DIR_HOST} \
    -DBUILD_REFERENCE=3b7bf1e9826bda2e8e32aa1c6c0724bc1ec83a65 \
    -DBUILD_SHARED_LIBS=ON \
"

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so"
FILES:${PN} += "${datadir}/WPEFramework/*"
FILES:${PN}-dev += "${libdir}/cmake/*"