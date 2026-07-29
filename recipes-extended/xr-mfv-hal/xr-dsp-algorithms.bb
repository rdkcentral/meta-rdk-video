#
# ============================================================================
# COMCAST C O N F I D E N T I A L AND PROPRIETARY
# ============================================================================
# This file and its contents are the intellectual property of Comcast.  It may
# not be used, copied, distributed or otherwise  disclosed in whole or in part
# without the express written permission of Comcast.
# ============================================================================
# Copyright (c) 2014 Comcast. All rights reserved.
# ============================================================================
#
DESCRIPTION = "Voice SDK xraudio FFV algorithms component for llama platform"
SECTION = "console/utils"

# COMCAST C O N F I D E N T I A L AND PROPRIETARY
LICENSE = "CLOSED"

SRC_URI =  "git://git@github.com/rdk-e/xr-dsp-algorithms-cpc.git;protocol=ssh;nobranch=1"

# feature/RDKEVD-4129_llama_ffv_hal
SRCREV = "eed585847d5dce72994d04e207a1c735ff2a6fa9"

SRCREV_FORMAT = "vsdk-xraudio-ffv-algorithms"

S = "${WORKDIR}/git"

# Workaround for package error "canonicalization unexpectedly shrank by one character"
PACKAGE_DEBUG_SPLIT_STYLE = "debug-without-src"

DEPENDS = "libbsd util-linux jansson xr-voice-sdk-ffv-headers xr-voice-sdk-xlog"

inherit autotools pkgconfig

INCLUDE_DIRS = " \
    -I${S}/src/include \
    -I${S}/src/private/include/arm \
    -I${PKG_CONFIG_SYSROOT_DIR}/usr/include/ \
    -I${PKG_CONFIG_SYSROOT_DIR}/usr/include/rdk/halif/vsdk/ffv \
    -I${PKG_CONFIG_SYSROOT_DIR}/usr/lib \
    -I${PKG_CONFIG_SYSROOT_DIR}/usr/xr_dsp/include \
    -I${S}/src/dga/src \
    -I${S}/src/vrexgain/source \
    -I${S}/src/ppr/src \
    "

CFLAGS:append = " -std=c11 -fPIC -D_REENTRANT -rdynamic -Wall -Werror ${INCLUDE_DIRS} -DXLOG_MODULE_ID=XLOG_MODULE_ID_XRAUDIO"
CXXFLAGS:append = " -std=c++11 -fPIC -D_REENTRANT -rdynamic -Wall -Werror ${INCLUDE_DIRS} -DXLOG_MODULE_ID=XLOG_MODULE_ID_XRAUDIO"
LDFLAGS:append = " -lbsd -lpthread -lxr-voice-sdk-xlog"

EXTRA_OECONF:append = " GIT_BRANCH=${RDK_GIT_BRANCH}"
EXTRA_OEMAKE:append = " XR_DSP_ALGORITHMS_PV=${VSDK_XRAUDIO_FFV_ALGORITHMS_PV}"
EXTRA_OEMAKE:append = " XR_DSP_ALGORITHMS_PR=${VSDK_XRAUDIO_FFV_ALGORITHMS_PR}"

SOLIBS = ".so"
FILES_SOLIBSDEV = ""
FILES:${PN}:append = " /vendor/lib/libxraudio-ffv-algorithms.so*"
INSANE_SKIP:${PN}:append = " dev-so"

# libxraudio-ffv-algorithms.so is installed to the non-standard /vendor/lib,
# which is not staged into dependent recipe sysroots by default. Add it so
# consumers (e.g. xr-mfv-hal) can link against it.
SYSROOT_DIRS:append = " /vendor/lib"

#
# IMPORTANT!!!
#
# You should enable components which will be included in xr_dsp_algorithms library in  bbappend file.
#   EXTRA_OECONF_append_<target> = " --enable-vadeos --enable-vrexgain --enable-compress3_4 --enable-srconv --enable-gain --enable-playback-processing"
# By default, only common components are included.
# Regardless of what is enabled, all header files will be installed
#
# Example:
#   meta-rdk-oem-arris-broadcom/meta-arrisxi6/recipes-extended/xraudio/xr-dsp-algorithms_1.0.bbappend
#   EXTRA_OECONF_append_arrisxi6wv = " --enable-vadeos --enable-vrexgain --enable-compress3_4"
#

