SUMMARY = "Teletext Decoder Library"
LICENSE = "Apache-2.0 & MIT & BSD-3-Clause"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=626bbc2ac7625da5b97fcb8a24bd88b3"
PV = "1.6.0"
PR = "r0"
DEPENDS = "subttxrend-common"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRCREV = "dc7e76dd031d5d237e9613d414fb2d7c27a7b5ed"
SRC_URI="${CMF_GITHUB_ROOT}/subtec-app;${CMF_GITHUB_SRC_URI_SUFFIX}"
S = "${WORKDIR}/git/ttxdecoder"

#
# pkgconfig         - pkgconfig used in cmake (adds dependency)
# cmake             - cmake build system used
#

inherit pkgconfig cmake coverity
EXTRA_OECMAKE:append = "-DBUILD_RDK_REFERENCE=1"
