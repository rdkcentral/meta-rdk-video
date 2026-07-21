# DS_COMRPC migration: disable libds (devicesettings) in AAMP.
# dsMgr daemon is disabled; PlayerExternalsRdkInterface is patched to use
# safe boot defaults for HDCP (authenticated/2.x) and resolution (1080p).
# Real HDCP state comes from Thunder entservices-devicesettings at runtime.
# Rollback: delete this file.

# Disable CMAKE_DS_EVENT_SUPPORTED so Manager::Initialize/Host::Register
# (USE_DS_EVENT_SUPPORTED guarded blocks) are excluded from compilation
EXTRA_OECMAKE:remove = "-DCMAKE_DS_EVENT_SUPPORTED=1"

# Define DISABLE_DEVICESETTINGS so SetHDMIStatus() uses hardcoded defaults
# and the libds headers are not included
CXXFLAGS:append = " -DDISABLE_DEVICESETTINGS=1 "

# Remove ds include path so the build does not rely on libds headers
CXXFLAGS:remove = "-I${STAGING_DIR_TARGET}${includedir}/rdk/ds"

# Remove libds from build-time and runtime deps
DEPENDS:remove = "devicesettings"
RDEPENDS:${PN}:remove = "devicesettings"

# Apply source patch
SRC_URI:append = " file://0001-DS-COMRPC-disable-libds-calls-in-AAMP-PlayerExternalsRdk.patch"
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
