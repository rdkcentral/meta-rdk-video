##
## Copyright (C) 2018 Liberty Global Service B.V.
## Modifications: Copyright 2025 Comcast Cable Communications Management, LLC
## Licensed under the MIT License
##
LICENSE = "Apache-2.0 & MIT & BSD-3-Clause"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=626bbc2ac7625da5b97fcb8a24bd88b3"
PV = "1.5.0"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DEPENDS = ""
DEPENDS += " subttxrend-common subttxrend-socksrc subttxrend-dbus"
DEPENDS += " subttxrend-gfx subttxrend-dvbsub subttxrend-ttxt subttxrend-protocol"
DEPENDS += " subttxrend-ttml subttxrend-scte subttxrend-cc subttxrend-webvtt"

DEPENDS:append = " virtual/egl "

SRC_URI="${CMF_GITHUB_ROOT}/subtec-app;${CMF_GITHUB_SRC_URI_SUFFIX}"
SRCREV:pn-subttxrend-ctrl = "${SRCREV:pn-subttxrend-app}"
S = "${WORKDIR}/git/subttxrend-ctrl"

#
# pkgconfig         - pkgconfig used in cmake (adds dependency)
# cmake             - cmake build system used
#

inherit pkgconfig cmake coverity
