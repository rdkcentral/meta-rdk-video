# Common part of recipes built from the entservices-apis repo:
#   entservices-apis      : all apis/ except the standalone ones (STANDALONE_APIS)
#   one recipe per standalone API : a single apis/<dir> (STANDALONE_API, via entservices-apis-standalone.inc)
#
# By default all of them build the same release (ENTSERVICES_APIS_VERSION), so splitting
# an API into its own recipe does not change what is built. A standalone API recipe can
# override PV and SRCREV_entservices-apis to move that API without bumping the API set
# used by all other entservices.

# TODO: must be a release with STANDALONE_API/STANDALONE_APIS support
ENTSERVICES_APIS_VERSION = "4.3.4"

PV = "${ENTSERVICES_APIS_VERSION}"
SRCREV_entservices-apis = "${ENTSERVICES_APIS_VERSION}"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d8927f3331d2b3e321b7dd1925166d25"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit python3native cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-apis;${CMF_GITHUB_SRC_URI_SUFFIX};name=entservices-apis"

S = "${WORKDIR}/git"
TOOLCHAIN = "gcc"

EXTRA_OECMAKE += " \
    -DBUILD_SHARED_LIBS=ON \
    -DBUILD_REFERENCE=${SRCREV} \
    -DCMAKE_SYSROOT=${STAGING_DIR_HOST} \
"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/* ${datadir}/WPEFramework/* ${PKG_CONFIG_DIR}/*.pc"
FILES:${PN}-dev += "${libdir}/cmake/*"
FILES:${PN}-dbg += "${libdir}/wpeframework/proxystubs/.debug/"

INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"
