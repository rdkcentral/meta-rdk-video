SUMMARY = "This recipe provides the libpackage abstraction of DAC applicaitons"
SECTION = "rdk/libs"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PROVIDES = "virtual/libpackage"
RPROVIDES:${PN} = "virtual/libpackage"

DEPENDS = "sqlite3 boost libarchive"
RDEPENDS_${PN} = " sqlite3 boost libarchive"

SRC_URI = "${CMF_GITHUB_ROOT}/libpackage;protocol=${CMF_GIT_PROTOCOL};branch=RDKECOREMW-302;name=lisapack"
SRC_URI += "file://0001-enable-debug.patch"
SRCREV_FORMAT = "lisapack"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

