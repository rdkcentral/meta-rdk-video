SUMMARY = "audiocapturemgr recipe"

SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"


PV ?= "1.0.0"
PR ?= "r0"

SRC_URI = "${CMF_GITHUB_ROOT}/audiocapturemgr;${CMF_GITHUB_SRC_URI_SUFFIX};name=audiocapturemgr"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRCREV_FORMAT = "audiocapturemgr"
DEPENDS = "virtual/vendor-media-utils media-utils-headers iarmbus iarmmgrs libunpriv"
RDEPENDS:${PN}:append = " virtual/vendor-media-utils"
DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec', " ", d)}"
DEPENDS += "safec-common-wrapper"
LDFLAGS:append = "-lprivilege"
CXXFLAGS += " -DDROP_ROOT_PRIV"

S = "${WORKDIR}/git"
export RDK_FSROOT_PATH = '${STAGING_DIR_TARGET}'

inherit autotools pkgconfig systemd breakpad-logmapper syslog-ng-config-gen
SYSLOG-NG_FILTER = "audiocapturemgr"
SYSLOG-NG_SERVICE_audiocapturemgr = "audiocapturemgr.service"
SYSLOG-NG_DESTINATION_audiocapturemgr = "audiocapturemgr.log"
SYSLOG-NG_LOGRATE_audiocapturemgr = "medium"


CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec',  ' `pkg-config --cflags libsafec`', '-fPIC', d)}"
CXXFLAGS:append:client = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec',  ' `pkg-config --cflags libsafec`', '-fPIC', d)}"

LDFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --libs libsafec`', '', d)}"
CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"

do_install:append() {
   install -d ${D}${systemd_unitdir}/system
   install -m 0644 ${S}/conf/audiocapturemgr.service ${D}${systemd_unitdir}/system
}

FILES:${PN} += "${systemd_unitdir}/system/*"
SYSTEMD_SERVICE:${PN} = "audiocapturemgr.service"
# Breakpad processname and logfile mapping
BREAKPAD_LOGMAPPER_PROCLIST = "audiocapturemgr"
BREAKPAD_LOGMAPPER_LOGLIST = "audiocapturemgr.log"

