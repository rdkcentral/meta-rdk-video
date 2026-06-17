SUMMARY = "GStreamer decoder handoff test for RDK STB"
DESCRIPTION = "Reproduces ESS RM notification bug where destroying a pipeline \
that previously owned a HW video decoder causes a second pipeline to believe \
its decoder is unavailable."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://CMakeLists.txt;beginline=1;endline=1;md5=214e8be833697f3b287190e870006586"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI = " \
    file://CMakeLists.txt \
    file://src/main.cpp \
"

S = "${WORKDIR}"

inherit cmake pkgconfig

DEPENDS = "gstreamer1.0"

# No runtime plugins strictly required by the binary itself,
# but playbin needs at least these to work:
RDEPENDS:${PN} = " \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
"

FILES:${PN} = "${bindir}/gst_multiple_playback"
