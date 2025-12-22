##
## Copyright (C) 2018 Liberty Global Service B.V.
## Modifications: Copyright 2025 Comcast Cable Communications Management, LLC
## Licensed under the MIT License
##
LICENSE = "Apache-2.0 & MIT & BSD-3-Clause"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=626bbc2ac7625da5b97fcb8a24bd88b3"
PV = "1.5.0"
PR = "r0"
DEPENDS = "dvbsubdecoder subttxrend-common subttxrend-gfx"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRCREV = "72ecd131bbd1ff569a62cb6df3928290e5a570d2"
SRC_URI="${CMF_GITHUB_ROOT}/subtec-app;${CMF_GITHUB_SRC_URI_SUFFIX}"
S = "${WORKDIR}/git/subttxrend-dvbsub"

#
# pkgconfig         - pkgconfig used in cmake (adds dependency)
# cmake             - cmake build system used
#

inherit pkgconfig cmake coverity
EXTRA_OECMAKE:append = "-DBUILD_RDK_REFERENCE=1"
