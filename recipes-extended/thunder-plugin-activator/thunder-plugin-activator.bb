DESCRIPTION = "ThunderPluginActivator: command-line tool to activate Thunder plugins"
HOMEPAGE = "https://github.com/rdkcentral/ThunderPluginActivator"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DEPENDS = "cmake-native wpeframework-tools-native wpeframework entservices-apis"

PV = "1.2.0"
PR = "r1"

SRC_URI = "git://github.com/rdkcentral/ThunderPluginActivator;protocol=https;branch=Thunder44PluginInitializerService;name=thunderpluginactivator"

SRCREV = "f9df0889f31a5ceb6c4f2103b825b298146a0f2f"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

FILES:${PN} += "${bindir}/PluginActivator"
