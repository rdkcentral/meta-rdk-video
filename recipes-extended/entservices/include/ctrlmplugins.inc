# Common settings for the ctrlm-backed ENTServices plugins (RemoteControl, VoiceControl)

require thunderplugin.inc

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7e2eceb64cc374eafafd7e1a4e763f63"

inherit features_check
REQUIRED_DISTRO_FEATURES = "ctrlm"

DEPENDS += "entservices-apis iarmbus iarmmgrs ctrlm-headers"
RDEPENDS:${PN} += "iarmbus ctrlm"

CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/wdmp-c/ "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/trower-base64/ "
CXXFLAGS += " -DRFC_ENABLED "
# enable filtering for undefined interfaces and link local ip address notifications
CXXFLAGS += " -DNET_DEFINED_INTERFACES_ONLY -DNET_NO_LINK_LOCAL_ANNOUNCE "

PACKAGECONFIG ?= "breakpadsupport"

# CMAKE_PROJECT_VERSION sets the plugin's API version and startup log version
EXTRA_OECMAKE += " -DCMAKE_PROJECT_VERSION=${PV}"

# Check if DRI_DEVICE_NAME is defined. If yes- use that as DEFAULT_DEVICE. If not, use DEFAULT_DEVICE configured from rdkservices.
python () {
    dri_device_name = d.getVar('DRI_DEVICE_NAME')
    if dri_device_name:
        d.appendVar('OECMAKE_CXX_FLAGS', ' -DDEFAULT_DEVICE=\'\\"{}\\"\' '.format(dri_device_name))
}
