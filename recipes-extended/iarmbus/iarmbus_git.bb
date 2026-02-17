SUMMARY = "IARM-Bus is a platform agnostic IPC interface."
DESCRIPTION = "It allows applications to communicate\
with each other by sending Events or invoking Remote Procedure\
Calls. The common programming APIs offered by the RDK IARM-Bus\
interface is independent of the operating system or the underlying IPC\
mechanism."
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"
PV = "1.0.3"
PR = "r0"

SRCREV_iarmbus = "97c6aa97457cf161a0821bb9a37b2e332cb263ba"
SRCREV_FORMAT = "iarmbus"
SRC_URI = "${CMF_GITHUB_ROOT}/iarmbus;${CMF_GITHUB_SRC_URI_SUFFIX};name=iarmbus"

S = "${WORKDIR}/git"

CFLAGS += "-DENABLE_SD_NOTIFY"
LDFLAGS += "-lsystemd"

DEPENDS="libxml2 dbus glib-2.0"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'directfb', 'directfb', '', d)}"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', '', d)}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit autotools pkgconfig systemd coverity syslog-ng-config-gen logrotate_config
SYSLOG-NG_FILTER = "uimgr"
SYSLOG-NG_SERVICE_uimgr = "iarmbusd.service"
SYSLOG-NG_DESTINATION_uimgr = "uimgr_log.txt"
SYSLOG-NG_LOGRATE_uimgr = "very-high"

LOGROTATE_NAME="uimgr"
LOGROTATE_LOGNAME_uimgr="uimgr_log.txt"
#HDD_DISABLE
LOGROTATE_SIZE_MEM_uimgr="512000"
LOGROTATE_ROTATION_MEM_uimgr="1"
#HDD_ENABLE
LOGROTATE_SIZE_uimgr="20971520"
LOGROTATE_ROTATION_uimgr="25"

DEPENDS += "safec-common-wrapper"
DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec', " ", d)}"


CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --cflags libsafec`', ' -fPIC', d)}"
CFLAGS:append:client = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --cflags libsafec`', ' -fPIC', d)}"

LDFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --libs libsafec`', '', d)}"
CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"



do_install:append() {
	install -d ${D}${includedir}/rdk/iarmbus
	install -m 0644 ${S}/core/include/*.h ${D}${includedir}/rdk/iarmbus
	install -m 0644 ${S}/core/*.h ${D}${includedir}/rdk/iarmbus
	install -d ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/conf/iarmbusd.service ${D}${systemd_unitdir}/system
}

SYSTEMD_SERVICE:${PN} = "iarmbusd.service"
FILES:${PN} += "${systemd_unitdir}/system/iarmbusd.service"
