SUMMARY = "Middleware Player Interface layer for AAMP"
DESCRIPTION = "This layer provides the Entos Player Firebolt Interface library for AAMP integration. It sources from https://github.com/rdkcentral/middleware-player-interface."
HOMEPAGE = "https://github.com/rdkcentral/middleware-player-interface"
LICENSE = "CLOSED"

PV ?= "1.0.0"
PR ?= "r0"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI = "git://github.com/rdkcentral/middleware-player-interface.git;protocol=https;branch=feature/RDKEMW-4040"
SRCREV = "11480e59e7f2a677b308f928d421e9520c187bf8"

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

FILES:${PN} += "${libdir}/lib*.so"
FILES:${PN} += "${libdir}/player-interface/lib*.so"

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
