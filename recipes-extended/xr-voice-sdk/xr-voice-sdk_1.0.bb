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

XRAUDIO_RESOURCE_MGMT     ?= "0"
XRAUDIO_USE_CURTAIL       ?= "0"

XLOG_USE_CURTAIL          ?= "0"
VSDK_DECODE_OPUS          ?= "1"

DEPENDS:append = "${@ ' curl'    if (d.getVar('ENABLE_HTTP_SUPPORT', expand=False) == "1") else ''}"
DEPENDS:append = "${@ ' nopoll'  if (d.getVar('ENABLE_WS_SUPPORT',   expand=False) == "1") else ''}"
DEPENDS:append = "${@ ' libopus' if (d.getVar('VSDK_DECODE_OPUS',    expand=False) == '1') else ''}"

DEPENDS:append = "${@ ' curtail' if (d.getVar('XLOG_USE_CURTAIL',    expand=False) == '1') else ''}"

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

EXTRA_OECMAKE:append = "${@' -DXRAUDIO_RESOURCE_MGMT=ON'   if (d.getVar('XRAUDIO_RESOURCE_MGMT', expand=False) == '1') else ''}"
EXTRA_OECMAKE:append = "${@' -DXRAUDIO_CURTAIL_ENABLED=ON' if (d.getVar('XRAUDIO_USE_CURTAIL',   expand=False) == '1') else ''}"

EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_HAL=${XRAUDIO_CONFIG_HAL}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_KWD=${XRAUDIO_CONFIG_KWD}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_EOS=${XRAUDIO_CONFIG_EOS}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_DGA=${XRAUDIO_CONFIG_DGA}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_SDF=${XRAUDIO_CONFIG_SDF}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_OVC=${XRAUDIO_CONFIG_OVC}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_PPR=${XRAUDIO_CONFIG_PPR}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_SUB=${XRAUDIO_CONFIG_OEM_SUB}"
EXTRA_OECMAKE:append = " -DXRAUDIO_CONFIG_JSON_ADD=${XRAUDIO_CONFIG_OEM_ADD}"

EXTRA_OECMAKE:append = "${@' -DXLOG_CURTAIL_ENABLED=ON' if (d.getVar('XLOG_USE_CURTAIL', expand=False) == '1') else ''}"
