# Version and SRCREV for this component is handled in conf/include/rdk-headers-versions.inc

SUMMARY = "This recipe provides xr-voice-sdk FFV interface headers"
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

inherit allarch

SRC_URI = "${CMF_GITHUB_ROOT}/xr-voice-sdk;${CMF_GITHUB_SRC_URI_SUFFIX};name=xr-voice-sdk-ffv-headers"

S = "${WORKDIR}/git"

# this is a Header package only, nothing to build
do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
   install -d ${D}${includedir}/rdk/halif/vsdk/ffv
   install -m 0644 ${S}/src/xr-audio/xraudio.h          ${D}${includedir}/rdk/halif/vsdk/ffv
   install -m 0644 ${S}/src/xr-audio/xraudio_hal.h      ${D}${includedir}/rdk/halif/vsdk/ffv
   install -m 0644 ${S}/src/xr-audio/xraudio_eos.h      ${D}${includedir}/rdk/halif/vsdk/ffv
   install -m 0644 ${S}/src/xr-audio/xraudio_dga.h      ${D}${includedir}/rdk/halif/vsdk/ffv
   install -m 0644 ${S}/src/xr-audio/xraudio_kwd.h      ${D}${includedir}/rdk/halif/vsdk/ffv
   install -m 0644 ${S}/src/xr-audio/xraudio_sdf.h      ${D}${includedir}/rdk/halif/vsdk/ffv
   install -m 0644 ${S}/src/xr-audio/xraudio_ovc.h      ${D}${includedir}/rdk/halif/vsdk/ffv
   install -m 0644 ${S}/src/xr-audio/xraudio_ppr.h      ${D}${includedir}/rdk/halif/vsdk/ffv
   install -m 0644 ${S}/src/xr-audio/xraudio_common.h   ${D}${includedir}/rdk/halif/vsdk/ffv
   install -m 0644 ${S}/src/xr-audio/xraudio_platform.h ${D}${includedir}/rdk/halif/vsdk/ffv
   install -m 0644 ${S}/src/xr-audio/xraudio_version.h  ${D}${includedir}/rdk/halif/vsdk/ffv
   install -m 0755 ${S}/scripts/vsdk_json_combine.py    ${D}${includedir}/rdk/halif/vsdk/ffv
   install -m 0755 ${S}/scripts/vsdk_json_to_header.py  ${D}${includedir}/rdk/halif/vsdk/ffv
}

