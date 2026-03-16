DESCRIPTION = "ThunderPluginActivator: command-line tool to activate Thunder plugins"
HOMEPAGE = "https://github.com/rdkcentral/ThunderPluginActivator"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DEPENDS = "cmake-native wpeframework-tools-native wpeframework"
RDEPENDS:${PN} = "wpeframework"

PV = "1.2.0"
PR = "r1"

SRC_URI = "git://github.com/dnnaveen151027/ThunderPluginActivator;protocol=https;branch=main;name=thunderpluginactivator"

SRCREV = "5e6c5e417b26993f2521a04e967a627a67ce4b47"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

FILES:${PN} += "${bindir}/PluginActivator"
