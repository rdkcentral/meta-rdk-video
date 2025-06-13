SUMMARY = "Middleware Player Interface layer for Player"
DESCRIPTION = "This layer provides the Player Firebolt Interface library for Player integration."
LICENSE = "CLOSED"

PV ?= "1.0.0"
PR ?= "r0"


inherit pkgconfig

DEPENDS += "curl libdash libxml2 cjson iarmmgrs wpeframework readline"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'gstreamer1', 'gstreamer1.0  gstreamer1.0-plugins-base', 'gstreamer gst-plugins-base', d)}"
RDEPENDS_${PN} +=  "${@bb.utils.contains('DISTRO_FEATURES', 'rdk_svp', 'gst-svp-ext', '', d)}"
DEPENDS += " wpe-webkit"
DEPENDS += " wpeframework-clientlibraries"
RDEPENDS:${PN} += "devicesettings"
DEPENDS:append = " virtual/vendor-gst-drm-plugins essos "
NO_RECOMMENDATIONS = "1"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI = "${CMF_GITHUB_ROOT}/middleware-player-interface;${CMF_GITHUB_SRC_URI_SUFFIX};name=player-interface"

S = "${WORKDIR}/git"

DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'webkitbrowser-plugin', '${WPEWEBKIT}', '', d)}"

DEPENDS:append = " virtual/vendor-secapi2-adapter "

require player-interface-common.inc

PACKAGECONFIG:append = " playready widevine clearkey"

DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec', '-DCMAKE_GST_SUBTEC_ENABLED=1 ', '', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'wpe_security_util_disable', ' -DDISABLE_SECURITY_TOKEN=ON ', '', d)}"

EXTRA_OECMAKE += "  -DCMAKE_WPEFRAMEWORK_REQUIRED=1 "

EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'sec_manager', ' -DCMAKE_USE_SECMANAGER=1 ', '', d)}"
EXTRA_OECMAKE += " -DCMAKE_WPEWEBKIT_WATERMARK_JSBINDINGS=1 "

RDEPENDS:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec', 'packagegroup-subttxrend-app', '', d)}"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec', 'closedcaption-hal-headers virtual/vendor-dvb virtual/vendor-closedcaption-hal', '', d)}"

#Ethan log is implemented by Dobby hence enabling it.
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_rialto', 'dobby', '', d)}"
PACKAGES = "${PN} ${PN}-dev ${PN}-dbg"

FILES:${PN} += "${libdir}/lib*.so"
FILES:${PN} += "${libdir}/player-interface/lib*.so"
FILES:${PN} +="${libdir}/gstreamer-1.0/lib*.so"
FILES:${PN}-dbg +="${libdir}/gstreamer-1.0/.debug/*"

INSANE_SKIP:${PN} = "dev-so"
CXXFLAGS += "-DCMAKE_LIGHTTPD_AUTHSERVICE_DISABLE=1 -I${STAGING_DIR_TARGET}${includedir}/WPEFramework/ "

CXXFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'wpe_security_util_disable', '', ' -lWPEFrameworkSecurityUtil ', d)}"
EXTRA_OECMAKE += " -DCMAKE_LIGHTTPD_AUTHSERVICE_DISABLE=1 "


#required for specific products but for now distro is available only for UK 
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_UK', ' -DENABLE_USE_SINGLE_PIPELINE=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_IT', ' -DENABLE_USE_SINGLE_PIPELINE=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_DE', ' -DENABLE_USE_SINGLE_PIPELINE=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_AU', ' -DENABLE_USE_SINGLE_PIPELINE=1', '', d)}"

# Enable PTS restamp feature
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_UK', ' -DENABLE_PTS_RESTAMP=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_IT', ' -DENABLE_PTS_RESTAMP=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_DE', ' -DENABLE_PTS_RESTAMP=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_AU', ' -DENABLE_PTS_RESTAMP=1', '', d)}"

SRC_URI += " file://libplayerfbinterface.pc"
SRC_URI += " file://libbaseconversion.pc"
SRC_URI += " file://libplayerlogmanager.pc"
SRC_URI += " file://libplayergstinterface.pc"
SRC_URI += " file://libsubtec.pc"

do_install:append() {
    install -d ${D}${libdir}/pkgconfig
    install -m0644 ${WORKDIR}/libplayerfbinterface.pc ${D}${libdir}/pkgconfig/
    install -m0644 ${WORKDIR}/libbaseconversion.pc ${D}${libdir}/pkgconfig/
    install -m0644 ${WORKDIR}/libplayerlogmanager.pc ${D}${libdir}/pkgconfig/
    install -m0644 ${WORKDIR}/libplayergstinterface.pc ${D}${libdir}/pkgconfig/
    install -m0644 ${WORKDIR}/libsubtec.pc ${D}${libdir}/pkgconfig/
}
