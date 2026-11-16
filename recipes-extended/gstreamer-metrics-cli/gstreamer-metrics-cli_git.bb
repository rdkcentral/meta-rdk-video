SUMMARY = "Rialto GStreamer metrics test CLI"
DESCRIPTION = "Standalone command-line tool for exercising Rialto GStreamer playback and metrics."
LICENSE = "LGPL-2.1-only"
LIC_FILES_CHKSUM = "file://COPYING.LGPL;md5=23c2a5e0106b99d75238986559bb5fc6"

require rialto_revision.inc

PV = "${RIALTO_GSTREAMER_VERSION}"
PR = "${RIALTO_GSTREAMER_PR}"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

# This revision must include the tests/metrics-cli target.
SRCREV = "${RIALTO_GSTREAMER_SRCREV}"
SRC_URI = "${CMF_GITHUB_ROOT}/rialto-gstreamer;protocol=${CMF_GIT_PROTOCOL};branch=${RIALTO_GSTREAMER_BRANCH}"

S = "${WORKDIR}/git"

DEPENDS = " \
    openssl \
    jsoncpp \
    glib-2.0 \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    wpeframework-clientlibraries \
    protobuf \
    protobuf-native \
    rialto \
    rialto-ocdm \
"

RDEPENDS:${PN} += "rialto-gstreamer"
FILES:${PN} = "${bindir}/rialto-gstreamer-metrics-test"

inherit pkgconfig cmake features_check

OECMAKE_TARGET_COMPILE = "rialto-gstreamer-metrics-test"
REQUIRED_DISTRO_FEATURES = "enable_rialto"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/tests/metrics-cli/rialto-gstreamer-metrics-test \
        ${D}${bindir}/rialto-gstreamer-metrics-test
}
