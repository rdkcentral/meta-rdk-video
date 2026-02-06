SUMMARY = "Host/Native tooling for the Web Platform for Embedded Framework"

LICENSE = "Apache-2.0"
HOMEPAGE = "https://github.com/rdkcentral/ThunderTools"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c3349dc67b2f8c28fc99b300eb39e3cc"

TOOLCHAIN = "gcc"
PR = "r6"
PV = "5.3.0"
S = "${WORKDIR}/git"

SRC_URI = "git://github.com/rdkcentral/ThunderTools.git;protocol=https;branch=R5_3"

SRC_URI += "file://0001-Change-MODULE-PATH.patch \
            file://0002-Change-namespace-Proxystub-Json-Generator.patch \
            file://0003-Callsign-not-generated-Json-Generator.patch \
            file://0004-Add-support-for-project-dir.patch \
            file://0005-jsongenerator_fallback_length_validation_fix.patch \
            file://0006-Autostart-startmode-deactivated.patch \
            "

SRCREV = "R5.3.0"

inherit cmake pkgconfig python3native

EXTRA_OECMAKE += "-DCMAKE_SYSROOT=${STAGING_DIR_HOST}"

DEPENDS = "\
    python3-native \
    python3-jsonref-native \
"

FILES:${PN} += "${datadir}/*/Modules/*.cmake"

OECMAKE_SOURCEPATH = "${WORKDIR}/git"
BBCLASSEXTEND = "native nativesdk"
