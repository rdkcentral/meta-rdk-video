SUMMARY = "WPE Framework OpenCDMi module for Playready"

LICENSE = "CLOSED"

include recipes-extended/wpe-framework/include/wpeframework-plugins.inc

DEPENDS += "  wpeframework wpeframework-clientlibraries wpeframework-tools-native rdkservices-apis"
DEPENDS += "  gst-svp-ext gstreamer1.0"

#platform specific dependency
DEPENDS += " ${@bb.utils.contains_any('DISTRO_FEATURES', 'amlogic-va amlogic-tv-va', " aml-secmem optee-userspace playready", '', d)}"
DEPENDS += " ${@bb.utils.contains('DISTRO_FEATURES', 'broadcom-va', ' broadcom-refsw', '', d)}"
DEPENDS += " ${@bb.utils.contains('DISTRO_FEATURES', 'realtek-va', ' openssl playready', '', d)}"

RDEPENDS_${PN} += " gst-svp-ext"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

TOOLCHAIN = "gcc"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRCREV = "1.0.0"
SRCREV_FORMAT = "pr-source pr-header"
SRC_URI = "git://github.com/rdkcentral/playready-rdk.git;${CMF_GIT_SRC_URI_SUFFIX};name=pr-source"

WPEFRAMEWORK_PERSISTENT_PATH := "${@bb.utils.contains('DISTRO_FEATURES', 'DOBBY_CONTAINERS', '/opt/persistent/rdkservices/', '/data/persistent/', d)}"
EXTRA_OECMAKE += " -DPERSISTENT_PATH=${WPEFRAMEWORK_PERSISTENT_PATH} "
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', ' -DCMAKE_SYSTEMD_JOURNAL=1', '', d)}"
EXTRA_OECMAKE += "-DNO_PERSISTENT_LICENSE_CHECK=ON"

#platform specific flags
EXTRA_OECMAKE += " ${@bb.utils.contains_any('DISTRO_FEATURES', 'amlogic-va amlogic-tv-va', '-DPLAYREADY_AMLOGIC=ENABLED', '', d)}"
EXTRA_OECMAKE += " ${@bb.utils.contains('DISTRO_FEATURES', 'realtek-va', '-DPLAYREADY_REALTEK=ENABLED', '', d)}"
EXTRA_OECMAKE += " ${@bb.utils.contains('DISTRO_FEATURES', 'broadcom-va', '-DNEXUS_PLAYREADY_SVP_ENABLE=ON -DEXPORT_SYMBOLS=ON -DPLAYREADY_BROADCOM=ENABLED -DDRM_ANTI_ROLLBACK_CLOCK_SUPPORT=ON', '', d)}"
EXTRA_OECMAKE += " ${@bb.utils.contains_any('DISTRO_FEATURES', 'amlogic-va amlogic-tv-va', '-DUSE_PLAYREADY_CMAKE=1 -DTEE_CONFIG_NEED=ON -DDRM_ERROR_NAME_SUPPORT=ON', '', d)}"


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
