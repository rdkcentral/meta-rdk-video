##
## Copyright (C) 2018 Liberty Global Service B.V.
## Modifications: Copyright 2025 Comcast Cable Communications Management, LLC
## Licensed under the MIT License
##
LICENSE = "Apache-2.0 & MIT & BSD-3-Clause"

require recipes-extended/subtec-app/subtec-repo.inc
PR = "r0"
S = "${WORKDIR}/git/subttxrend-ctrl"

DEPENDS = ""
DEPENDS += " subttxrend-common subttxrend-socksrc subttxrend-dbus"
DEPENDS += " subttxrend-gfx subttxrend-dvbsub subttxrend-ttxt subttxrend-protocol"
DEPENDS += " subttxrend-ttml subttxrend-scte subttxrend-cc subttxrend-webvtt"

#
# pkgconfig         - pkgconfig used in cmake (adds dependency)
# cmake             - cmake build system used
#

inherit pkgconfig cmake coverity
