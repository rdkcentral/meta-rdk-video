SUMMARY = "entservices-apis"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d8927f3331d2b3e321b7dd1925166d25"
PV = "2.8.0"
PR = "r0"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit python3native cmake pkgconfig


DEPENDS = "wpeframework wpeframework-tools-native"

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-apis;${CMF_GITHUB_SRC_URI_SUFFIX};name=entservices-apis"

SRC_URI += "file://RDKEMW-1007.patch"


# Tag 2.8.0
SRCREV_entservices-apis = "216a156332baa1bf9943d2d4b3673f33c1101e6b"

S = "${WORKDIR}/git"
TOOLCHAIN = "gcc"
# ----------------------------------------------------------------------------

EXTRA_OECMAKE += " \
    -DBUILD_SHARED_LIBS=ON \
    -DBUILD_REFERENCE=${SRCREV} \
    -DCMAKE_SYSROOT=${STAGING_DIR_HOST} \
"

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/* ${datadir}/WPEFramework/* ${PKG_CONFIG_DIR}/*.pc"
FILES:${PN}-dev += "${libdir}/cmake/*"
FILES:${PN}-dbg += "${libdir}/wpeframework/proxystubs/.debug/"

INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"
