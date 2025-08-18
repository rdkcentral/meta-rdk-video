FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

SUMMARY = "This recipe compiles Power State Monitor code base"
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

SRC_URI = "${CMF_GIT_ROOT}/rdk/components/generic/sys_mon_tools/power-state-monitor;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GIT_BRANCH}"

PV = "${RDK_RELEASE}+git${SRCPV}"
SRCREV = "519715a7690643163b8b973904d8351424906478"
S = "${WORKDIR}/git"

DEPENDS = "iarmbus glib-2.0"

export LINK = "${LD}"

CFLAGS += " -I=${includedir}/rdk/iarmbus \
        -I=${includedir}/rdk/iarmmgrs-hal \
        -I=${includedir}/directfb \
        -I=${libdir}/glib-2.0/include \
        -I=${includedir}/glib-2.0 "

export GLIBS = "-lglib-2.0 -lz"

LDFLAGS += "-Wl,-O1"

export USE_DBUS = "y"

inherit coverity

do_compile() {
        oe_runmake -B -C ${S}/src
}

do_install() {

    install -d ${D}${bindir}
    install -m 0755 ${S}/src/pwr-state-monitor ${D}${bindir}
}

