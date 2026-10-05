SUMMARY = "ENTServices AppGateway plugins"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=af6837dbae6be5e19b54c9b43888caa3"

PV = "1.6.0.0"
PR = "r0"
# Release version - 1.6.0.0
SRCREV = "9d5218033f464845eea46cd31c2d82ad1b7723d1"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-appgateway;${CMF_GITHUB_SRC_URI_SUFFIX}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"

DEPENDS += "wpeframework wpeframework-tools-native wpeframework-clientlibraries entservices-apis"
RDEPENDS:${PN} += "wpeframework entservices-apis"

TARGET_LDFLAGS += " -Wl,--no-as-needed -ltelemetry_msgsender -Wl,--as-needed "

EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'wpe_security_util_disable', ' -DDISABLE_SECURITY_TOKEN=ON', '', d)}"

RDKAPPMANAGERS_PATH ?= ""
EXTRA_OECMAKE += " -DRDKAPPMANAGERS_PATH=${RDKAPPMANAGERS_PATH}"

PACKAGECONFIG ?= "appgateway appnotifications appgatewaycommon telemetrysupport appactions"

PACKAGECONFIG[appgateway]       = "-DPLUGIN_APPGATEWAY=ON,-DPLUGIN_APPGATEWAY=OFF"
PACKAGECONFIG[appnotifications] = "-DPLUGIN_APPNOTIFICATIONS=ON,-DPLUGIN_APPNOTIFICATIONS=OFF"
PACKAGECONFIG[appgatewaycommon] = "-DPLUGIN_APPGATEWAYCOMMON=ON,-DPLUGIN_APPGATEWAYCOMMON=OFF,networkmanager-plugin"
PACKAGECONFIG[telemetrysupport] = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"
PACKAGECONFIG[appactions]       = "-DPLUGIN_APPACTIONS=ON,-DPLUGIN_APPACTIONS=OFF"


do_install:append() {
    install -d ${D}${sysconfdir}
    echo "APP_GATEWAY_PV = \"${PV}\""     >  ${D}${sysconfdir}/appgatewayversion.txt
    echo "APP_GATEWAY_SHA = \"${SRCREV}\"" >> ${D}${sysconfdir}/appgatewayversion.txt
}

FILES:${PN} += "${libdir}/wpeframework/plugins/*.so"

INSANE_SKIP:${PN} += "dev-so"
