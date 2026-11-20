##
## Copyright (C) 2018 Liberty Global Service B.V.
## Modifications: Copyright 2025 Comcast Cable Communications Management, LLC
## Licensed under the MIT License
##
LICENSE = "Apache-2.0 & MIT & BSD-3-Clause"

require recipes-extended/subtec-app/subtec-repo.inc
PR = "r0"
S = "${WORKDIR}/git/subttxrend-dvbsub"

DEPENDS = "dvbsubdecoder subttxrend-common subttxrend-gfx"

#
# pkgconfig         - pkgconfig used in cmake (adds dependency)
# cmake             - cmake build system used
#

inherit pkgconfig cmake coverity
EXTRA_OECMAKE:append = "-DBUILD_RDK_REFERENCE=1"
