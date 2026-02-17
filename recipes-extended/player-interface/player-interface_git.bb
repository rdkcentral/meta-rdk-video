SUMMARY = "Middleware Player Interface layer for Player"
DESCRIPTION = "This layer provides the Player Firebolt Interface library for Player integration."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=97dd37dbf35103376811825b038fc32b"

PV = "0.1.0"
PR = "r0"

SRCREV = "313035de827a93b8c31d0e2ed3601ca66678ad3a"

inherit pkgconfig
inherit cmake

DEPENDS += "iarmmgrs wpeframework ${@bb.utils.contains('DISTRO_FEATURES', 'gstreamer1', 'gstreamer1.0 gstreamer1.0-plugins-base', 'gstreamer gst-plugins-base', d)} wpeframework-clientlibraries wpe-webkit virtual/vendor-gst-drm-plugins essos virtual/vendor-secapi2-adapter"
RDEPENDS:${PN} += "devicesettings ${@bb.utils.contains('DISTRO_FEATURES', 'subtec', 'packagegroup-subttxrend-app', '', d)}"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec', 'closedcaption-hal-headers virtual/vendor-dvb virtual/vendor-closedcaption-hal', '', d)} ${@bb.utils.contains('DISTRO_FEATURES', 'enable_rialto', 'dobby', '', d)}"

NO_RECOMMENDATIONS = "1"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI = "${CMF_GITHUB_ROOT}/middleware-player-interface;${CMF_GITHUB_SRC_URI_SUFFIX};name=player-interface;branch=develop"
S = "${WORKDIR}/git"

require player-interface-common.inc

PACKAGECONFIG:append = " playready widevine clearkey"

DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec', '-DCMAKE_GST_SUBTEC_ENABLED=1 ', '', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'wpe_security_util_disable', ' -DDISABLE_SECURITY_TOKEN=ON ', '', d)}"

EXTRA_OECMAKE += " -DCMAKE_WPEFRAMEWORK_REQUIRED=1 "

EXTRA_OECMAKE += " -DCMAKE_TELEMETRY_2_0_REQUIRED=1 " 
DEPENDS += " telemetry"

EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'sec_manager', ' -DCMAKE_USE_SECMANAGER=1 ', '', d)}"

EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'rdk_svp', ' -DCMAKE_RDK_SVP=1 ', '', d)}"

PACKAGES = "${PN} ${PN}-dev ${PN}-dbg"

FILES:${PN} += "${libdir}/lib*.so"
FILES:${PN} += "${libdir}/player-interface/lib*.so"
FILES:${PN} +="${libdir}/gstreamer-1.0/lib*.so"
FILES:${PN}-dbg +="${libdir}/gstreamer-1.0/.debug/*"

INSANE_SKIP:${PN} = "dev-so"
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/WPEFramework/ "

LDFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'wpe_security_util_disable', '', ' -lWPEFrameworkSecurityUtil ', d)}"
