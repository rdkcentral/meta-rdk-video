SUMMARY = "Cobalt plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=a1045f140d2e71b4e089875cd5d07e42"

require larboard_revision.inc

TOOLCHAIN = "gcc"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

PV = "1.0.0"
PR = "r0"

SRC_URI = "${LARBOARD_SRC_URI};protocol=${CMF_GIT_PROTOCOL};branch=develop"

SRCREV = "${LARBOARD_SRCREV_DEV}"

S = "${WORKDIR}/git/plugin"

inherit cmake pkgconfig

DEPENDS = "wpeframework wpeframework-tools-native rdkservices-apis"
DEPENDS += "libloader-app"

RDEPENDS:${PN} = "libloader-app virtual/cobalt-evergreen"

COBALT_PERSISTENTPATHPOSTFIX ?= "Cobalt-0"
COBALT_CLIENTIDENTIFIER ?= "wst-Cobalt-0"

EXTRA_OECMAKE += " \
    -DCMAKE_BUILD_TYPE=Release \
    -DBUILD_SHARED_LIBS=ON \
    -DPLUGIN_COBALT_CLIENTIDENTIFIER="${COBALT_CLIENTIDENTIFIER}" \
    -DPLUGIN_COBALT_PERSISTENTPATHPOSTFIX="${COBALT_PERSISTENTPATHPOSTFIX}" \
"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so"
