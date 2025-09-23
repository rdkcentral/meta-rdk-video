DESCRIPTION = "ThunderPluginActivator: command-line tool to activate Thunder plugins"
HOMEPAGE = "https://github.com/rdkcentral/ThunderPluginActivator"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DEPENDS = "cmake-native wpeframework-tools-native wpeframework"

PV = "1.1.0"
PR = "r0"

SRC_URI = "git://github.com/rdkcentral/ThunderPluginActivator;protocol=https;branch=main;name=thunderpluginactivator"

SRCREV = "1f0fd5618965b72850906f96762ec9f5f5704517"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

FILES:${PN} += "${bindir}/PluginActivator"
