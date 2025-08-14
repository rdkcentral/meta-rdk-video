######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

SUMMARY = "Rialto-gstreamer"
LICENSE = "LGPL-2.1-only"
LIC_FILES_CHKSUM = "file://COPYING.LGPL;md5=23c2a5e0106b99d75238986559bb5fc6"

require rialto_revision.inc

SRC_URI = "${CMF_GITHUB_ROOT}/rialto-gstreamer;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GITHUB_MASTER_BRANCH}"

DEPENDS = "openssl jsoncpp glib-2.0 gstreamer1.0 gstreamer1.0-plugins-base wpeframework-clientlibraries protobuf protobuf-native rialto rialto-ocdm"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'rdk_svp', " gst-svp-ext", "", d)}"

S = "${WORKDIR}/git"
inherit pkgconfig cmake coverity features_check

FILES:${PN} += "${libdir}/gstreamer-1.0/libgstrialtosinks.so"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'rdk_svp', '-DCMAKE_RDK_SVP=1', "", d)}"

RDEPENDS_${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'rdk_svp', " gst-svp-ext", "", d)}"

FILES_${PN} += "${libdir}/gstreamer-1.0/libgstrialtosinks.so"
 
REQUIRED_DISTRO_FEATURES = "enable_rialto"

#Needed for Kirkstone packagegroup error
DEBIAN_NOAUTONAME_${PN} = "1"
DEBIAN_NOAUTONAME_${PN}-dev = "1"
DEBIAN_NOAUTONAME_${PN}-dbg = "1"
