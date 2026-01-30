SUMMARY = "Sys mon tool - SI CACHE PARSER DIAG recipe"

DESCRIPTION = "Sys mon tool - SI CACHE PARSER DIAG recipe"

SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "${RDK_RELEASE}+git${SRCPV}"

SRCREV = "c9b67954d24c2834bfb1e81ca0e7d2238a6c1bd3"

S = "${WORKDIR}/git"

SRC_URI = "${CMF_GIT_ROOT}/rdk/components/generic/sys_mon_tools/si_cache_parser;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GIT_BRANCH}"

DEPENDS = "iarmbus iarmmgrs dbus glib-2.0"

inherit autotools pkgconfig coverity

do_install:append() {
 ln -sf /usr/bin/si_cache_parser_121 ${D}/si_cache_parser_121
}

FILES:${PN} += "/si_cache_parser_121"
