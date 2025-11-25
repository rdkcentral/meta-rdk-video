######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

SUMMARY = "Rialto-ocdm"
LICENSE  = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1fa88b316b1ce25ab7d95ae4d854ec8f"

require rialto_revision.inc

SRC_URI = "${CMF_GITHUB_ROOT}/rialto-ocdm;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GITHUB_MASTER_BRANCH}"
SRCREV = "4f8e4556754cde3498d960d47fd0827f997ea43b"

DEPENDS = "openssl jsoncpp glib-2.0 gstreamer1.0 gstreamer1.0-plugins-base wpeframework-tools-native wpeframework-clientlibraries protobuf protobuf-native rialto"

S = "${WORKDIR}/git"

inherit pkgconfig cmake coverity features_check

REQUIRED_DISTRO_FEATURES = "enable_rialto"

#Needed for Kirkstone packagegroup error
DEBIAN_NOAUTONAME_${PN} = "1"
DEBIAN_NOAUTONAME_${PN}-dev = "1"
DEBIAN_NOAUTONAME_${PN}-dbg = "1"
