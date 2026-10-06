SUMMARY = "PowerController client library (libWPEFrameworkPowerController)"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=be650d9617f9f9d24bcaccf78a97b28b"

PV = "1.4.9"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-powermanager;${CMF_GITHUB_SRC_URI_SUFFIX}"
SRCREV = "d03328b5fa0b36003e2ad1330526b5c09a8b7a73"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"

DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"

DEPENDS = "entservices-apis wpeframework wpeframework-tools-native"

# Builds only the PowerController client library. The PowerManager plugin is
# disabled here because it depends on iarmmgrs, which links this library.
EXTRA_OECMAKE += " \
    -DPOWERCONTROLLER=ON \
    -DPLUGIN_POWERMANAGER=OFF \
    -DBUILD_SHARED_LIBS=ON \
    -DBUILD_REFERENCE=${SRCREV} \
    ${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)} \
"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so ${PKG_CONFIG_DIR}/*.pc"
FILES:${PN}-dev += "${includedir}/WPEFramework/powercontroller ${libdir}/cmake/*"

INSANE_SKIP:${PN} += "dev-so"
