SUMMARY = "xr-voice-sdk provides a shared library that controls how and where speech gets distributed."
DESCRIPTION = "TBD."
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV ?= "1.0.1"
PR ?= "r0"

PACKAGE_ARCH  = "${MIDDLEWARE_ARCH}"

SRCREV = "${SRCREV:pn-xr-voice-sdk}"
SRCREV_FORMAT = "xr-voice-sdk"

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

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
   install -d ${D}${includedir}
   install -m 644 ${S}/src/xr-mq/xr_mq.h               ${D}${includedir}
   install -m 644 ${S}/src/xr-fdc/xr_fdc.h             ${D}${includedir}
   install -m 644 ${S}/src/xr-timer/xr_timer.h         ${D}${includedir}
   install -m 644 ${S}/src/xr-timestamp/xr_timestamp.h ${D}${includedir}
   install -m 644 ${S}/src/xr-sm-engine/xr_sm_engine.h ${D}${includedir}
   install -m 644 ${S}/src/xr-audio/xraudio.h          ${D}${includedir}
   install -m 644 ${S}/src/xr-audio/xraudio_hal.h      ${D}${includedir}
   install -m 644 ${S}/src/xr-audio/xraudio_eos.h      ${D}${includedir}
   install -m 644 ${S}/src/xr-audio/xraudio_dga.h      ${D}${includedir}
   install -m 644 ${S}/src/xr-audio/xraudio_kwd.h      ${D}${includedir}
   install -m 644 ${S}/src/xr-audio/xraudio_sdf.h      ${D}${includedir}
   install -m 644 ${S}/src/xr-audio/xraudio_ovc.h      ${D}${includedir}
   install -m 644 ${S}/src/xr-audio/xraudio_ppr.h      ${D}${includedir}
   install -m 644 ${S}/src/xr-audio/xraudio_common.h   ${D}${includedir}
   install -m 644 ${S}/src/xr-audio/xraudio_platform.h ${D}${includedir}
   install -m 644 ${S}/src/xr-audio/xraudio_version.h  ${D}${includedir}
   install -m 644 ${S}/scripts/vsdk_json_combine.py    ${D}${includedir}
   install -m 644 ${S}/scripts/vsdk_json_to_header.py  ${D}${includedir}
   install -m 644 ${S}/src/xr-logger/rdkx_logger.h     ${D}${includedir}

   ${S}/scripts/rdkx_logger_modules_to_c.py ${S}/src/xr-logger/rdkv/rdkx_logger_modules.json rdkx_logger_modules
   install -m 644 rdkx_logger_modules.h  ${D}${includedir}
}

ALLOW_EMPTY:${PN} = "1"


