SUMMARY = "C++ Firebolt: Transport layer"
DESCRIPTION = "Recipe for building Transport layer for C++ Firebolt Clients"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=724ed260f33bc706a8fbafbbb35a316b"

inherit cmake

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

PV = "1.1.11"
PR = "r0"

SRC_URI = "https://github.com/rdkcentral/firebolt-cpp-transport/releases/download/v${PV}/firebolt-cpp-transport-${PV}.tar.gz"
SRC_URI[sha256sum] = "fcd0f77f6cc7911baee93a91f262e2ee1f7217f3d3eab0ba5618dc2821ce7da4"
S = "${WORKDIR}/firebolt-cpp-transport-${PV}"

DEPENDS = "nlohmann-json websocketpp boost"
RDEPENDS:${PN} = "boost-system"

PACKAGECONFIG ??= ""
PACKAGECONFIG[legacy-rpc-v1] = "-DENABLE_LEGACY_RPC_V1=ON,-DENABLE_LEGACY_RPC_V1=OFF"
PACKAGECONFIG[disable-so-version] = "-DDISABLE_SO_VERSION=ON,-DDISABLE_SO_VERSION=OFF"

EXTRA_OECMAKE:append = " ${PACKAGECONFIG_CONFARGS}"

PACKAGES = "${PN} ${PN}-dev ${PN}-dbg"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so"
FILES:${PN}-dev += "${libdir}/cmake/* ${includedir}/firebolt"
FILES:${PN}-dbg += "${libdir}/.debug"

INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"
