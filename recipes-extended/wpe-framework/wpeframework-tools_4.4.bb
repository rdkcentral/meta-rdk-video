SUMMARY = "Host/Native tooling for the Web Platform for Embedded Framework"

LICENSE = "Apache-2.0"
HOMEPAGE = "https://github.com/rdkcentral/ThunderTools"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c3349dc67b2f8c28fc99b300eb39e3cc"

TOOLCHAIN = "gcc"
PR = "r2"
PV = "4.4.3"
S = "${WORKDIR}/git"

SRC_URI = "git://github.com/rdkcentral/ThunderTools.git;protocol=https;branch=R4_4"

SRC_URI += "file://0003-R4.4-Change-MODULE-PATH.patch"
SRC_URI += "file://00010-R4.4-Add-support-for-project-dir.patch"
SRCREV = "b6789ca88b1e6c56942d9858d6d4411eae389ddc"

inherit cmake pkgconfig python3native

EXTRA_OECMAKE += "-DCMAKE_SYSROOT=${STAGING_DIR_HOST}"

DEPENDS = "\
    python3-native \
    python3-jsonref-native \
"

FILES:${PN} += "${datadir}/*/Modules/*.cmake"

OECMAKE_SOURCEPATH = "${WORKDIR}/git"
BBCLASSEXTEND = "native nativesdk"
