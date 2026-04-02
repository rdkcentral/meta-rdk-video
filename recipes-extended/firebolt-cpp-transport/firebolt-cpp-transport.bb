SUMMARY = "C++ Firebolt: Transport layer"
DESCRIPTION = "Recipe for building Transport layer for C++ Firebolt Clients"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

inherit cmake

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

PV = "1.1.7"
PR = "r0"

SRC_URI = "https://github.com/rdkcentral/firebolt-cpp-transport/releases/download/v${PV}/firebolt-cpp-transport-${PV}.tar.gz"
SRC_URI[sha256sum] = "7cc0d1d387c9d7ac42e72163aee3ffdf6f2e8985b8167f5e8b9cee29f8d3aaf4"

S = "${WORKDIR}/firebolt-cpp-transport-${PV}"

DEPENDS = "nlohmann-json websocketpp boost"
RDEPENDS:${PN} = "websocketpp boost-system"

PACKAGECONFIG ??= ""
PACKAGECONFIG[legacy-rpc-v1] = "-DENABLE_LEGACY_RPC_V1=ON,-DENABLE_LEGACY_RPC_V1=OFF"

EXTRA_OECMAKE:append = " ${PACKAGECONFIG_CONFARGS}"

PACKAGES = "${PN} ${PN}-dev ${PN}-dbg"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so"
FILES:${PN}-dev += "${libdir}/cmake/* ${includedir}/firebolt"
FILES:${PN}-dbg += "${libdir}/.debug"

INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"
