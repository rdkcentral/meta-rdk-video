SUMMARY = "C++ Firebolt: Transport layer"
DESCRIPTION = "Recipe for building Transport layer for C++ Firebolt Clients"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b1e01b26bacfc2232046c90a330332b3"

inherit cmake

FIREBOLT_TRANSPORT_VERSION = "1.0.0-next.23"

SRC_URI = "https://github.com/rdkcentral/firebolt-native-transport/releases/download/v${FIREBOLT_TRANSPORT_VERSION}/firebolt-native-transport-${FIREBOLT_TRANSPORT_VERSION}.tar.gz"
SRC_URI[sha256sum] = "61bfb10f7f32c50ee926dae6f9c462e8a9148a043a907a46e2005eed61c7eadb"

S = "${WORKDIR}/firebolt-native-transport-${FIREBOLT_TRANSPORT_VERSION}"

DEPENDS = "nlohmann-json websocketpp boost"
RDEPENDS:${PN} = "websocketpp boost-system"

PACKAGES = "${PN} ${PN}-dev ${PN}-dbg"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so"
FILES:${PN}-dev += "${libdir}/cmake/* ${includedir}/firebolt"
FILES:${PN}-dbg += "${libdir}/.debug"

INSANE_SKIP:${PN} = "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"
