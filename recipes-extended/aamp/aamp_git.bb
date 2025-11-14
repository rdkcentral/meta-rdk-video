SUMMARY = "RDK AAMP component"
SECTION = "console/utils"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=97dd37dbf35103376811825b038fc32b"

PV ?= "2.0.2"
PR ?= "r0"

SRCREV_FORMAT = "aamp"

inherit pkgconfig
DEPENDS += "curl libdash libxml2 cjson readline"
DEPENDS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'subtec1', ' player-interface', '', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec1', '-DCMAKE_TEST_MW=0', '', d)}"

RDEPENDS:${PN} += "devicesettings"
RDEPENDS:${PN}:append = "${@bb.utils.contains('DISTRO_FEATURES', 'subtec1', ' player-interface', '', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec1', ' -DCMAKE_TEST_MW=0', '', d)}"
#DEPENDS += "curl libdash libxml2 cjson readline player-interface"
#RDEPENDS:${PN} += "devicesettings player-interface"
NO_RECOMMENDATIONS = "1"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
#To be removed later, the AAMP_RELEASE_TAG_NAME is not using.
AAMP_RELEASE_TAG_NAME ?= "5.9.1.0"

SRC_URI = "${CMF_GITHUB_ROOT}/aamp;${CMF_GITHUB_SRC_URI_SUFFIX};name=aamp"

S = "${WORKDIR}/git"

DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'webkitbrowser-plugin', '${WPEWEBKIT}', '', d)}"

require aamp-common.inc
require ${@bb.utils.contains('DISTRO_FEATURES', 'subtec1', '', 'aamp-middleware.inc', d)}

EXTRA_OECMAKE += " -DCMAKE_WPEWEBKIT_WATERMARK_JSBINDINGS=1 "

RDEPENDS:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec', 'packagegroup-subttxrend-app', '', d)}"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec', 'closedcaption-hal-headers virtual/vendor-dvb virtual/vendor-closedcaption-hal', '', d)}"

#Ethan log is implemented by Dobby hence enabling it.
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_rialto', 'dobby', '', d)}"
PACKAGES = "${PN} ${PN}-dev ${PN}-dbg"

FILES:${PN} += "${libdir}/lib*.so"
FILES:${PN} += "${libdir}/aamp-cli"
FILES:${PN} += "${libdir}/aamp/lib*.so"
FILES:${PN} +="${libdir}/gstreamer-1.0/lib*.so"
FILES:${PN}-dbg +="${libdir}/gstreamer-1.0/.debug/*"

INSANE_SKIP:${PN} = "dev-so"

EXTRA_OECMAKE += " -DCMAKE_LIGHTTPD_AUTHSERVICE_DISABLE=1 "

CXXFLAGS += " -DAAMP_BUILD_INFO=${AAMP_RELEASE_TAG_NAME}" 

#required for specific products but for now distro is available only for UK 
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_UK', ' -DENABLE_USE_SINGLE_PIPELINE=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_IT', ' -DENABLE_USE_SINGLE_PIPELINE=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_DE', ' -DENABLE_USE_SINGLE_PIPELINE=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_AU', ' -DENABLE_USE_SINGLE_PIPELINE=1', '', d)}"

# Enable PTS restamp feature
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_UK', ' -DENABLE_PTS_RESTAMP=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_IT', ' -DENABLE_PTS_RESTAMP=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_DE', ' -DENABLE_PTS_RESTAMP=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_AU', ' -DENABLE_PTS_RESTAMP=1', '', d)}"

INCLUDE_DIRS = " \
    -I=${includedir}/rdk/halif/ds-hal \
    "

do_install:append() {
    echo "Installing aamp-cli..."
    install -m755 ${B}/aamp-cli ${D}${libdir}

    # remove the static library if it is installed, 
    # CMakelist in aamp code installing static lib below line should avoid build error 
    rm -f ${D}${libdir}/libtsb.a
}
