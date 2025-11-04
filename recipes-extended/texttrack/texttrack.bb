SUMMARY = "Text Track Plugin"
DESCRIPTION = "Text Track Plugin Meta Package"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

SRCREV = "635553bbfa1234f231c15165d3018ce79972b1a8"
PV = "1.4.0"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit cmake features_check pkgconfig

# This recipe supports one distro-feature:
# texttrack : enables full functionality, including session management and compatibility socket
REQUIRED_DISTRO_FEATURES = "texttrack"

SRC_URI = "${CMF_GITHUB_ROOT}/texttrack;protocol=${CMF_GIT_PROTOCOL};branch=main"
SRC_URI += "file://texttrack.conf"
SRC_URI += "file://config.ini"
S = "${WORKDIR}/git"

# Build depends
DEPENDS += " entservices-apis wpeframework-tools-native"
DEPENDS += " subttxrend-ctrl subttxrend-common subttxrend-socksrc"
DEPENDS += " subttxrend-gfx subttxrend-protocol"

# Image depends
#RDEPENDS:${PN} = ""

# Autostart variants
# Handled automatically by "thunderstartupservices"

# Make it possible to override this in .bbappend files
TEXTTRACK_STANDARD_DISPLAY ?= "westeros-asplayer-subtitles"

PACKAGECONFIG:append = " sessions cchal"
# Even though RDK-E does run with Dobby containers, it does not have the dobbyapp user, so this config is irrelevant
# PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'DOBBY_CONTAINERS', 'dobbyapp', '', d)}"

PACKAGECONFIG[debug] = "-DCMAKE_BUILD_TYPE=Debug,-DCMAKE_BUILD_TYPE=Release,"
PACKAGECONFIG[sessions] = "-DTEXTTRACK_WITH_SESSIONS=ON,-DTEXTTRACK_WITH_SESSIONS=OFF,,,,"
PACKAGECONFIG[dobbyapp] = "-DTEXTTRACK_WITH_CHOWN_DOBBYAPP=ON,-DTEXTTRACK_WITH_CHOWN_DOBBYAPP=OFF,,,,"
PACKAGECONFIG[cchal] = "-DTEXTTRACK_WITH_CCHAL=ON,-DTEXTTRACK_WITH_CCHAL=OFF,closedcaption-hal-headers virtual/vendor-closedcaption-hal,,,"

EXTRA_OECMAKE += " -DTEXTTRACK_STANDARD_DISPLAY=${TEXTTRACK_STANDARD_DISPLAY}"
EXTRA_OECMAKE += " -DTEXTTRACK_AUTOSTART=true"
EXTRA_OECMAKE += " -DTEXTTRACK_CONFIG_FILE_PATH=${sysconfdir}/texttrack/config.ini"

do_install:append() {
    install -D -m 0644 -t ${D}${sysconfdir}/tmpfiles.d ${WORKDIR}/texttrack.conf
    install -D -m 0644 -t ${D}${sysconfdir}/texttrack ${WORKDIR}/config.ini
    if ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'true', 'false', d)} == 'true'; then
        if [ -d "${D}/etc/WPEFramework/plugins" ]; then
            find ${D}/etc/WPEFramework/plugins/ -type f | xargs sed -i -r 's/"autostart"[[:space:]]*:[[:space:]]*true/"autostart":false/g'
        fi
    fi
}

#
# files to be installed
#
FILES:${PN} += "${libdir}/wpeframework/* ${datadir}/WPEFramework/*"
