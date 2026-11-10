SUMMARY = "Teletext Decoder Library"
LICENSE = "Apache-2.0 & MIT & BSD-3-Clause"

require recipes-extended/subtec-app/subtec-repo.inc
PR = "r0"
S = "${WORKDIR}/git/ttxdecoder"

DEPENDS = "subttxrend-common"

#
# pkgconfig         - pkgconfig used in cmake (adds dependency)
# cmake             - cmake build system used
#

inherit pkgconfig cmake coverity
EXTRA_OECMAKE:append = "-DBUILD_RDK_REFERENCE=1"
