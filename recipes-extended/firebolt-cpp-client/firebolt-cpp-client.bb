SUMMARY = "C++ Firebolt Client"
DESCRIPTION = "Recipe for building C++ Firebolt Client"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

inherit cmake

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

PV = "0.3.0"
PR = "r0"

SRC_URI = "https://github.com/rdkcentral/firebolt-cpp-client/releases/download/v${PV}/firebolt-cpp-client-${PV}.tar.gz"
SRC_URI[sha256sum] = "64eeeb6dd30ad6c12934946b3b55340675075c4a604df25de1d29ecfa07b8ddd"

S = "${WORKDIR}/firebolt-cpp-client-${PV}"

DEPENDS = "firebolt-cpp-transport nlohmann-json"
RDEPENDS:${PN} = "firebolt-cpp-transport"

PACKAGES = "${PN} ${PN}-dev ${PN}-dbg"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so"
FILES:${PN}-dev += "${libdir}/cmake/* ${includedir}/firebolt"
FILES:${PN}-dbg += "${libdir}/.debug"

INSANE_SKIP:${PN} = "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"
