# DS_COMRPC migration: disable libds calls in player-interface.
# PlayerExternalsRdkInterface.h/.cpp use device:: (libds C++ API) for HDCP
# status. With DISABLE_DEVICESETTINGS=1 the device:: calls are stubbed to return
# safe HDCP 2.x defaults, and -lds/-ldshalcli are not linked.
# CMAKE_IARM_MGR=1 is retained so PlayerExternalsRdkInterface/DeviceFirebolt
# files are still compiled — only the DS-specific code paths are guarded.
# Rollback: delete this file.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI:append = " file://0001-DS-COMRPC-disable-libds-calls-in-player-interface.patch"
SRC_URI:append = " file://0002-DS-COMRPC-exclude-DeviceIARMInterface-when-DS-disabled.patch"

# Disable device:: (libds) calls; patch stubs SetHDMIStatus() with HDCP 2.x defaults
CXXFLAGS:append = " -DDISABLE_DEVICESETTINGS=1 "
# Pass as cmake variable too — patches use if(NOT DISABLE_DEVICESETTINGS) in CMakeLists.txt
# Without this, the cmake variable is undefined → NOT undefined = TRUE → -lds still added
EXTRA_OECMAKE:append = " -DDISABLE_DEVICESETTINGS=ON"
# DS include paths are intentionally kept: DeviceIARMInterface.cpp uses dsMgr.h
# (from /rdk/ds-rpc/) for IARM event constants without calling device:: methods.
# The DS C++ includes (manager.hpp, host.hpp) in PlayerExternalsRdkInterface.h
# are guarded by #ifndef DISABLE_DEVICESETTINGS in the source patch.

RDEPENDS:${PN}:remove = "devicesettings"
