DESCRIPTION = "ThunderPluginActivator: command-line tool to activate Thunder plugins"
HOMEPAGE = "https://github.com/rdkcentral/ThunderPluginActivator"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DEPENDS = "cmake-native wpeframework-tools-native wpeframework"

PV = "1.0.0"
PR = "r0"

SRC_URI = "git://github.com/tabbas651/ThunderPluginActivator;protocol=https;branch=main;name=thunderpluginactivator"

SRCREV = "e7b58f6ac67f46db8385cd9c3c4662fadf7181a5"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

FILES:${PN} += "${bindir}/PluginActivator"
