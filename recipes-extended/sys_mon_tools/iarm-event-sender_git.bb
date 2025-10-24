SUMMARY = "Sys mon tool key simulator recipe"

DESCRIPTION = "Sys mon tool key simulator recipe"

SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV ?= "1.0.0"
PR ?= "r0"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
SRC_URI = "${CMF_GITHUB_ROOT}/sys_mon_tools;${CMF_GITHUB_SRC_URI_SUFFIX}"
S = "${WORKDIR}/git"

CFLAGS:append = " -DYOCTO_BUILD"
DEPENDS = "iarmbus iarmmgrs dbus glib-2.0 wpeframework-clientlibraries devicesettings"
RDEPENDS:${PN} += "iarmmgrs wpeframework-clientlibraries"
DEPENDS += " ${@bb.utils.contains('DISTRO_FEATURES', 'wifi', bb.utils.contains('DISTRO_FEATURES', 'ENABLE_NETWORKMANAGER', '', 'netsrvmgr', d), '', d)}"


CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'ctrlm', '-DCTRLM_ENABLED', '', d)}"
CFLAGS += "-DPLATFORM_SUPPORTS_RDMMGR"
CFLAGS += " ${@bb.utils.contains('DISTRO_FEATURES', 'wifi', bb.utils.contains('DISTRO_FEATURES', 'ENABLE_NETWORKMANAGER', '', '-DHAS_WIFI_SUPPORT', d), '', d)}"
CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_maintenance_manager', '-DHAS_MAINTENANCE_MANAGER', '', d)}"

inherit autotools pkgconfig coverity

do_install() {
        install -d ${D}${bindir}
        install -m 0755 ${B}/IARM_event_sender ${D}${bindir}
}

FILES:${PN} += "${bindir}/IARM_event_sender"
INSANE_SKIP:${PN} += "useless-rpaths"

