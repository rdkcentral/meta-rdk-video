DESCRIPTION = "ThunderPluginActivator: command-line tool to activate Thunder plugins"
HOMEPAGE = "https://github.com/rdkcentral/ThunderPluginActivator"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DEPENDS = "cmake-native wpeframework-tools-native wpeframework entservices-apis"

PV = "1.2.0"
PR = "r1"

SRC_URI = "git://github.com/rdkcentral/ThunderPluginActivator;protocol=https;branch=PIS_Test_4.4;name=thunderpluginactivator"

SRCREV = "82b887f0fa4f27cf1000a4f1519b2cf2a076029f"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

FILES:${PN} += "${bindir}/PluginActivator"
