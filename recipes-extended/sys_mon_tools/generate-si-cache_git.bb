SUMMARY = "Sys mon tool - GENERATE SI CACHE recipe"

DESCRIPTION = "Sys mon tool - GENERATE SI CACHE recipe"

SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "${RDK_RELEASE}+git${SRCPV}"

SRCREV = "c951c66e17fed2d63e6c6b62a0ae993fe5068c2a"

SRC_URI = "${CMF_GIT_ROOT}/rdk/components/generic/rmf_tools/generate_si_cache;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GIT_BRANCH}"
S = "${WORKDIR}/git"

DEPENDS = "iarmbus iarmmgrs dbus glib-2.0 libxml2"

inherit autotools pkgconfig coverity

do_install:append() {
 ln -sf /usr/bin/generate_si_cache ${D}/generate_si_cache
}

FILES:${PN} += "/generate_si_cache"
