SUMMARY = "ENTServices PersistentStore plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-persistentstore;${CMF_GITHUB_SRC_URI_SUFFIX}"

PV = "1.0+git${SRCPV}"
SRCREV = "1.0.7"

S = "${WORKDIR}/git/plugin"

inherit cmake pkgconfig

DEPENDS += "wpeframework wpeframework-tools-native entservices-apis sqlite3 iarmbus iarmmgrs"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

EXTRA_OECMAKE += "-DBUILD_REFERENCE=${SRCREV}"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${datadir}/WPEFramework/*"

INSANE_SKIP:${PN} += "dev-so"
