SUMMARY = "TTS Engine is a generic solution for text to speech"
DESCRIPTION = "This component receives text inputs from various apps and \
voices it out through gstreamer"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${THISDIR}/files/Apache-2.0;md5=3b83ef96387f14655fc854ddc3c6bd57"

DEPENDS += "gstreamer1.0-plugins-base rdk-logger wpeframework wpeframework-clientlibraries"

SRCREV = "0845f4da2b886e6cc707077c8f290cc279a84e11"

PV = "1.0.5"
PR = "r0"

inherit cmake breakpad-wrapper pkgconfig

DEPENDS += "breakpad breakpad-wrapper"
EXTRA_OECMAKE += "-DENABLE_BREAKPAD=1"
BREAKPAD_BIN = " TTSEngine *.so "

SRC_URI = "${CMF_GITHUB_ROOT}/ttsengine;${CMF_GITHUB_SRC_URI_SUFFIX}"

S = "${WORKDIR}/git"

CXXFLAGS += " -I${STAGING_INCDIR}/Firebolt -I${STAGING_INCDIR}/glib-2.0 -I${STAGING_INCDIR}/../usr/lib/glib-2.0/include "
CXXFLAGS += " -std=gnu++17"
EXTRA_OECMAKE:remove = " -std=gnu++11"
CXXFLAGS:remove = " -std=gnu++11"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

INSANE_SKIP:${PN} = "dev-so"
INSANE_SKIP:${PN} += "ldflags"
FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so"
FILES:${PN} += "/lib/rdk/*"
