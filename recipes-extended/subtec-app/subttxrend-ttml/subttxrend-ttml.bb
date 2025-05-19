##
## Copyright (C) 2018 Liberty Global Service B.V.
## Modifications: Copyright 2025 Comcast Cable Communications Management, LLC
## Licensed under the MIT License
##
LICENSE = "Apache-2.0 & MIT & BSD-3-Clause"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=626bbc2ac7625da5b97fcb8a24bd88b3"
DEPENDS = "subttxrend-common libxml2 subttxrend-gfx"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI="${CMF_GITHUB_ROOT}/subtec-app;${CMF_GITHUB_SRC_URI_SUFFIX};branch=topic/RDKEMW-4413"
S = "${WORKDIR}/git/subttxrend-ttml"

#
# pkgconfig         - pkgconfig used in cmake (adds dependency)
# cmake             - cmake build system used
#

inherit pkgconfig cmake coverity

EXTRA_OECMAKE:append = "-DBUILD_RDK_REFERENCE=1"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "${@ 'file://006-subttxrend-ttml-hardcode-center-textalign.patch' if (d.getVar('BUILTIN_PARTNER_ID',True) == "sky-it") else 'file://006-subttxrend-ttml-hardcode-center-textalign.patch' if (d.getVar('TARGET_REGION',True) == "IT") else ""}"

