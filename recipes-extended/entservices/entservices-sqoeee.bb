SUMMARY = "ENTServices SQoEEE playback QoE reporting plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://CMakeLists.txt;beginline=1;endline=17;md5=589ec5d109f76803240c0c10664f8352"

PV = "1.0.0"
PR = "r1"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-sqoeee;${CMF_GITHUB_SRC_URI_SUFFIX}"
SRCREV = "ea42dd7c835a4b8bc41760343f3e3266e1fae5a9"

inherit cmake pkgconfig

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"

DEPENDS = "wpeframework wpeframework-tools-native wpeframework-clientlibraries entservices-apis"
RDEPENDS:${PN} += "wpeframework entservices-apis"

PLUGIN_SQOEEE_MODE ?= "Off"
PLUGIN_SQOEEE_STARTMODE ?= "${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'Deactivated', 'Activated', d)}"

EXTRA_OECMAKE += " \
    -DBUILD_TESTING=OFF \
    -DPLUGIN_SQOEEE_MODE=${PLUGIN_SQOEEE_MODE} \
    -DPLUGIN_SQOEEE_STARTMODE=${PLUGIN_SQOEEE_STARTMODE} \
"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${datadir}/WPEFramework/*"
