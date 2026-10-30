SUMMARY = "Text Track Plugin"
DESCRIPTION = "Text Track Plugin Meta Package"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

SRCREV = "e4dbeea1b8762235f320cfb96525f9bbb0bb8b86"
PV = "2.0.0"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit cmake features_check pkgconfig

# This recipe supports one distro-feature:
# texttrack : enables full functionality, including session management and compatibility socket
REQUIRED_DISTRO_FEATURES = "texttrack"

SRC_URI = "${CMF_GITHUB_ROOT}/texttrack;protocol=${CMF_GITHUB_PROTOCOL};branch=main"
SRC_URI += "file://texttrack.conf"
SRC_URI += "file://config.ini"
S = "${WORKDIR}/git"

# Build depends
DEPENDS = "entservices-apis wpeframework-tools-native"
DEPENDS += "subttxrend-ctrl subttxrend-common subttxrend-socksrc subttxrend-gfx subttxrend-protocol"

# Make it possible to override this in .bbappend files
TEXTTRACK_STANDARD_DISPLAY ?= ""

#PACKAGECONFIG:append = " cchal"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'thunderstartup', '', d)}"

PACKAGECONFIG[debug] = "-DCMAKE_BUILD_TYPE=Debug,-DCMAKE_BUILD_TYPE=Release,"
# The 'cchal' config controls whether to compile support for use of CC HAL to obtain the CC data from the video decoder subsystem
PACKAGECONFIG[cchal] = "-DTEXTTRACK_WITH_CCHAL=ON,-DTEXTTRACK_WITH_CCHAL=OFF,closedcaption-hal-headers virtual/vendor-closedcaption-hal,,,"
# The 'thunderstartup' config ensures that the baked-in autostart for the plugin is set to false, expecting that a systemd service will handle the startup
PACKAGECONFIG[thunderstartup] = "-DTEXTTRACK_AUTOSTART=false,-DTEXTTRACK_AUTOSTART=true"

EXTRA_OECMAKE += " -DTEXTTRACK_STANDARD_DISPLAY=${TEXTTRACK_STANDARD_DISPLAY}"
EXTRA_OECMAKE += " -DTEXTTRACK_CONFIG_FILE_PATH=${sysconfdir}/texttrack/config.ini"

do_install:append() {
    install -D -m 0644 -t ${D}${sysconfdir}/tmpfiles.d ${WORKDIR}/texttrack.conf
    install -D -m 0644 -t ${D}${sysconfdir}/texttrack ${WORKDIR}/config.ini
}

#
# files to be installed
#
FILES:${PN} += "${libdir}/wpeframework/* ${datadir}/WPEFramework/*"
