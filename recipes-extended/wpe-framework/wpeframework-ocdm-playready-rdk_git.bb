SUMMARY = "WPE Framework OpenCDMi module for Playready"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=19a2b3c39737289f92c7991b16599360"

include recipes-extended/wpe-framework/include/wpeframework-plugins.inc

DEPENDS += "  wpeframework wpeframework-clientlibraries wpeframework-tools-native entservices-apis"
DEPENDS += "  gst-svp-ext gstreamer1.0"

# Platform configurations
DEPENDS += " ${platform-playready-depends}"
EXTRA_OECMAKE += " ${platform-playready-flags}"
RDEPENDS:${PN} += " ${platform-playready-rdepends}"

RDEPENDS_${PN} += " gst-svp-ext"

inherit cmake pkgconfig

TOOLCHAIN = "gcc"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI = "git://github.com/rdkcentral/playready-rdk.git;${CMF_GITHUB_SRC_URI_SUFFIX};name=pr-source"
SRCREV = "1dbf957f4f6a7ce8a2708fa241c189a890fa6e58"
SRCREV_FORMAT = "pr-source pr-header"
S = "${WORKDIR}/git"

WPEFRAMEWORK_PERSISTENT_PATH := "${@bb.utils.contains('DISTRO_FEATURES', 'DOBBY_CONTAINERS', '/opt/persistent/rdkservices/', '/data/persistent/', d)}"
EXTRA_OECMAKE += " -DPERSISTENT_PATH=${WPEFRAMEWORK_PERSISTENT_PATH} "
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', ' -DCMAKE_SYSTEMD_JOURNAL=1', '', d)}"

do_install:append() {
    install --mode=0755 -d ${D}/usr/include/playready
}

BREAKPAD_BIN = "PlayReady.drm"
EXTRA_OECMAKE += " -DCMAKE_SYSROOT=${STAGING_DIR_TARGET} "
CFLAGS += " -fpermissive "
CXXFLAGS += " -fpermissive "

OECMAKE_C_FLAGS += " -I${STAGING_INCDIR}/gstreamer-1.0 -I${STAGING_INCDIR} -I${STAGING_INCDIR}/glib-2.0 -I${STAGING_DIR_TARGET}${libdir}/glib-2.0/include"
CXXFLAGS += " -I${STAGING_INCDIR}/gstreamer-1.0 -I${STAGING_INCDIR} -I${STAGING_INCDIR}/glib-2.0 -I${STAGING_DIR_TARGET}${libdir}/glib-2.0/include"
INHIBIT_PACKAGE_DEBUG_SPLIT = '1'

FILES:${PN} += " \
    ${datadir}/WPEFramework/OCDM/*.drm \
    ${WPEFRAMEWORK_PERSISTENT_PATH}OCDM/playready \
"
FILES:${PN}-dbg += " \
    ${WPEFRAMEWORK_PERSISTENT_PATH} \
    ${datadir}/WPEFramework/OCDM/.debug/ \
"
