# DS_COMRPC migration: disable libds calls in player-interface.
#
# The new COM-RPC refactored source (SRCREV 819802890...) already has upstream
# support for the Thunder DS path via CMAKE_DS_THUNDER_PLUGIN:
#   - externals/CMakeLists.txt: if(CMAKE_DS_THUNDER_PLUGIN) → no -lds/-ldshalcli
#   - PlayerExternalsRdkInterface.h: #ifndef USE_DS_THUNDER_PLUGIN → DS C++ headers excluded
#   - PlayerExternalsRdkInterface.cpp: SetHDMIStatus() #ifdef USE_DS_THUNDER_PLUGIN → Thunder path
#   - PlayerThunderAccess.cpp provides the Thunder COM-RPC DS access layer
#
# No source patches needed. Just set CMAKE_DS_THUNDER_PLUGIN=ON.
# Rollback: delete this file.

# Enable Thunder DS path — removes -lds/-ldshalcli, activates USE_DS_THUNDER_PLUGIN guards
EXTRA_OECMAKE:append = " -DCMAKE_DS_THUNDER_PLUGIN=ON"

RDEPENDS:${PN}:remove = "devicesettings"
