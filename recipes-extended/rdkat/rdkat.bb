SUMMARY = "RDK-AT Bridge"

DESCRIPTION = "This component installs a hook into atk through which it\
detects the DOM events and provides the ARIA information to TTS"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"


SRC_URI = "${CMF_GITHUB_ROOT}/rdkat;${CMF_GITHUB_SRC_URI_SUFFIX}"

SRCREV = "e52ebe05b6703dff7ca700fd286d84c0c72c41ea"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

DEPENDS += "atk tts"
RDEPENDS:${PN} += "tts"

S = "${WORKDIR}/git"

EXTRA_OEMAKE += "SYSROOT_INCLUDES_DIR=${STAGING_INCDIR}"
EXTRA_OEMAKE += "SYSROOT_LIBS_DIR=${STAGING_LIBDIR}"

do_compile () {
    oe_runmake -C ${S} -f Makefile
}

do_install () {
    export INSTALL_PATH=${D}
    oe_runmake -C ${S} -f Makefile install
}

INSANE_SKIP:${PN} = "dev-so"
FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so"

INSANE_SKIP:${PN} += "ldflags"
