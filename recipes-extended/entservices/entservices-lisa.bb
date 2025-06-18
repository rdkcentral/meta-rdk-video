SUMMARY  = "RDK services LISA plugin"
LICENSE  = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=e3fc50a88d0a364313df4b21ef20c29e"
S = "${WORKDIR}/git"

PACKAGE_ARCH ?= "${MIDDLEWARE_ARCH}"
PV ?= "3.0"
PR ?= "r1"

inherit cmake pkgconfig coverity

DEPENDS += "wpeframework boost curl libarchive wpeframework-tools-native entservices-apis"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
SRC_URI = "${CMF_GITHUB_ROOT}/LISA;${CMF_GITHUB_SRC_URI_SUFFIX}"

TOOLCHAIN = "gcc"
EXTRA_OECMAKE += "-DCMAKE_SYSROOT=${STAGING_DIR_HOST}"
EXTRA_OECMAKE += "-DBUILD_REFERENCE=${SRCREV}"
EXTRA_OECMAKE += "-DPLUGIN_LISA_IMAGE_PATH=${LISA_IMAGE_PATH}"
EXTRA_OECMAKE += "-DPLUGIN_LISA_APPDATA_PATH=${LISA_APPDATA_PATH}"

# 17 Jun 2025 or head
SRCREV = "ced76740507d39e5078e9e1e30936cb7addff438"

OECMAKE_TARGET_COMPILE = "WPEFrameworkLISA"
OECMAKE_TARGET_INSTALL = "LISA/install"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/*.so ${datadir}/WPEFramework/*"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"

require include/entservices-lisa-dac-config.inc
