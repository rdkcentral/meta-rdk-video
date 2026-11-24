SUMMARY = "Device Automation Bus Adapter plugin"
LICENSE  = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

inherit pkgconfig cmake features_check

REQUIRED_DISTRO_FEATURES = "enable-dabadapter"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

DEPENDS  = "paho-mqtt-c curl boost fmt libevdev"
DEPENDS += "wpeframework wpeframework-tools-native entservices-apis networkmanager-plugin"
RDEPENDS:${PN} = "mosquitto"

PR = "r0"
PV = "1.0.0"
PV .= "+${@bb.fetch2.get_srcrev(d).replace('AUTOINC+','')}"

SRCREV = "90c1cd440012ea1fbbd591151750f5fa6cb08986"
SRC_URI = "${CMF_GITHUB_ROOT}/entservices-dabadapter;${CMF_GITHUB_SRC_URI_SUFFIX}"

PACKAGECONFIG ?= ""
PACKAGECONFIG[tests] = "-DBUILD_TESTS=ON,-DBUILD_TESTS=OFF,googletest"

S = "${WORKDIR}/git"

FILES:${PN} += "${libdir}/wpeframework/plugins/*.so"
FILES:${PN}-dbg += "${libdir}/wpeframework/plugins/.debug"
