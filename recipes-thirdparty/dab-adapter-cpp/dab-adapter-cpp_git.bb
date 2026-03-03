SUMMARY = "Device Automation Bus - C++ Reference"
LICENSE  = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

inherit pkgconfig cmake features_check systemd syslog-ng-config-gen

SYSLOG-NG_FILTER = "rdk-dab-adapter"
SYSLOG-NG_SERVICE_rdk-dab-adapter = "rdk-dab-adapter.service"
SYSLOG-NG_DESTINATION_rdk-dab-adapter = "dab.log"
SYSLOG-NG_LOGRATE_rdk-dab-adapter = "low"

REQUIRED_DISTRO_FEATURES = "enable-dab"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

DEPENDS = "paho-mqtt-c openssl curl boost fmt libevdev"
RRECOMMENDS:${PN} = "mosquitto"

SRCREV = "83d78ff0f1b63b4676fb5839a3f9585a7ff06a0b"

SRC_URI  = "git://github.com/rdk-e/dab-adapter-cpp;protocol=https;branch=topic/RDKEMW-14852"
SRC_URI += "file://rdk-dab-adapter.service.in"

S = "${WORKDIR}/git/generic"

do_install () {
    install -d ${D}${bindir}
    install -m 0755 ${B}/DAB ${D}${bindir}/

    install -d ${D}${systemd_unitdir}/system
    install -m 0755 ${WORKDIR}/rdk-dab-adapter.service.in ${D}${systemd_unitdir}/system/rdk-dab-adapter.service
}

SYSTEMD_SERVICE:${PN} = "rdk-dab-adapter.service"
FILES:${PN} += "${systemd_unitdir}/system/rdk-dab-adapter.service"
