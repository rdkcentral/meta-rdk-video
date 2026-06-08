SUMMARY = "Host/Native tooling for the Web Platform for Embedded Framework"

LICENSE = "Apache-2.0"
HOMEPAGE = "https://github.com/rdkcentral/ThunderTools"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c3349dc67b2f8c28fc99b300eb39e3cc"

TOOLCHAIN = "gcc"
PR = "r3"
PV = "4.4.6"
S = "${WORKDIR}/git"

SRC_URI = "git://github.com/rdkcentral/ThunderTools.git;protocol=https;branch=development/R4.4.6_porting"

SRCREV = "151c9b79984adc79d6eb68a37f658d464071a682"

inherit cmake pkgconfig python3native

EXTRA_OECMAKE += "-DCMAKE_SYSROOT=${STAGING_DIR_HOST}"

DEPENDS = "\
    python3-native \
    python3-jsonref-native \
"

FILES:${PN} += "${datadir}/*/Modules/*.cmake"

OECMAKE_SOURCEPATH = "${WORKDIR}/git"
BBCLASSEXTEND = "native nativesdk"
