SUMMARY = "This recipe compiles and installs hdmicec component."
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV ?= "1.0.1"
PR ?= "r0"

SRC_URI = "${CMF_GITHUB_ROOT}/hdmicec;${CMF_GITHUB_SRC_URI_SUFFIX};name=hdmicec"
SRCREV_FORMAT = "hdmicec"
SRCREV = "b407684e91a936a07fadf4ef393505a5d06db890"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DEPENDS = "glib-2.0 dbus iarmbus devicesettings devicesettings-hal-headers hdmicecheader virtual/vendor-hdmicec-hal iarmmgrs-hal-headers telemetry"
RDEPENDS:${PN} = " devicesettings telemetry"

DEPENDS += "safec-common-wrapper"

ASNEEDED = ""
ALLOW_EMPTY:${PN} = "1"

S = "${WORKDIR}/git"

DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec', " ", d)}"

inherit systemd autotools pkgconfig coverity breakpad-logmapper syslog-ng-config-gen logrotate_config
#SYSLOG-NG_FILTER = "cec"
#SYSLOG-NG_SERVICE_cec = "cecdaemon.service cecdevmgr.service"
#SYSLOG-NG_DESTINATION_cec = "cec_log.txt"
#SYSLOG-NG_LOGRATE_cec = "medium"

LOGROTATE_NAME="cec"
LOGROTATE_LOGNAME_cec="cec_log.txt"
#HDD_ENABLE
LOGROTATE_SIZE_cec="5242880"
LOGROTATE_ROTATION_cec="1"
#HDD_DISABLE
LOGROTATE_SIZE_MEM_cec="128000"
LOGROTATE_ROTATION_MEM_cec="1"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec',  ' `pkg-config --cflags libsafec`', '-fPIC', d)}"

CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec',  ' `pkg-config --cflags libsafec`', '-fPIC', d)}"

LDFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --libs libsafec`', '', d)}"
CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"
CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"

INCLUDE_DIRS = " \
    -I=${includedir}/rdk/halif/ds-hal \
    "



do_install:append() {
#        install -d ${D}${includedir}/rdk/hdmicec
#        install -d ${D}${includedir}/ccec/drivers
#        install -m 0644 ${S}/ccec/drivers/include/ccec/drivers/iarmbus/CecIARMBusMgr.h ${D}${includedir}/ccec/drivers
#        install -d ${D}${systemd_unitdir}/system
#        install -m 0644 ${S}/cecdaemon.service ${D}${systemd_unitdir}/system
#        install -m 0644 ${S}/cecdevmgr.service ${D}${systemd_unitdir}/system
#        install -d ${D}${base_libdir}/rdk
}

#SYSTEMD_SERVICE:${PN} = "cecdaemon.service"
#SYSTEMD_SERVICE:${PN} = "cecdevmgr.service"
#FILES:${PN} += "${systemd_unitdir}/system/cecdaemon.service"
#FILES:${PN} += "${systemd_unitdir}/system/cecdevmgr.service"
# Breakpad processname and logfile mapping
#BREAKPAD_LOGMAPPER_PROCLIST = "CecDaemonMain"
#BREAKPAD_LOGMAPPER_LOGLIST = "cec_log.txt"
