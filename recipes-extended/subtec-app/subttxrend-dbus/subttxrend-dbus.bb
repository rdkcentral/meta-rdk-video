##
## Copyright (C) 2018 Liberty Global Service B.V.
## Modifications: Copyright 2025 Comcast Cable Communications Management, LLC
## Licensed under the MIT License
##
LICENSE = "Apache-2.0 & MIT & BSD-3-Clause"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=626bbc2ac7625da5b97fcb8a24bd88b3"
PV = "1.8.0"
PR = "r0"
DEPENDS = "glib-2.0-native glib-2.0 jansson subttxrend-common"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRCREV = "3949db67e6dc141d1ada9f0f610cc493133d1f41"
SRC_URI="${CMF_GITHUB_ROOT}/subtec-app;${CMF_GITHUB_SRC_URI_SUFFIX}"
S = "${WORKDIR}/git/subttxrend-dbus"

#
# pkgconfig         - pkgconfig used in cmake (adds dependency)
# cmake             - cmake build system used
#

inherit pkgconfig cmake coverity
EXTRA_OECMAKE:append = "-DBUILD_RDK_REFERENCE=1"
