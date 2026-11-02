SRC_URI = "git://github.com/rdkcentral/firebolt-cpp-transport.git;protocol=https;branch=RDKEMW-19461-develop"
SRCREV = "c4a8bff343f972dcd8bb8c690b39c30f807e890f"
S = "${WORKDIR}/git"
PV = "1.1.13+git${SRCPV}"

PACKAGECONFIG:append = " legacy-rpc-v1"
