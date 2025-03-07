SUMMARY = "Sys mon tool - IARM SET POWERSTATE recipe"

DESCRIPTION = "Sys mon tool - IARM SET POWERSTATE recipe"

SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
PV ?= "1.0.0"
PR ?= "r0"

SRC_URI = "${CMF_GITHUB_ROOT}/sys_mon_tools;${CMF_GITHUB_SRC_URI_SUFFIX};name=iarm-set-powerstate"
S = "${WORKDIR}/git"

CFLAGS:append = " -DYOCTO_BUILD"
DEPENDS = "iarmbus iarmmgrs dbus glib-2.0 wpeframework-clientlibraries"
RDEPENDS:${PN} += "iarmmgrs wpeframework-clientlibraries"

inherit autotools pkgconfig coverity 

do_install() {
        install -d ${D}${bindir}
        install -m 0755 ${B}/SetPowerState ${D}${bindir}/
        # Create a symbolic link in / as the current automation team testing scripts
        # expect binaries to be present in root
        ln -sf ${bindir}/SetPowerState ${D}/SetPowerState
}

FILES:${PN} = "${bindir}/SetPowerState \
               /SetPowerState"
INSANE_SKIP:${PN} += "useless-rpaths"
