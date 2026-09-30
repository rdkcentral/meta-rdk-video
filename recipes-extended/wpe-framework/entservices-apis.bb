SUMMARY = "entservices-apis"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d8927f3331d2b3e321b7dd1925166d25"
PV = "4.1.6"
PR = "r0"
SRCREV_entservices-apis = "9fc6ffb937d538e6d3029f2b6cfead3288e83dde"


PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit python3native cmake pkgconfig


DEPENDS = "${THUNDER_NAMESPACE_LC} ${THUNDER_NAMESPACE_LC}-tools-native"

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-apis;${CMF_GITHUB_SRC_URI_SUFFIX};name=entservices-apis"

IAUTHSERVICE_PATCH = "${@bb.utils.contains('DISTRO_FEATURES', 'thunder_5', 'file://RDKEMW-1007-Thunder5.patch', 'file://RDKEMW-1007.patch', d)}"
SRC_URI += "${IAUTHSERVICE_PATCH}"
SRC_URI += "file://entservices-apis-fps-ocdm.patch"
SRC_URI += "file://idrm.patch"


S = "${WORKDIR}/git"
TOOLCHAIN = "gcc"
# ----------------------------------------------------------------------------

EXTRA_OECMAKE += " \
    -DBUILD_SHARED_LIBS=ON \
    -DBUILD_REFERENCE=${SRCREV} \
    -DCMAKE_SYSROOT=${STAGING_DIR_HOST} \
"

# ----------------------------------------------------------------------------

do_install:append() {
    if ${@bb.utils.contains("DISTRO_FEATURES", "opencdm", "true", "false", d)}
    then
        install -m 0644 ${D}${includedir}/${THUNDER_NAMESPACE}/interfaces/IDRM.h ${D}${includedir}/cdmi.h
    fi
}

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/* ${datadir}/${THUNDER_NAMESPACE}/* ${PKG_CONFIG_DIR}/*.pc"
FILES:${PN}-dev += "${libdir}/cmake/*"
FILES:${PN}-dbg += "${libdir}/${THUNDER_NAMESPACE_LC}/proxystubs/.debug/"
FILES:${PN} += "${includedir}/cdmi.h"

INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"
