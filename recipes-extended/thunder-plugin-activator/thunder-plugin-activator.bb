DESCRIPTION = "ThunderPluginActivator: command-line tool to activate Thunder plugins"
HOMEPAGE = "https://github.com/rdkcentral/ThunderPluginActivator"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DEPENDS = "cmake-native wpeframework-tools-native wpeframework wpeframework-extensions"
RDEPENDS:${PN} = "wpeframework wpeframework-extensions"

PV = "1.2.0"
PR = "r1"

SRC_URI = "git://github.com/rdkcentral/ThunderPluginActivator;protocol=https;branch=Thunder_44_PIS;name=thunderpluginactivator"

SRCREV = "0332a94cac3378fa3c23d3b106fa2fe642064753"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

FILES:${PN} += "${bindir}/PluginActivator"
