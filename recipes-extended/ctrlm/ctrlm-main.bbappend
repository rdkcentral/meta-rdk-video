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
CXXFLAGS:append = " -DDISABLE_DEVICESETTINGS=1"
EXTRA_OECMAKE:append = " -DDISABLE_DEVICESETTINGS=1"
SRC_URI:append = " file://0001-DS-COMRPC-disable-libds-calls-in-ctrlm-main.patch"
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

