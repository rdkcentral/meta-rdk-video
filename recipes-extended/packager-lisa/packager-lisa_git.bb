SUMMARY = "This recipe provides the libpackage abstraction of DAC applicaitons"
SECTION = "rdk/libs"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

PV ?= "0.1.0"
PR ?= "r0"

PROVIDES = "virtual/libpackage"
RPROVIDES:${PN} = "virtual/libpackage"

SRCREV_lisapack = "91e2798617f1b6d3f50963726ba81016fa7b558c"

SRC_URI = "${CMF_GITHUB_ROOT}/libpackage;${CMF_GITHUB_SRC_URI_SUFFIX};name=lisapack"
SRCREV_FORMAT = "lisapack"

S = "${WORKDIR}/git"

inherit cmake

