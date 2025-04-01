##############################################################################
# Copyright © 2018 Liberty Global B.V. and its Licensors.
# All rights reserved.
# Licensed by RDK Management, LLC under the terms of the RDK license.
# ============================================================================
# Liberty Global B.V. CONFIDENTIAL AND PROPRIETARY
# ============================================================================
# This file (and its contents) are the intellectual property of Liberty Global B.V.
# It may not be used, copied, distributed or otherwise disclosed in whole or in
# part without the express written permission of Liberty Global B.V.
# The RDK License agreement constitutes express written consent by Liberty Global.
# ============================================================================
# This software is the confidential and proprietary information of Liberty Global B.V.
# ("Confidential Information"). You shall not disclose this source code or
# such Confidential Information and shall use it only in accordance with the
# terms of the license agreement you entered into.
#
# LIBERTY GLOBAL B.V. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
# SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT
# LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
# PARTICULAR PURPOSE, OR NON-INFRINGEMENT. LIBERTY GLOBAL B.V. SHALL NOT BE LIABLE FOR
# ANY DAMAGES SUFFERED BY LICENSEE NOR SHALL THEY BE RESPONSIBLE AS A RESULT
# OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
##############################################################################
LICENSE = "RDK & BSD-3-Clause"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=626bbc2ac7625da5b97fcb8a24bd88b3"
DEPENDS = "glib-2.0"
DEPENDS += " subttxrend-common subttxrend-socksrc subttxrend-dbus"
DEPENDS += " subttxrend-gfx subttxrend-dvbsub subttxrend-ttxt subttxrend-protocol"
DEPENDS += " subttxrend-ttml subttxrend-scte subttxrend-cc subttxrend-webvtt"

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
    install -m 0644 ${S}/conf/subttxrend-app.conf ${D}${sysconfdir}/tmpfiles.d/
    install -d ${D}/${sysconfdir}/subttxrend
    install -m 0755 ${WORKDIR}/config.ini ${D}${sysconfdir}/subttxrend/
}

#
# files to be installed
#

FILES:${PN} += "${sysconfdir}/tmpfiles.d/subttxrend-app.conf"
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

DEPENDS += " subttxrend-webvtt "
DEPENDS += " westeros-simpleshell"
