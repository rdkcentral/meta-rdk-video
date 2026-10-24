SUMMARY = "WPE Framework OpenCDMi module for Widevine"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=19a2b3c39737289f92c7991b16599360"

include recipes-extended/wpe-framework/include/wpeframework-plugins.inc

PR = "r0"
PV = "1.2.0"

SRC_URI = "git://github.com/rdkcentral/widevine-rdk.git;${CMF_GITHUB_SRC_URI_SUFFIX}"
# TAG version 1.2.0
SRCREV = "da44826fce15cb420d5f637affddab8bb4dc8264"

# Platform configurations
DEPENDS += " ${platform-widevine-depends}"
EXTRA_OECMAKE += " ${platform-widevine-flags} -DCMAKE_WIDEVINE_VERSION=${WIDEVINE_VERSION}"
RDEPENDS:${PN} += " ${platform-widevine-rdepends}"

DEPENDS += "  wpeframework wpeframework-clientlibraries wpeframework-tools-native entservices-apis entservices-apis-ocdm"
DEPENDS += "  gst-svp-ext gstreamer1.0"

RDEPENDS:${PN} += " gst-svp-ext"

EXTRA_OECMAKE += " ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', ' -DCMAKE_SYSTEMD_JOURNAL=1', '', d)}"
EXTRA_OECMAKE += " -DCMAKE_SYSROOT=${STAGING_DIR_TARGET}"

FILES:${PN} = " ${datadir}/WPEFramework/OCDM/*.drm"
FILES:${PN}-dbg += " ${datadir}/WPEFramework/OCDM/.debug/"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

WIDEVINE_VERSION="${@bb.utils.contains('DISTRO_FEATURES', 'widevine_v19', '19', \
                     bb.utils.contains('DISTRO_FEATURES', 'widevine_v18', '18', \
                     bb.utils.contains('DISTRO_FEATURES', 'widevine_v17', '17', \
                                                                          '16', \
                                                                            d), \
                                                                            d), \
                                                                            d)}"
