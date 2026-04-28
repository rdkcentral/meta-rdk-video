SUMMARY = "ENTServices AppGateway plugins"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=9adde9d5cb6e9c095d3e3abf0e9500f1"

PV = "1.1.0.2"
PR = "r0"
# Release version - 1.1.0.2
SRCREV = "ba37bae07608d29c52e5db2b2310dd7680dca815"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-appgateway;${CMF_GITHUB_SRC_URI_SUFFIX}"
SRC_URI += "file://0100-RDKEMW-17100-Firebolt-fixes.patch"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"

DEPENDS += "wpeframework wpeframework-tools-native wpeframework-clientlibraries entservices-apis"
RDEPENDS:${PN} += "wpeframework entservices-apis"

TARGET_LDFLAGS += " -Wl,--no-as-needed -ltelemetry_msgsender -Wl,--as-needed "

EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'wpe_security_util_disable', ' -DDISABLE_SECURITY_TOKEN=ON', '', d)}"

PACKAGECONFIG ?= "appgateway appnotifications appgatewaycommon telemetrysupport"

PACKAGECONFIG[appgateway]       = "-DPLUGIN_APPGATEWAY=ON,-DPLUGIN_APPGATEWAY=OFF"
PACKAGECONFIG[appnotifications] = "-DPLUGIN_APPNOTIFICATIONS=ON,-DPLUGIN_APPNOTIFICATIONS=OFF"
PACKAGECONFIG[appgatewaycommon] = "-DPLUGIN_APPGATEWAYCOMMON=ON,-DPLUGIN_APPGATEWAYCOMMON=OFF,networkmanager-plugin"
PACKAGECONFIG[telemetrysupport] = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"

do_install:append() {
    install -d ${D}${sysconfdir}
    echo "APP_GATEWAY_PV = \"${PV}\""     >  ${D}${sysconfdir}/appgatewayversion.txt
    echo "APP_GATEWAY_SHA = \"${SRCREV}\"" >> ${D}${sysconfdir}/appgatewayversion.txt
}

FILES:${PN} += "${libdir}/wpeframework/plugins/*.so"

INSANE_SKIP:${PN} += "dev-so"
