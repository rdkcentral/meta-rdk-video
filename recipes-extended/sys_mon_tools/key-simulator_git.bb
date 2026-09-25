SUMMARY = "Sys mon tool key simulator recipe"

DESCRIPTION = "Sys mon tool key simulator recipe"

SECTION = "console/utils"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
PV = "1.0.8"
PR = "r1"

SRCREV_key-simulator = "f6439f53b1f0dcd611dad206a910095613cd2004"
SRC_URI = "${CMF_GITHUB_ROOT}/sys_mon_tools;${CMF_GITHUB_SRC_URI_SUFFIX};name=key-simulator"
S = "${WORKDIR}/git"

CFLAGS:append = " -DYOCTO_BUILD"

DEPENDS = "iarmbus iarmmgrs dbus glib-2.0 wpeframework-clientlibraries"
RDEPENDS:${PN} += "iarmmgrs wpeframework-clientlibraries"
inherit autotools pkgconfig coverity

do_install() {
        install -d ${D}${bindir}
        install -m 0755 ${B}/keySimulator ${D}${bindir}
}

FILES:${PN} += "${bindir}/keySimulator"
INSANE_SKIP:${PN} += "useless-rpaths"
