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

FILES:${PN} += "${includedir}/xr_mq.h \

                ${includedir}/xr_fdc.h \
                ${includedir}/xr_timer.h \
                ${includedir}/xr_timestamp.h \
                ${includedir}/xr_sm_engine.h \
                ${includedir}/xraudio.h \
                ${includedir}/xraudio_hal.h \
                ${includedir}/xraudio_eos.h \
                ${includedir}/xraudio_dga.h \
                ${includedir}/xraudio_kwd.h \
                ${includedir}/xraudio_sdf.h \
                ${includedir}/xraudio_ovc.h \
                ${includedir}/xraudio_ppr.h \
                ${includedir}/xraudio_common.h \
                ${includedir}/xraudio_platform.h \
                ${includedir}/xraudio_version.h \
                ${includedir}/vsdk_json_combine.py \
                ${includedir}/vsdk_json_to_header.py \
                ${includedir}/rdkx_logger.h \
                ${includedir}/rdkx_logger_modules.h \
               "

inherit cmake

EXTRA_OECMAKE = "-DCMAKE_SYSROOT=${RECIPE_SYSROOT} -DCMAKE_PROJECT_VERSION=${PV} \
                 -DSTAGING_BINDIR_NATIVE=${STAGING_BINDIR_NATIVE}"

# Install only the internal-headers cmake component (sub-component interfaces
# from the great component consolidation; not part of the public xr-voice-sdk API).
# The scripts are not cmake targets so they are installed manually.
do_install() {
   cmake --install ${B} --component internal-headers --prefix ${D}${prefix}
   install -d ${D}${includedir}
   install -m 755 ${S}/scripts/vsdk_json_combine.py   ${D}${includedir}
   install -m 755 ${S}/scripts/vsdk_json_to_header.py ${D}${includedir}
}

ALLOW_EMPTY:${PN} = "1"


