SUMMARY = "Plugin component for AVMonitor - playback observability tool"
HOMEPAGE = "https://github.com/FireboltConnectApps/ThunderClientLibraryAVMonitor"
SECTION = "rdk"
LICENSE = "CLOSED"

PN = "wpeframework-clientlibraries-avm"

FILESEXTRAPATHS:prepend := "${THISDIR}/wpeframework-clientlibraries:"

inherit cmake pkgconfig

TOOLCHAIN = "gcc"

DEPENDS = "wpeframework entservices-apis wpeframework-tools-native"
RDEPENDS:${PN}:append += " wpeframework"



SRC_URI = "file://r4.4/ThunderClientLibraryAVMonitor-main"
S = "${WORKDIR}/r4.4/ThunderClientLibraryAVMonitor-main"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

PACKAGECONFIG[debug] = "-DCMAKE_BUILD_TYPE=Debug,-DCMAKE_BUILD_TYPE=Release,"

EXTRA_OECMAKE = " \
    -DBUILD_SHARED_LIBS=ON \
    -DCMAKE_SYSROOT=${STAGING_DIR_HOST} \
    -DBUILD_REFERENCE=${SRCREV} \
"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so"
FILES:${PN} += "${datadir}/WPEFramework/*"
FILES:${PN} += "${PKG_CONFIG_DIR}/*.pc"
ASNEEDED = ""

INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"
