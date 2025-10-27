SUMMARY = "xr-voice-sdk provides a shared library that controls how and where speech gets distributed."
DESCRIPTION = "TBD."
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV ?= "1.0.1"
PR ?= "r0"

PACKAGE_ARCH  = "${MIDDLEWARE_ARCH}"
SRCREV_FORMAT = "xr-voice-sdk"

SRC_URI = "${CMF_GITHUB_ROOT}/xr-voice-sdk;${CMF_GITHUB_SRC_URI_SUFFIX};name=xr-voice-sdk"

S = "${WORKDIR}/git"

DEPENDS = "libbsd util-linux safec-common-wrapper gperf-native jansson rdkversion openssl"

INHERIT_BREAKPAD_WRAPPER := "${@bb.utils.contains('BBLAYERS', '${RDKROOT}/meta-rdk', 'breakpad-wrapper', '',d)}"

inherit cmake pkgconfig coverity ${INHERIT_BREAKPAD_WRAPPER}

DEPENDS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec', '', d)}"
CFLAGS:append  = "${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --cflags libsafec`', ' -DSAFEC_DUMMY_API', d)}"
LDFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --libs libsafec`', '', d)}"

# Set to "1" in recipe append to enable/disable HTTP or WS support
ENABLE_HTTP_SUPPORT    ?= "0"
ENABLE_WS_SUPPORT      ?= "1"
ENABLE_SDT_SUPPORT     ?= "0"

XRSR_ALLOW_INPUT_FAILURE  ?= "0"

XRSR_KEYWORD_PHRASE       ?= ""

XRAUDIO_KWD_COMPONENT     ?= ""
XRAUDIO_EOS_COMPONENT     ?= ""
XRAUDIO_DGA_COMPONENT     ?= ""
XRAUDIO_SDF_COMPONENT     ?= ""
XRAUDIO_OVC_COMPONENT     ?= ""
XRAUDIO_PPR_COMPONENT     ?= ""
XRAUDIO_FFV_HAL_COMPONENT ?= ""
XRAUDIO_DECODE_ADPCM      ?= "1"
XRAUDIO_DECODE_OPUS       ?= "1"
XRAUDIO_RESOURCE_MGMT     ?= "0"

VSDK_CURTAIL_ENABLED      ?= "0"

DEPENDS:append = " ${XRAUDIO_KWD_COMPONENT} ${XRAUDIO_EOS_COMPONENT} ${XRAUDIO_DGA_COMPONENT} ${XRAUDIO_SDF_COMPONENT} ${XRAUDIO_OVC_COMPONENT} ${XRAUDIO_PPR_COMPONENT} ${XRAUDIO_FFV_HAL_COMPONENT}"

DEPENDS:append = "${@ ' curl'    if (d.getVar('ENABLE_HTTP_SUPPORT', expand=False) == "1") else ''}"
DEPENDS:append = "${@ ' nopoll'  if (d.getVar('ENABLE_WS_SUPPORT',   expand=False) == "1") else ''}"
DEPENDS:append = "${@ ' libopus' if (d.getVar('XRAUDIO_DECODE_OPUS', expand=False) == '1') else ''}"

DEPENDS:append = "${@ ' curtail' if (d.getVar('VSDK_CURTAIL_ENABLED',   expand=False) == '1') else ''}"

XRAUDIO_CONFIG_HAL     = "${PKG_CONFIG_SYSROOT_DIR}/usr/include/xraudio_hal_config.json"
XRAUDIO_CONFIG_KWD     = "${PKG_CONFIG_SYSROOT_DIR}/usr/include/xraudio_kwd_config.json"
XRAUDIO_CONFIG_EOS     = "${PKG_CONFIG_SYSROOT_DIR}/usr/include/xraudio_eos_config.json"
XRAUDIO_CONFIG_DGA     = "${PKG_CONFIG_SYSROOT_DIR}/usr/include/xraudio_dga_config.json"
XRAUDIO_CONFIG_SDF     = "${PKG_CONFIG_SYSROOT_DIR}/usr/include/xraudio_sdf_config.json"
XRAUDIO_CONFIG_OVC     = "${PKG_CONFIG_SYSROOT_DIR}/usr/include/xraudio_ovc_config.json"
XRAUDIO_CONFIG_PPR     = "${PKG_CONFIG_SYSROOT_DIR}/usr/include/xraudio_ppr_config.json"
XRAUDIO_CONFIG_OEM_ADD = "${S}/../xraudio_config_oem.add.json"
XRAUDIO_CONFIG_OEM_SUB = "${S}/../xraudio_config_oem.sub.json"

addtask clean_oem_config after do_unpack before do_configure

do_clean_oem_config() { 
    rm -f ${XRAUDIO_CONFIG_OEM_ADD} ${XRAUDIO_CONFIG_OEM_SUB}
}

# Configuration Options

EXTRA_OECMAKE:append = " -DCMAKE_SYSROOT=${RECIPE_SYSROOT} -DCMAKE_PROJECT_VERSION=${PV}"
EXTRA_OECMAKE:append = " -DSTAGING_BINDIR_NATIVE=${STAGING_BINDIR_NATIVE}"

EXTRA_OECMAKE:append = "${@ ' -DHTTP_ENABLED=ON'    if (d.getVar('ENABLE_HTTP_SUPPORT', expand=False) == "1")  else ''}"
EXTRA_OECMAKE:append = "${@ ' -DWS_ENABLED=ON -DWS_NOPOLL_PATCHES=ON' if (d.getVar('ENABLE_WS_SUPPORT',   expand=False) == "1")  else ''}"
EXTRA_OECMAKE:append = "${@ ' -DSDT_ENABLED=ON'     if (d.getVar('ENABLE_SDT_SUPPORT',  expand=False) == "1")  else ''}"
EXTRA_OECMAKE:append = "${@bb.utils.contains('DISTRO_FEATURES', 'ctrlm_mic_tap', ' -DMICROPHONE_TAP_ENABLED=ON', '', d)}"
EXTRA_OECMAKE:append = "${@ ' -DXRSR_ALLOW_INPUT_FAILURE=ON'     if (d.getVar('XRSR_ALLOW_INPUT_FAILURE',  expand=False) == "1")  else ''}"

EXTRA_OECMAKE:append = "${@' -DXRSR_KEYWORD_PHRASE=${XRSR_KEYWORD_PHRASE}' if (d.getVar('XRSR_KEYWORD_PHRASE', expand=False) != '') else ''}"

EXTRA_OECMAKE:append = "${@' -DXRAUDIO_RESOURCE_MGMT=ON'   if (d.getVar('XRAUDIO_RESOURCE_MGMT', expand=False) == '1') else ''}"
EXTRA_OECMAKE:append = "${@' -DXRAUDIO_DECODE_ADPCM=ON'    if (d.getVar('XRAUDIO_DECODE_ADPCM',  expand=False) == '1') else ''}"
EXTRA_OECMAKE:append = "${@' -DXRAUDIO_DECODE_OPUS=ON'     if (d.getVar('XRAUDIO_DECODE_OPUS',   expand=False) == '1') else ''}"

EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_HAL=${XRAUDIO_CONFIG_HAL}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_KWD=${XRAUDIO_CONFIG_KWD}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_EOS=${XRAUDIO_CONFIG_EOS}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_DGA=${XRAUDIO_CONFIG_DGA}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_SDF=${XRAUDIO_CONFIG_SDF}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_OVC=${XRAUDIO_CONFIG_OVC}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_PPR=${XRAUDIO_CONFIG_PPR}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_SUB=${XRAUDIO_CONFIG_OEM_SUB}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_ADD=${XRAUDIO_CONFIG_OEM_ADD}"

EXTRA_OECMAKE:append = "${@' -DXRAUDIO_KWD_ENABLED=ON' if d.getVar('XRAUDIO_KWD_COMPONENT', True) else ''}"
EXTRA_OECMAKE:append = "${@' -DXRAUDIO_EOS_ENABLED=ON' if d.getVar('XRAUDIO_EOS_COMPONENT', True) else ''}"
EXTRA_OECMAKE:append = "${@' -DXRAUDIO_DGA_ENABLED=ON' if d.getVar('XRAUDIO_DGA_COMPONENT', True) else ''}"
EXTRA_OECMAKE:append = "${@' -DXRAUDIO_SDF_ENABLED=ON' if d.getVar('XRAUDIO_SDF_COMPONENT', True) else ''}"
EXTRA_OECMAKE:append = "${@' -DXRAUDIO_OVC_ENABLED=ON' if d.getVar('XRAUDIO_OVC_COMPONENT', True) else ''}"
EXTRA_OECMAKE:append = "${@' -DXRAUDIO_PPR_ENABLED=ON' if d.getVar('XRAUDIO_PPR_COMPONENT', True) else ''}"
