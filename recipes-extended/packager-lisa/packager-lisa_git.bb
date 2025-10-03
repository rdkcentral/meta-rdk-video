SUMMARY = "This recipe provides the libpackage abstraction of DAC applicaitons"
SECTION = "rdk/libs"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PROVIDES = "virtual/libpackage"
RPROVIDES:${PN} = "virtual/libpackage"

DEPENDS = "sqlite3 boost libarchive"
DEPENDS += "package-headers"
RDEPENDS_${PN} = " sqlite3 boost libarchive"

SRC_URI = "${CMF_GITHUB_ROOT}/libpackage;${CMF_GITHUB_SRC_URI_SUFFIX};name=lisapack"
SRCREV_FORMAT = "lisapack"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

