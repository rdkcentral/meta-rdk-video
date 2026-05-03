SUMMARY = "ENTServices helpers common shared library"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "1.0.0"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-helpers;${CMF_GITHUB_SRC_URI_SUFFIX}"

SRCREV = "${AUTOREV}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

DEPENDS += "wpeframework wpeframework-tools-native"
RDEPENDS:${PN} += "wpeframework"

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
"

# ----------------------------------------------------------------------------

PACKAGECONFIG ?= "helpers"

PACKAGECONFIG[helpers] = "-DPLUGIN_HELPERS=ON,-DPLUGIN_HELPERS=OFF,,"

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += " \
    ${libdir}/wpeframework/plugins/*.so \
    ${libdir}/*.so \
    ${includedir}/wpeframework/helpers/* \
    ${libdir}/cmake/WPEFrameworkHelpers/* \
"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
