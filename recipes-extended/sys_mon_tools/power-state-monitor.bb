SUMMARY = "Sys mon tool Power State Monitor recipe"

DESCRIPTION = "Sys mon tool Power State Monitor recipe"

SECTION = "console/utils"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
PV = "1.0.10"
PR = "r0"

SRC_URI = "${CMF_GITHUB_ROOT}/sys_mon_tools;${CMF_GITHUB_SRC_URI_SUFFIX};name=pwr-state-monitor;branch=RDKEMW-14816-power-state-monitor"
SRCREV = "61fec4dc7077f0182074f31a510a8be4e0c5980d"
S = "${WORKDIR}/git"

CFLAGS:append = " -DYOCTO_BUILD"
DEPENDS = "iarmbus iarmmgrs dbus glib-2.0 directfb"
RDEPENDS:${PN} += "iarmmgrs"

inherit autotools pkgconfig coverity

do_install() {

    install -d ${D}${bindir}
    install -m 0755 ${B}/pwr-state-monitor ${D}${bindir}
}

FILES:${PN} += "${bindir}/pwr-state-monitor"
INSANE_SKIP:${PN} += "useless-rpaths"
