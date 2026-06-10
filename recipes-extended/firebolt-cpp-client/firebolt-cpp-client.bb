SUMMARY = "C++ Firebolt Client"
DESCRIPTION = "Recipe for building C++ Firebolt Client"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

inherit cmake

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

PV = "0.6.1"
PR = "r0"

SRCREV = "a7770e8b02e0254dd1670148473266df1b6368bf"

SRC_URI = "https://github.com/rdkcentral/firebolt-cpp-client/archive/${SRCREV}.tar.gz;downloadfilename=firebolt-cpp-client-${SRCREV}.tar.gz"
SRC_URI[sha256sum] = "efa3a76e16f8c3239fe6db59c8b889ec7620c4cc95ea4f8bb67c256aa9be3b2e"

S = "${WORKDIR}/firebolt-cpp-client-${SRCREV}"

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
