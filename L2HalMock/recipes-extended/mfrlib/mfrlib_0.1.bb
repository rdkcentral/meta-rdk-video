SUMMARY = "L2HalMock stub for mfrlib"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PROVIDES += "virtual/mfrlib"
RPROVIDES:${PN} += "virtual/mfrlib"
PACKAGE_ARCH = "${MACHINE_ARCH}"

do_configure() {
    :
}
do_compile() {
    :
}
do_install() {
    :
}
ALLOW_EMPTY:${PN} = "1"
