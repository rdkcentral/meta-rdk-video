SUMMARY = "ENTServices AppGateway plugins"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=9adde9d5cb6e9c095d3e3abf0e9500f1"

PV = "1.0.0.0.rc1"
PR = "r0"
SRCREV = "b30eafea1ad256a9ddc66def44f18ac4debe0204"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

#Announce as firebolt provider
PROVIDES = "virtual/firebolt"
RPROVIDES:${PN} = "virtual/firebolt"

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-appgateway;${CMF_GITHUB_SRC_URI_SUFFIX}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"

DEPENDS += "wpeframework wpeframework-tools-native wpeframework-clientlibraries entservices-apis"
RDEPENDS:${PN} += "wpeframework entservices-apis"

EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'wpe_security_util_disable', ' -DDISABLE_SECURITY_TOKEN=ON', '', d)}"

PACKAGECONFIG ?= "appgateway appnotifications appgatewaycommon telemetrysupport"

PACKAGECONFIG[appgateway]       = "-DPLUGIN_APPGATEWAY=ON,-DPLUGIN_APPGATEWAY=OFF"
PACKAGECONFIG[appnotifications] = "-DPLUGIN_APPNOTIFICATIONS=ON,-DPLUGIN_APPNOTIFICATIONS=OFF"
PACKAGECONFIG[appgatewaycommon] = "-DPLUGIN_APPGATEWAYCOMMON=ON,-DPLUGIN_APPGATEWAYCOMMON=OFF,networkmanager-plugin"
PACKAGECONFIG[telemetrysupport] = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"

FILES:${PN} += "${libdir}/wpeframework/plugins/*.so"

INSANE_SKIP:${PN} += "dev-so"
