SUMMARY = "L2HalMock aggregate build target"
DESCRIPTION = "Aggregate BitBake target for the L2HalMock plugin stack. Start with HdmiCecSource and extend this recipe as more plugins are added."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit allarch

ALLOW_EMPTY:${PN} = "1"

DEPENDS = " \
    hdmicec \
    entservices-hdmicecsource \
"

RDEPENDS:${PN} = " \
    hdmicec \
    entservices-hdmicecsource \
"

do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[noexec] = "1"
