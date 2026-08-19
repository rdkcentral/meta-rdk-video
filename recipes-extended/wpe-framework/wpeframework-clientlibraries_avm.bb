# This software is the confidential and proprietary information of
#
# RDK Management, LLC (RDKM) ("Confidential Information").
#
# Access to RDKM’s source code is conditional on the acceptance of,
# and continued compliance with, the terms of the Software License Agreement
# and the NondisclosureAgreement between you (the “Licensee”) and RDKM.
#
# You shall not disclose this source code or such Confidential Information.

SUMMARY = "Plugin component for AVMonitor - playback observability tool"
HOMEPAGE = "https://github.com/FireboltConnectApps/ThunderClientLibraryAVMonitor"
SECTION = "rdk"
LICENSE = "CLOSED"

inherit cmake pkgconfig

TOOLCHAIN = "gcc"

DEPENDS = "wpeframework entservices-apis wpeframework-tools-native"
RDEPENDS:${PN}:append += " wpeframework"

SRC_URI = "file://r4.4/ThunderClientLibraryAVMonitor-main.zip"
S = "${WORKDIR}/ThunderClientLibraryAVMonitor-main"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

PACKAGECONFIG[debug] = "-DCMAKE_BUILD_TYPE=Debug,-DCMAKE_BUILD_TYPE=Release,"

EXTRA_OECMAKE = " \
    -DBUILD_SHARED_LIBS=ON \
    -DCMAKE_SYSROOT=${STAGING_DIR_HOST} \
    -DBUILD_REFERENCE=${SRCREV} \
"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so"
FILES:${PN} += "${datadir}/WPEFramework/*"
FILES:${PN} += "${PKG_CONFIG_DIR}/*.pc"
ASNEEDED = ""

INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"