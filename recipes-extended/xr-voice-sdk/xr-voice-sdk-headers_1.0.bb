SUMMARY = "xr-voice-sdk provides a shared library that controls how and where speech gets distributed."
DESCRIPTION = "TBD."
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

include xr-voice-sdk.inc

PACKAGE_ARCH   = "${MIDDLEWARE_ARCH}"
PV            := "${XR_VOICE_SDK_PV}"
PR            := "${XR_VOICE_SDK_PR}"
SRCREV        := "${XR_VOICE_SDK_SRCREV}"
SRCREV_FORMAT  = "xr-voice-sdk"

SRC_URI = "${CMF_GITHUB_ROOT}/xr-voice-sdk;${CMF_GITHUB_SRC_URI_SUFFIX};name=xr-voice-sdk"

S = "${WORKDIR}/git"

DEPENDS = "gperf-native util-linux jansson"

inherit cmake

EXTRA_OECMAKE:append = " -DCMAKE_PROJECT_VERSION=${PV} -DINSTALL_INTERNAL_HEADERS=ON"

# Header list is maintained in the xr-voice-sdk CMakeLists.txt; only run the internal headers install target
OECMAKE_TARGET_INSTALL = "install_internal_headers"

do_compile[noexec] = "1"

ALLOW_EMPTY:${PN} = "1"


