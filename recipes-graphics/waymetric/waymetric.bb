
SUMMARY = "This receipe compiles the waymetric component"

LICENSE = "Apache-2.0"
LICENSE_LOCATION ?= "${S}/LICENSE"
LIC_FILES_CHKSUM = "file://${LICENSE_LOCATION};md5=09b658f7398abbac507b10feada73aac"

PV = "1.0+gitr${SRCPV}"
SRCREV = "66b10fa71cb4dbfc6e5f802da261050c8a2661c9"

SRC_URI = "${CMF_GIT_ROOT}/components/opensource/waymetric;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GIT_MASTER_BRANCH};name=waymetric"
SRCREV_FORMAT = "waymetric"

S = "${WORKDIR}/git"

DEPENDS = "wayland virtual/egl"

INSANE_SKIP:${PN} = "ldflags"

inherit autotools pkgconfig coverity

acpaths = "-I cfg"
