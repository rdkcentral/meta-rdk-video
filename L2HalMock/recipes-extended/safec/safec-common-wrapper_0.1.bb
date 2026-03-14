SUMMARY = "L2HalMock stub for safec-common-wrapper"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# Packages downstream use -DSAFEC_DUMMY_API so no real safec library is needed.
# This empty package satisfies the build-time DEPENDS without providing any real library.

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
