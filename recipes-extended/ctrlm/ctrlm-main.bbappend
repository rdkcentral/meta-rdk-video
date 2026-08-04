# DS_COMRPC migration: disable libds in ctrlm-main.
# All 5 ctrlm_dsmgr_*() wrappers become no-ops returning true.
# Audio ducking and FrontPanel LED via libds are silently skipped.
#
# NOTE: ctrlm-main.bb already removes devicesettings from DEPENDS/RDEPENDS.
# This bbappend is still needed because:
#  - ctrlm-main source does NOT yet have CTRLM_USE_THUNDER_FR_DS cmake guards
#  - THUNDER is not set to 'true' for xione-uk → CTRLM_USE_THUNDER_FR_DS=OFF
#  - factory/CMakeLists.txt and ctrlmf_audio_control.cpp still need source
#    patches (DISABLE_DEVICESETTINGS guards) to exclude -lds/-ldshalcli linkage
# Once ctrlm-main source has CTRLM_USE_THUNDER_FR_DS guards and THUNDER=true,
# this bbappend and patch can be removed.
#
# Rollback: delete this file.

# Define DISABLE_DEVICESETTINGS so guarded code is excluded from compilation
CXXFLAGS:append:xione-uk = " -DDISABLE_DEVICESETTINGS=1 "
# Pass as cmake variable too — factory/CMakeLists.txt uses if(NOT DISABLE_DEVICESETTINGS)
EXTRA_OECMAKE:append:xione-uk = " -DDISABLE_DEVICESETTINGS=1"

# Apply source patch — scoped to xione-uk: patch was generated from xione-uk
# source tree; sharp-a60 uses a different ctrlm-main source revision.
SRC_URI:append:xione-uk = " file://0001-DS-COMRPC-disable-libds-calls-in-ctrlm-main.patch"

# rdktv-us-armv8a: devicesettings (libds.so/libdshalcli.so) removed from image.
# ctrlm-main v1.1.18 unconditionally #includes dsMgr.h/host.hpp even when
# CTRLM_USE_THUNDER_FR_DS=ON; DISABLE_DEVICESETTINGS guards those includes.
CXXFLAGS:append:rdktv-us-armv8a = " -DDISABLE_DEVICESETTINGS=1"
EXTRA_OECMAKE:append:rdktv-us-armv8a = " -DDISABLE_DEVICESETTINGS=1"
SRC_URI:append:rdktv-us-armv8a = " file://0001-DS-COMRPC-disable-libds-calls-in-ctrlm-main.patch"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

