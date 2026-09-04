SUMMARY = "ENTServices helpers common shared library"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "1.0.5_Test"
PR = "r1"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-helpers;${CMF_GITHUB_SRC_URI_SUFFIX}"

SRCREV = "ef96fc8a37148020dd74b4a8a4fb3a4424d02a1c"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

DEPENDS += "wpeframework wpeframework-tools-native"
RDEPENDS:${PN} += "wpeframework"
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/rdk/ds "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/rdk/ds-hal "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/rdk/iarmbus"
CXXFLAGS += "-DUSE_THUNDER_COMMUNICATION"

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
"

# ----------------------------------------------------------------------------

PACKAGECONFIG ?= "helpers"

HELPERS_DEPS = "entservices-apis iarmbus devicesettings virtual/vendor-devicesettings-hal"
HELPERS_DEPS:vdevice_x86-64-mw = "entservices-apis iarmbus"

HELPERS_RDEPS = "entservices-apis devicesettings"
HELPERS_RDEPS:vdevice_x86-64-mw = "entservices-apis"

PACKAGECONFIG[helpers] = "-DPLUGIN_HELPERS=ON,-DPLUGIN_HELPERS=OFF,${HELPERS_DEPS},${HELPERS_RDEPS}"

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
