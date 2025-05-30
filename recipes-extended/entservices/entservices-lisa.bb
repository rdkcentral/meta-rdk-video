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
SRC_URI = "${CMF_GITHUB_ROOT}/LISA;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GITHUB_MAIN_BRANCH}"
SRC_URI += "file://0100-update-config.patch;apply=no"

do_lisa_patches() {
    cd ${S}
    if [ -e ${WORKDIR}/0100-update-config.patch ]; then
        if [ ! -e lisa_patch_0100_applied ]; then
            bbnote "Patching 0100-update-config.patch"
            patch -p1 < ${WORKDIR}/0100-update-config.patch
            touch lisa_patch_0100_applied
        fi
    fi
}
addtask lisa_patches after do_unpack before do_configure

TOOLCHAIN = "gcc"
EXTRA_OECMAKE += "-DCMAKE_SYSROOT=${STAGING_DIR_HOST}"
EXTRA_OECMAKE += "-DBUILD_REFERENCE=${SRCREV}"

# 18 Dec 2023 or head
SRCREV = "f0888c8f5a3919540952fb24cc466cfc97d08f07"

OECMAKE_TARGET_COMPILE = "WPEFrameworkLISA"
OECMAKE_TARGET_INSTALL = "LISA/install"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/*.so ${datadir}/WPEFramework/*"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"

require include/entservices-lisa-dac-config.inc
