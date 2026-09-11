SUMMARY = "Host/Native tooling for the Web Platform for Embedded Framework"

LICENSE = "Apache-2.0"
HOMEPAGE = "https://github.com/rdkcentral/ThunderTools"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c3349dc67b2f8c28fc99b300eb39e3cc"

TOOLCHAIN = "gcc"
PR = "r5"
PV = "4.4.6"
S = "${WORKDIR}/git"
SRC_URI = "git://github.com/rdkcentral/ThunderTools.git;protocol=https;branch=support/R4_4_6-RDK"

SRCREV = "29f4202f3c4166696f92970348aaac8068bd0bc2"

inherit cmake pkgconfig python3native

EXTRA_OECMAKE += "-DCMAKE_SYSROOT=${STAGING_DIR_HOST}"

DEPENDS = "\
    python3-native \
    python3-jsonref-native \
"

FILES:${PN} += "${datadir}/*/Modules/*.cmake"

OECMAKE_SOURCEPATH = "${WORKDIR}/git"
BBCLASSEXTEND = "native nativesdk"
