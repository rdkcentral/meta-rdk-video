DESCRIPTION = "ThunderPluginActivator: command-line tool to activate Thunder plugins"
HOMEPAGE = "https://github.com/rdkcentral/ThunderPluginActivator"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DEPENDS = "cmake-native ${THUNDER_NAMESPACE_LC}-tools-native ${THUNDER_NAMESPACE_LC}"
RDEPENDS:${PN} = "${THUNDER_NAMESPACE_LC}"

PV = "1.3.0"
PR = "r2"

SRC_URI = "git://github.com/rdkcentral/ThunderPluginActivator;protocol=https;nobranch=1"

SRCREV = "0332a94cac3378fa3c23d3b106fa2fe642064753"
SRCREV:thunder_5 = "5640ba65a4397e85612ef9e38f50b379beba5f6f"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

FILES:${PN} += "${bindir}/PluginActivator"
