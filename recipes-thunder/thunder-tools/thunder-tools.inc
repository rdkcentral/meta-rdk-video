SUMMARY = "Host/Native tooling for the Thunder Framework"
DESCRIPTION = "Host/Native tooling (i.e. generators) required to build the Thunder Framework"
HOMEPAGE = "https://github.com/rdkcentral/ThunderTools"
SECTION = "thunder"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c3349dc67b2f8c28fc99b300eb39e3cc"

DEPENDS = "\
    python3-native \
    python3-jsonref-native \
"

SRC_URI = "git://github.com/rdkcentral/ThunderTools;protocol=https;nobranch=1"

SRCREV ?= "R${PV}"

S = "${WORKDIR}/git"

inherit cmake pkgconfig python3native

inherit native

PACKAGECONFIG[proxystub_security] = "\
    -DPROXYSTUB_GENERATOR_ENABLE_SECURITY=ON \
    ,-DPROXYSTUB_GENERATOR_ENABLE_SECURITY=OFF, \
"
PACKAGECONFIG[proxystub_coherency] = "\
    -DPROXYSTUB_GENERATOR_ENABLE_COHERENCY=ON \
    ,-DPROXYSTUB_GENERATOR_ENABLE_COHERENCY=OFF, \
"

EXTRA_OECMAKE += "-DCMAKE_SYSROOT=${STAGING_DIR_HOST}"

FILES:${PN} += "${datadir}/*/Modules/*.cmake"
