SUMMARY = "Host/Native tooling for the Web Platform for Embedded Framework"

LICENSE = "Apache-2.0"
HOMEPAGE = "https://github.com/rdkcentral/ThunderTools"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c3349dc67b2f8c28fc99b300eb39e3cc"

TOOLCHAIN = "gcc"
PR = "r5"
PV = "4.4.7"
S = "${WORKDIR}/git"

SRC_URI = "git://github.com/rdkcentral/ThunderTools.git;protocol=https;branch=R4_4-RDK"

# supports outputting and parsing of multi namespaces, should be included in 4.4.8
SRCREV = "a6976a075099ec5babb45c81329ace17becfad33"

inherit cmake pkgconfig python3native

EXTRA_OECMAKE += "-DCMAKE_SYSROOT=${STAGING_DIR_HOST}"

DEPENDS = "\
    python3-native \
    python3-jsonref-native \
"

FILES:${PN} += "${datadir}/*/Modules/*.cmake"

OECMAKE_SOURCEPATH = "${WORKDIR}/git"
BBCLASSEXTEND = "native nativesdk"
