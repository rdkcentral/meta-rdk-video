SUMMARY = "TTS Engine is a generic solution for text to speech"
DESCRIPTION = "This component receives text inputs from various apps and \
voices it out through gstreamer"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${THISDIR}/files/Apache-2.0;md5=3b83ef96387f14655fc854ddc3c6bd57"

DEPENDS += "gstreamer1.0-plugins-base"

require ${@'tts-bolt.inc' if d.getVar('DISTRO') == 'bolt-distro' else 'tts-mw.inc'}

SRCREV = "${AUTOREV}"

PV = "1.0.5"
PR = "r0"

inherit cmake breakpad-wrapper pkgconfig

DEPENDS += "breakpad breakpad-wrapper"
EXTRA_OECMAKE += "-DENABLE_BREAKPAD=1"
BREAKPAD_BIN = " TTSEngine *.so "

SRC_URI = "git://github.com/lulicdarko/ttsengine.git;protocol=https;branch=tts_against_firebolt_0.5.3 \
    "

S = "${WORKDIR}/git"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

INSANE_SKIP:${PN} = "dev-so"
INSANE_SKIP:${PN} += "ldflags"
FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so"
FILES:${PN} += "/lib/rdk/*"
