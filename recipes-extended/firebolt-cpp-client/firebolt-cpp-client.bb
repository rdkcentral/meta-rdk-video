SUMMARY = "C++ Firebolt Client"
DESCRIPTION = "Recipe for building C++ Firebolt Client"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

inherit cmake

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

PV = "0.5.5"
PR = "r0"

SRC_URI = "https://github.com/rdkcentral/firebolt-cpp-client/releases/download/v${PV}/firebolt-cpp-client-${PV}.tar.gz"
SRC_URI[sha256sum] = "9ecb43d70cfe9f5737d28c2cae150e8b3258a70a54cb77982fa7da508aecfbbd"

S = "${WORKDIR}/firebolt-cpp-client-${PV}"

DEPENDS = "firebolt-cpp-transport nlohmann-json"
RDEPENDS:${PN} = "firebolt-cpp-transport"

PACKAGECONFIG ??= ""
PACKAGECONFIG[disable-so-version] = "-DDISABLE_SO_VERSION=ON,-DDISABLE_SO_VERSION=OFF"

EXTRA_OECMAKE:append = " ${PACKAGECONFIG_CONFARGS}"

PACKAGES = "${PN} ${PN}-dev ${PN}-dbg"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so"
FILES:${PN}-dev += "${libdir}/cmake/* ${includedir}/firebolt"
FILES:${PN}-dbg += "${libdir}/.debug"

INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"
