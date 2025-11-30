SUMMARY = "Sys mon tool key simulator recipe"

DESCRIPTION = "Sys mon tool key simulator recipe"

SECTION = "console/utils"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "1.0.8"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI = "${CMF_GITHUB_ROOT}/sys_mon_tools;${CMF_GITHUB_SRC_URI_SUFFIX};name=key-simulator"
SRCREV_key-simulator = "${SRCREV:pn-iarm-query-powerstate}"
S = "${WORKDIR}/git"

CFLAGS:append = " -DYOCTO_BUILD"

DEPENDS = "iarmbus iarmmgrs dbus glib-2.0 wpeframework-clientlibraries devicesettings"
RDEPENDS:${PN} += "iarmmgrs wpeframework-clientlibraries"
inherit autotools pkgconfig coverity

do_install() {
        install -d ${D}${bindir}
        install -m 0755 ${B}/keySimulator ${D}${bindir}
}

FILES:${PN} += "${bindir}/keySimulator"
INSANE_SKIP:${PN} += "useless-rpaths"
