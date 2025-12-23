SUMMARY = "RDK Gstreamer plugins"
DESCRIPTION = "RDK gst-plugins. These are the plugins encoding and \
decoding gstreamer elements. Theere are plugins available for dtcp and \
aes encoding/decoding. DTCP is used for Data exchange between the home \
networking elements."

SECTION = "console/utils"

LICENSE = "LGPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=db791dc95f6a08e8e4d206839bc67ec0"

PV ?= "1.0.0"
PR ?= "r0"

SRC_URI = "${CMF_GITHUB_ROOT}/gst-plugins-rdk;${CMF_GITHUB_SRC_URI_SUFFIX};name=gst-plugins-rdk"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRCREV_FORMAT = "gst-plugins-rdk"

S = "${WORKDIR}/git"

FILES:${PN} += "${libdir}/gstreamer-*/*.so"
FILES:${PN}-dev += "${libdir}/gstreamer-*/*.la"
FILES:${PN}-dbg += "${libdir}/gstreamer-*/.debug/*"
FILES:${PN}-staticdev += "${libdir}/gstreamer-*/*.a "

DEPENDS = "curl"
DEPENDS += " ${@bb.utils.contains('DISTRO_FEATURES', 'gstreamer1', 'gstreamer1.0', 'gstreamer', d)}"
RDEPENDS:${PN} += " ${@bb.utils.contains('DISTRO_FEATURES', 'gstreamer1', 'gstreamer1.0', 'gstreamer', d)}"
DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec', " ", d)}"
DEPENDS += "safec-common-wrapper"

ENABLE_GST1 = "--enable-gstreamer1=${@bb.utils.contains('DISTRO_FEATURES', 'gstreamer1', 'yes', 'no', d)}"
EXTRA_OECONF = "${ENABLE_GST1}"

DEBIAN_NOAUTONAME:${PN} = "1"

inherit autotools pkgconfig

CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --cflags libsafec`', ' -fPIC', d)}"
CFLAGS:append:client = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --cflags libsafec`', ' -fPIC', d)}"

LDFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --libs libsafec`', '', d)}"
CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"

PACKAGECONFIG ??= "httpsrc"
PACKAGECONFIG[httpsrc] = "--enable-httpsrc,,openssl,"