do_install:append() {
   install -d ${D}${includedir}
   install -d ${D}${includedir}/xr_dsp
   install -d ${D}${includedir}/xr_dsp/include
   install -m 644 ${S}/src/include/srconv.h        ${D}${includedir}/xr_dsp/xr_dsp_sample_rate_converter.h
   install -m 644 ${S}/src/include/dspsw_typedef.h ${D}${includedir}/xr_dsp/include/
   install -m 644 ${S}/src/include/dspswmem.h      ${D}${includedir}/xr_dsp/include/
   install -m 644 ${S}/src/include/dspsw_macros.h  ${D}${includedir}/xr_dsp/include/
   install -m 644 ${S}/src/include/dspsw_fft.h     ${D}${includedir}/xr_dsp/include/
   install -m 644 ${S}/src/private/include/arm/platform_specific.h   ${D}${includedir}/xr_dsp/include/
   install -m 644 ${S}/src/vrexgain/source/vrexgain.h                ${D}${includedir}/xr_dsp/include/
   install -m 644 ${S}/src/include/vad_eos_hd.h    ${D}${includedir}/xr_dsp/include/
   install -m 644 ${S}/src/include/beamformer.h    ${D}${includedir}/xr_dsp/include/
   install -m 644 ${S}/src/include/aecdspsw.h      ${D}${includedir}/xr_dsp/include/
   install -m 644 ${S}/src/include/multithread.h   ${D}${includedir}/xr_dsp/include/
   install -m 644 ${S}/src/include/dspsw_log.h     ${D}${includedir}/xr_dsp/include/
   install -m 644 ${S}/src/include/efifo.h         ${D}${includedir}/xr_dsp/include/
   install -m 644 ${S}/src/include/dspsw_version.h ${D}${includedir}/xr_dsp/include/
   install -m 644 ${S}/src/MFCC/source/MFCC.h      ${D}${includedir}/xr_dsp/include/

   install -d ${D}/etc
   install -d ${D}/etc/vendor
   install -d ${D}/etc/vendor/input
   # The per-component config JSONs are only generated when the matching
   # component is enabled (ENABLE_DGA/ENABLE_EOS/ENABLE_PPR via EXTRA_OECONF).
   # Only deploy the ones the build actually produced.
   for cfg in xraudio_dga_config.json xraudio_eos_config.json xraudio_ppr_config.json; do
       if [ -f ${B}/src/${cfg} ]; then
           install -m 0644 ${B}/src/${cfg} ${D}/etc/vendor/input/${cfg}
       fi
   done
}

FILES:${PN}:append = " /etc/vendor/input/*.json"

EXTRA_OECONF:append = " VSDK_UTILS_JSON_TO_HEADER=${RECIPE_SYSROOT}/usr/include/rdk/halif/vsdk/ffv/vsdk_json_to_header.py VSDK_UTILS_JSON_COMBINE=${RECIPE_SYSROOT}/usr/include/rdk/halif/vsdk/ffv/vsdk_json_combine.py"

# create DGA default configuration header
XRAUDIO_DGA_CONFIG_OEM_ADD    = "${S}/../xraudio_dga_config_oem.add.json"
XRAUDIO_DGA_CONFIG_OEM_SUB    = "${S}/../xraudio_dga_config_oem.sub.json"
EXTRA_OECONF:append = " XRAUDIO_DGA_CONFIG_JSON_SUB=${XRAUDIO_DGA_CONFIG_OEM_SUB} XRAUDIO_DGA_CONFIG_JSON_ADD=${XRAUDIO_DGA_CONFIG_OEM_ADD}"

# create EOS default configuration header
XRAUDIO_EOS_CONFIG_OEM_ADD = "${S}/../xraudio_eos_config_oem.add.json"
XRAUDIO_EOS_CONFIG_OEM_SUB = "${S}/../xraudio_eos_config_oem.sub.json"
EXTRA_OECONF:append = " XRAUDIO_EOS_CONFIG_JSON_SUB=${XRAUDIO_EOS_CONFIG_OEM_SUB} XRAUDIO_EOS_CONFIG_JSON_ADD=${XRAUDIO_EOS_CONFIG_OEM_ADD}"

# create PPR default configuration header
XRAUDIO_PPR_CONFIG_OEM_ADD = "${S}/../xraudio_ppr_config_oem.add.json"
XRAUDIO_PPR_CONFIG_OEM_SUB = "${S}/../xraudio_ppr_config_oem.sub.json"
EXTRA_OECONF:append = " XRAUDIO_PPR_CONFIG_JSON_SUB=${XRAUDIO_PPR_CONFIG_OEM_SUB} XRAUDIO_PPR_CONFIG_JSON_ADD=${XRAUDIO_PPR_CONFIG_OEM_ADD}"

addtask clean_oem_config after do_unpack before do_configure

do_clean_oem_config() { 
    rm -f ${XRAUDIO_DGA_CONFIG_OEM_ADD} ${XRAUDIO_DGA_CONFIG_OEM_SUB}
    rm -f ${XRAUDIO_EOS_CONFIG_OEM_ADD} ${XRAUDIO_EOS_CONFIG_OEM_SUB}
    rm -f ${XRAUDIO_PPR_CONFIG_OEM_ADD} ${XRAUDIO_PPR_CONFIG_OEM_SUB}
}

