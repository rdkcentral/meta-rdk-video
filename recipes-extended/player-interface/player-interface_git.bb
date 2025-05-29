SUMMARY = "Middleware Player Interface layer for Player"
DESCRIPTION = "This layer provides the Player Firebolt Interface library for Player integration."
LICENSE = "CLOSED"

PV ?= "1.0.0"
PR ?= "r0"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI = "${CMF_GITHUB_ROOT}/middleware-player-interface;${CMF_GITHUB_SRC_URI_SUFFIX};name=player-interface"

S = "${WORKDIR}/git"

inherit cmake
inherit pkgconfig

require player-interface-common.inc

# Pass install directories to cmake
EXTRA_OECMAKE += " -DCMAKE_INSTALL_LIBDIR=${libdir} -DCMAKE_INSTALL_INCLUDEDIR=${includedir} "

# Add any external dependencies required by the interface layer.
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'gstreamer1', 'gstreamer1.0  gstreamer1.0-plugins-base', 'gstreamer gst-plugins-base', d)}"
DEPENDS += "iarmmgrs wpeframework wpe-webkit wpeframework-clientlibraries"

DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'webkitbrowser-plugin', '${WPEWEBKIT}', '', d)}"
DEPENDS:append = " virtual/vendor-gst-drm-plugins essos "
RDEPENDS:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec', 'packagegroup-subttxrend-app', '', d)}"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec', 'closedcaption-hal-headers virtual/vendor-dvb virtual/vendor-closedcaption-hal', '', d)}"

FILES:${PN} += "${libdir}/lib*.so"
FILES:${PN} += "${libdir}/player-interface/lib*.so"
FILES:${PN} +="${libdir}/gstreamer-1.0/lib*.so"
FILES:${PN}-dbg +="${libdir}/gstreamer-1.0/.debug/*"

CXXFLAGS += "-I${STAGING_DIR_TARGET}${includedir}/WPEFramework/ -lWPEFrameworkSecurityUtil"

PACKAGES = "${PN} ${PN}-dev ${PN}-dbg"

SRC_URI += " file://libEntosPlayerFireboltInterface.pc"
SRC_URI += " file://libBaseConversion.pc"
SRC_URI += " file://libPlayerLogManager.pc"

do_install:append() {
    install -d ${D}${libdir}/pkgconfig
    install -m0644 ${WORKDIR}/libEntosPlayerFireboltInterface.pc ${D}${libdir}/pkgconfig/
    install -m0644 ${WORKDIR}/libBaseConversion.pc ${D}${libdir}/pkgconfig/
    install -m0644 ${WORKDIR}/libPlayerLogManager.pc ${D}${libdir}/pkgconfig/
}
