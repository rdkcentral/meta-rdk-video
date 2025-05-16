SUMMARY = "WPE Framework OpenCDMi module for Widevine"
LICENSE = "CLOSED"

include recipes-extended/wpe-framework/include/wpeframework-plugins.inc

SRC_URI = "git://github.com/rdkcentral/widevine-rdk.git;${CMF_GIT_SRC_URI_SUFFIX}"
SRCREV = "1.0.0"

DEPENDS += "  wpeframework wpeframework-clientlibraries wpeframework-tools-native rdkservices-apis"
DEPENDS += "  widevine gst-svp-ext gstreamer1.0"
DEPENDS += " ${@bb.utils.contains_any('DISTRO_FEATURES', 'amlogic-va amlogic-tv-va', 'aml-secmem optee-userspace', '', d)}"

# SWRDKV-2413/SWRDKV-2773, in dunfell build, each module has specific sysroot.
# Since in case of sage dtcp build, libcmndrm_tl.so is provided by dtcp, below are needed.
DTCP_DEPENDS = "${@oe.utils.conditional('WITHOUT_DTCP', 's', 'dtcp', '', d)}"
DEPENDS += " ${@bb.utils.contains('DISTRO_FEATURES', 'broadcom-va', 'broadcom-refsw protobuf ${DTCP_DEPENDS}', '', d)}"
DEPENDS:remove = " ${@bb.utils.contains('DISTRO_FEATURES', 'broadcom-va', 'widevine', '', d)}"

RDEPENDS:${PN} += " gst-svp-ext"
RDEPENDS:${PN} += " ${@bb.utils.contains('DISTRO_FEATURES', 'broadcom-va', '${DTCP_DEPENDS}', '', d)}"

WIDEVINE_VERSION="16"

EXTRA_OECMAKE += " -DCMAKE_WIDEVINE_VERSION=${WIDEVINE_VERSION} -DWIDEVINE_VERSION=${WIDEVINE_VERSION}"
EXTRA_OECMAKE += " ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', ' -DCMAKE_SYSTEMD_JOURNAL=1', '', d)}"
EXTRA_OECMAKE += " -DCMAKE_SYSROOT=${STAGING_DIR_TARGET}"

EXTRA_OECMAKE += " ${@bb.utils.contains_any('DISTRO_FEATURES', 'amlogic-va amlogic-tv-va', '-DWIDEVINE_AMLOGIC=ENABLED', '', d)}"
EXTRA_OECMAKE += " ${@bb.utils.contains('DISTRO_FEATURES', 'realtek-va', '-DWIDEVINE_REALTEK=ENABLED', '', d)}"
EXTRA_OECMAKE += " ${@bb.utils.contains('DISTRO_FEATURES', 'broadcom-va', '-DEXPORT_SYMBOLS=ON -DWIDEVINE_BROADCOM=ENABLED', '', d)}"

FILES:${PN} = " ${datadir}/WPEFramework/OCDM/*.drm"
FILES:${PN}-dbg += " ${datadir}/WPEFramework/OCDM/.debug/"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
