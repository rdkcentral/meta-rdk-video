SUMMARY = "PowerController client library (libWPEFrameworkPowerController)"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=be650d9617f9f9d24bcaccf78a97b28b"

PV = "1.4.9"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-powermanager;${CMF_GITHUB_SRC_URI_SUFFIX}"
SRCREV = "2b8212b80ad5d799443e0d11446e2df9e0f19c74"

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
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/*.so ${datadir}/WPEFramework/*"

INSANE_SKIP:${PN} += "dev-so"
