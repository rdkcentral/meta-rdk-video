##
## Copyright (C) 2018 Liberty Global Service B.V.
## Modifications: Copyright 2025 Comcast Cable Communications Management, LLC
## Licensed under the MIT License
##
LICENSE = "Apache-2.0 & MIT & BSD-3-Clause"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=626bbc2ac7625da5b97fcb8a24bd88b3"
PV = "1.5.0"
PR = "r0"
DEPENDS = "glib-2.0 subttxrend-common wayland wayland-protocols wayland-native freetype fontconfig libxkbcommon harfbuzz libpng"
DEPENDS:append = " virtual/egl "

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRCREV = "20fa66c0d5a8a6e06f39588751d76a0dec0974fc"
SRC_URI="${CMF_GITHUB_ROOT}/subtec-app;${CMF_GITHUB_SRC_URI_SUFFIX}"
S = "${WORKDIR}/git/subttxrend-gfx"
#
# pkgconfig         - pkgconfig used in cmake (adds dependency)
# cmake             - cmake build system used
#

inherit pkgconfig cmake coverity

RDEPENDS:subttxrend-gfx += "wayland cpc-fonts "

EXTRA_OECMAKE += "-DWITH_OPENGL=1"
EXTRA_OECMAKE:append = " -DBUILD_RDK_REFERENCE=1"

CXXFLAGS:append:kirkstone = " -fpermissive"

INSANE_SKIP:subttxrend-gfx := "file-rdeps"
