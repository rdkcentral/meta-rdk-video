PACKAGECONFIG:append = " legacy-rpc-v1"

# Local dev override for the transport work-in-progress.
# This pins the recipe to the exact transport commit we are testing and ensures
# the version metadata matches the intended 1.1.13 build while leaving the base
# recipe unchanged for upstream compatibility.
SRCREV = "184100e0c2cf98c8b2215e4cc604dfa0bc06abc0"
SRC_URI = "git://github.com/rdkcentral/firebolt-cpp-transport;protocol=https;branch=RDKEMW-19461-develop"

EXTRA_OECMAKE:append = " -DFIREBOLT_TRANSPORT_VERSION=${PV} -DFIREBOLT_TRANSPORT_DEFAULT_LOG_LEVEL=debug -DFIREBOLT_TRANSPORT_DEFAULT_LOG_OUTPUT=stdout"