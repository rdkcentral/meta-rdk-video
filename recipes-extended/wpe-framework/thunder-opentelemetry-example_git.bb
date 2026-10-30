SUMMARY = "Thunder OpenTelemetry example plugins and clients"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "1.0.0"
PR = "r0"

S = "${WORKDIR}/git/examples"
inherit cmake pkgconfig

# The ThunderOpenTelemetry example lives in the ThunderNanoServices repo. Override
# BRANCH/SRCREV to match the branch that carries the OpenTelemetry example.
BRANCH ?= "master"
SRC_URI = "git://github.com/rdkcentral/ThunderNanoServices.git;protocol=https;branch=${BRANCH}"

SRCREV = "${AUTOREV}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

# wpeframework provides find_package(Thunder) plus the OpenTelemetry library
# (built when WPEFRAMEWORK_OPENTELEMETRY=ON).
DEPENDS += "wpeframework wpeframework-tools-native"
RDEPENDS:${PN} += "wpeframework"

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DCMAKE_SYSROOT=${STAGING_DIR_HOST} \
    -DEXAMPLE_THUNDER_OPEN_TELEMETRY=ON \
"

CXXFLAGS += " -Wall -Werror "
CXXFLAGS:remove_morty = " -Wall -Werror "
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += " \
    ${libdir}/wpeframework/plugins/*.so \
    ${bindir}/CalculatorClientCOMRPC \
    ${bindir}/CalculatorClientJSONRPC \
    ${datadir}/WPEFramework/* \
    ${sysconfdir}/WPEFramework/plugins/*.json \
"

FILES:${PN}-dbg += " \
    ${libdir}/wpeframework/plugins/.debug/ \
"

INSANE_SKIP:${PN} += "dev-so"
