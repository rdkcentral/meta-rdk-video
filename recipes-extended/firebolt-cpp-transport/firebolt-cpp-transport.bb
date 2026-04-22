SUMMARY = "C++ Firebolt: Transport layer"
DESCRIPTION = "Recipe for building Transport layer for C++ Firebolt Clients"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

inherit cmake

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

PV = "2.0.0"
PR = "r0"
SRC_REV = "be5cb9edcee8b45f199805e44eec889a565e0f83"
# Temporarily Commenting out the SRC_URI as the release is not yet available. Will be uncommented once the release is available.
#SRC_URI = "https://github.com/rdkcentral/firebolt-cpp-transport/releases/download/v${PV}/firebolt-cpp-transport-${PV}.tar.gz"
#SRC_URI[sha256sum] = "0af04e3040cc87f92f05d0c35662792d95403b801e221aeb2e263af72a4c4966"
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
