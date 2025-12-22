SUMMARY = "C++ Firebolt Client"
DESCRIPTION = "Recipe for building C++ Firebolt Client"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b1e01b26bacfc2232046c90a330332b3"

inherit cmake

FIREBOLT_CORE_VERSION = "1.8.0-next.32"

SRC_URI = "https://github.com/rdkcentral/firebolt-apis/releases/download/v${FIREBOLT_CORE_VERSION}/firebolt-apis--native-core-${FIREBOLT_CORE_VERSION}.tar.gz"
SRC_URI[sha256sum] = "df7cd17276e24d1659b1d8b531efaad2d64df6ee981d90d341fc530c83d1eefb"

S = "${WORKDIR}/firebolt-apis--native-core-${FIREBOLT_CORE_VERSION}"

DEPENDS = "firebolt-cpp-transport nlohmann-json"
RDEPENDS:${PN} = "firebolt-cpp-transport"

PACKAGES = "${PN} ${PN}-dev ${PN}-dbg"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so"
FILES:${PN}-dev += "${libdir}/cmake/* ${includedir}/firebolt"
FILES:${PN}-dbg += "${libdir}/.debug"

INSANE_SKIP:${PN} = "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"
