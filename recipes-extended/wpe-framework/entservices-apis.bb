SUMMARY = "entservices-apis"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d8927f3331d2b3e321b7dd1925166d25"
PV = "3.1.0"
PR = "r0"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit python3native cmake pkgconfig


DEPENDS = "wpeframework wpeframework-tools-native"

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-apis;${CMF_GITHUB_SRC_URI_SUFFIX};name=entservices-apis"

SRC_URI += "file://RDKEMW-1007.patch"


# Tag 3.1.0
SRCREV_entservices-apis = "19c5b062d6b09c7e39c3fb0a0ca00f0b12f7ec91"

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
        install -m 0644 ${D}${includedir}/WPEFramework/interfaces/IDRM.h ${D}${includedir}/cdmi.h
    fi
}

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/* ${datadir}/WPEFramework/* ${PKG_CONFIG_DIR}/*.pc"
FILES:${PN}-dev += "${libdir}/cmake/*"
FILES:${PN}-dbg += "${libdir}/wpeframework/proxystubs/.debug/"
FILES:${PN} += "${includedir}/cdmi.h"

INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"
