# DS_COMRPC migration: disable libds in ctrlm-main.
# All 5 ctrlm_dsmgr_*() wrappers become no-ops returning true.
# Audio ducking and FrontPanel LED via libds are silently skipped.
# Rollback: delete this file.

# Remove build-time and runtime deps on devicesettings
DEPENDS:remove = "devicesettings"
RDEPENDS:${PN}:remove = "devicesettings"

# Define DISABLE_DEVICESETTINGS so guarded code is excluded from compilation
CXXFLAGS:append = " -DDISABLE_DEVICESETTINGS=1 "
# Pass as cmake variable too — factory/CMakeLists.txt uses if(NOT DISABLE_DEVICESETTINGS)
EXTRA_OECMAKE:append = " -DDISABLE_DEVICESETTINGS=1"

# Apply source patch
SRC_URI:append = " file://0001-DS-COMRPC-disable-libds-calls-in-ctrlm-main.patch"
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

