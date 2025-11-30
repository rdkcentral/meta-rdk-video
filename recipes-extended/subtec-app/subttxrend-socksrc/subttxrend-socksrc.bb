##
## Copyright (C) 2018 Liberty Global Service B.V.
## Modifications: Copyright 2025 Comcast Cable Communications Management, LLC
## Licensed under the MIT License
##
LICENSE = "Apache-2.0 & MIT & BSD-3-Clause"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=626bbc2ac7625da5b97fcb8a24bd88b3"
PV = "1.5.0"
PR = "r0"
DEPENDS = "glib-2.0 subttxrend-common subttxrend-protocol"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI="${CMF_GITHUB_ROOT}/subtec-app;${CMF_GITHUB_SRC_URI_SUFFIX}"
SRCREV:pn-subttxrend-socksrc = "${SRCREV:pn-subttxrend-app}"
S = "${WORKDIR}/git/subttxrend-socksrc"

#
# onemwsrc          - external source folder used
# pkgconfig         - pkgconfig used in cmake (adds dependency)
# cmake             - cmake build system used
#

inherit pkgconfig cmake coverity
EXTRA_OECMAKE:append = "-DBUILD_RDK_REFERENCE=1"
