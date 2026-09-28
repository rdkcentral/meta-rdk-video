SUMMARY = "ENTServices PersistentStore plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=943bbbdffda09ea061f1ee2d8cf5f7c09c20ae6d"

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
