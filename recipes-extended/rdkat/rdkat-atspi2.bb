SUMMARY = "RDK-AT Bridge (AT-SPI2)"

DESCRIPTION = "This component provides an accessibility implementation to \
support Text-To-Speech (TTS) using the AT-SPI2 protocol (D-Bus) to have access \
to accessibility related information and send it to TTS component."

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

SRC_URI = "${RDK_GENERIC_ROOT_GIT}/rdkat/generic;protocol=${RDK_GIT_PROTOCOL};branch=25Q2_sprint"
SRCREV = "${AUTOREV}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

S = "${WORKDIR}/git"

EXTRA_OEMAKE += "SYSROOT_INCLUDES_DIR=${STAGING_INCDIR}"
EXTRA_OEMAKE += "SYSROOT_LIBS_DIR=${STAGING_LIBDIR}"

do_compile () {
    oe_runmake -C ${S} -f Makefile.atspi2
}

do_install () {
    export INSTALL_PATH=${D}
    oe_runmake -C ${S} -f Makefile.atspi2 install
}

FILES_${PN} += "${libdir}/*.so"

DEPENDS += "glib-2.0 tts"
RDEPENDS_${PN} += "glib-2.0 tts"
