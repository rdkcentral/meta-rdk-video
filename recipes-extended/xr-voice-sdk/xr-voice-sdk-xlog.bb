DESCRIPTION = "xr-voice-sdk-xlog provides a shared library with logging for the vendor layer."
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

# xlog ships from the same xr-voice-sdk repo, so track the same version/SRCREV.
include xr-voice-sdk.inc

SRC_URI = "${CMF_GITHUB_ROOT}/xr-voice-sdk;${CMF_GITHUB_SRC_URI_SUFFIX};name=xr-voice-sdk-xlog"

SRCREV        := "${XR_VOICE_SDK_SRCREV}"
SRCREV_FORMAT = "xr-voice-sdk-xlog"

S = "${WORKDIR}/git"

DEPENDS = "gperf-native jansson"

inherit cmake pkgconfig coverity

DEPENDS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec', '', d)}"
CFLAGS:append  = "${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --cflags libsafec`', ' -DSAFEC_DUMMY_API', d)}"
LDFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --libs libsafec`', '', d)}"

# Configuration Options

EXTRA_OECMAKE:append = " -DCMAKE_SYSROOT=${RECIPE_SYSROOT} -DCMAKE_PROJECT_VERSION=${PV}"
EXTRA_OECMAKE:append = " -DSTAGING_BINDIR_NATIVE=${STAGING_BINDIR_NATIVE}"
EXTRA_OECMAKE:append = " -DVSDK_VENDOR_XLOG=ON"

SOLIBS=".so"
FILES_SOLIBSDEV=""
FILES:${PN} += "${libdir}/libxr-voice-sdk-xlog.so*"
INSANE_SKIP:${PN}:append = " dev-so"
