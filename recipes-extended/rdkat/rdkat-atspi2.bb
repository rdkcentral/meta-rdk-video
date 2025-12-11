SUMMARY = "RDK-AT Bridge (AT-SPI2)"

DESCRIPTION = "This component provides an accessibility implementation to \
support Text-To-Speech (TTS) using the AT-SPI2 protocol (D-Bus) to have access \
to accessibility related information and send it to TTS component."

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"


SRC_URI = "${CMF_GITHUB_ROOT}/rdkat;${CMF_GITHUB_SRC_URI_SUFFIX}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

S = "${WORKDIR}/git"

EXTRA_OEMAKE += "SYSROOT_INCLUDES_DIR=${STAGING_INCDIR}"
EXTRA_OEMAKE += "SYSROOT_LIBS_DIR=${STAGING_LIBDIR}"

inherit pkgconfig

do_compile () {
    oe_runmake -C ${S} -f Makefile.atspi2
    oe_runmake -C ${S} -f Makefile.atspi2 tests
}

do_install () {
    export INSTALL_PATH=${D}
    oe_runmake -C ${S} -f Makefile.atspi2 install
    oe_runmake -C ${S} -f Makefile.atspi2 install-tests
}

PACKAGES += "${PN}-tests"

FILES:${PN} = "${libdir}/*.so*"
FILES:${PN}-dbg = "${libdir}/.debug/*.so*"
FILES:${PN}-tests = "${bindir}/run-tests"

RDEPENDS:${PN}-tests += "${PN}"

DEPENDS += "glib-2.0 tts gtest gmock"
RDEPENDS:${PN} += "glib-2.0 tts"
