PACKAGECONFIG:append = " legacy-rpc-v1"

# Local dev override for the transport work-in-progress.
# This pins the recipe to the exact transport commit we are testing and ensures
# the version metadata matches the intended 1.1.13 build while leaving the base
# recipe unchanged for upstream compatibility.
SRCREV = "bbfd8a88f0275d1cdbceef8a9e18ee4479216a29"
SRC_URI = "git://github.com/rdkcentral/firebolt-cpp-transport;protocol=https;branch=RDKEMW-19461-develop"
S = "${WORKDIR}/git"

EXTRA_OECMAKE:append = " -DFIREBOLT_TRANSPORT_VERSION=${PV} -DFIREBOLT_TRANSPORT_DEFAULT_LOG_LEVEL=debug -DFIREBOLT_TRANSPORT_DEFAULT_LOG_OUTPUT=stdout"