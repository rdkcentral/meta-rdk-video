SUMMARY = "This recipe provides the libpackage abstraction of DAC applicaitons"
SECTION = "rdk/libs"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "1.0.0"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

PROVIDES = "virtual/libpackage"
RPROVIDES:${PN} = "virtual/libpackage"

DEPENDS += "packager-headers"

SRCREV = "53fea63b4fb6e5491364781dcdfd91bcc47397f8"
SRC_URI = "${CMF_GITHUB_ROOT}/libpackage;${CMF_GITHUB_SRC_URI_SUFFIX};name=lisapack"
SRCREV_FORMAT = "lisapack"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

PACKAGECONFIG ?= " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'enable_ralf', 'ralfsupport', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'DAC_SUPPORT', 'lisa', '', d)} \
    "
PACKAGECONFIG[ralfsupport]    = "-DENABLE_RALF_SUPPORT=ON -DDAC_APP_PATH=${DAC_APP_PATH}, -DENABLE_RALF_SUPPORT=OFF, ralf-utils jsoncpp, ralf-utils jsoncpp"
PACKAGECONFIG[depcheck]    = "-DDISABLE_DEPENDENCY_CHECK=OFF, -DDISABLE_DEPENDENCY_CHECK=ON"
PACKAGECONFIG[lisa]    = ",,sqlite3 boost libarchive,sqlite3 boost libarchive"


EXTRA_OECMAKE:append = " -DRDK_PACKAGE_CERT_PATH=/etc/rdk/certs -DDAC_APP_PATH=${DAC_APP_PATH}"
