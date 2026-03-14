SUMMARY = "L2HalMock stub for vendor-pwrmgr-hal"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PROVIDES += "virtual/vendor-pwrmgr-hal"
RPROVIDES:${PN} += "virtual/vendor-pwrmgr-hal"
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
