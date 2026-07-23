DESCRIPTION = "RDK Binder example module"
SECTION = "BinderModule"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

SRC_URI = "${RDKCENTRAL_GITHUB_ROOT}/linux_binder_idl;${RDKCENTRAL_GITHUB_SRC_URI_SUFFIX}"

PV ?= "1.0.0"
PR ?= "r0"
SRCREV ?= "1.0.0"

DEPENDS = "libbinder"
RDEPENDS:${PN} = "libbinder"

S = "${WORKDIR}/git"
OECMAKE_SOURCEPATH = "${WORKDIR}/git/example"

inherit cmake

FILES:${PN} += "${libdir}/* \
                ${bindir}/* \
               "
INSANE_SKIP:${PN}-dev += "dev-elf"
INSANE_SKIP:${PN} += "dev-deps"
FILES_SOLIBSDEV = ""
