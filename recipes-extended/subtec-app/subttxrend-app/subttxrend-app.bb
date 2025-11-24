##
## Copyright (C) 2018 Liberty Global Service B.V.
## Modifications: Copyright 2025 Comcast Cable Communications Management, LLC
## Licensed under the MIT License
##
LICENSE = "Apache-2.0 & MIT & BSD-3-Clause"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=626bbc2ac7625da5b97fcb8a24bd88b3"
DEPENDS = "glib-2.0"
DEPENDS += " subttxrend-ctrl"

DEPENDS:append = " virtual/egl "

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI="${CMF_GITHUB_ROOT}/subtec-app;${CMF_GITHUB_SRC_URI_SUFFIX}"
S = "${WORKDIR}/git/subttxrend-app"

#
# pkgconfig         - pkgconfig used in cmake (adds dependency)
# cmake             - cmake build system used
#

inherit pkgconfig cmake coverity

do_install:append() {
    install -d ${D}${sysconfdir}/tmpfiles.d
    install -d ${D}/${sysconfdir}/subttxrend
    install -m 0755 ${WORKDIR}/config.ini ${D}${sysconfdir}/subttxrend/
}

#
# files to be installed
#

FILES:${PN} += "${sysconfdir}/subttxrend/config.ini"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# Determine if we're actually going to install the service files (texttrack not present) or not
OVERRIDES .= ":${@bb.utils.contains('DISTRO_FEATURES', 'texttrack', '', 'service', d) }"

SRC_URI:append:service = " file://subttxrend-app.service"
SRC_URI:append:service = " file://wait-for-subttxrend-socket.sh"
SRC_URI:append = " file://config.ini"

EXTRA_OECMAKE += "-DINSTALL_CONFIG_FILE=0"
EXTRA_OECMAKE:append = "-DBUILD_RDK_REFERENCE=1"

do_install:append:service() {
    install -D -m 0644 ${WORKDIR}/subttxrend-app.service ${D}${systemd_system_unitdir}/subttxrend-app.service
    install -m 0755 ${WORKDIR}/wait-for-subttxrend-socket.sh ${D}${bindir}
}

inherit systemd syslog-ng-config-gen

SYSLOG-NG_FILTER = "subttxrend-app"
SYSLOG-NG_SERVICE:subttxrend-app = "subttxrend-app.service"
SYSLOG-NG_DESTINATION:subttxrend-app = "subttxrend-app.log"
SYSLOG-NG_LOGRATE:subttxrend-app = "very-high"

SYSTEMD_SERVICE:${PN}:service = "subttxrend-app.service"
SYSTEMD_AUTO_ENABLE = "disable"
SYSTEMD_AUTO_ENABLE:service = "enable"

FILES:${PN}:append:service = "${systemd_system_unitdir}/subttxrend-app.service"

DEPENDS += " westeros-simpleshell"
SRCREV = "72ecd131bbd1ff569a62cb6df3928290e5a570d2"
PV = "1.5.0"
PR = "r0"
