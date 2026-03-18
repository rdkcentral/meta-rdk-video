SUMMARY          = "ralf-utils - library for working with app widgets / packages"
DESCRIPTION      = "C++ library for extracting and verifying app and runtime packages. \
Supports packages in the traditional W3C-like widget format and \
RALF (RDK Application Layer Format) / OCI Artifact format. \
"

HOMEPAGE         = "https://github.com/rdkcentral/ralf-utils"

LICENSE          = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=291858d2271fa690cffedb2d0abc5c11"
SRC_URI          = "${CMF_GITHUB_ROOT}/ralf-utils.git;${CMF_GITHUB_SRC_URI_SUFFIX}"
SRCREV           = "2eda857fd887dbefe915c4b5a2b7294d259073fd"
PV              ?= "1.2.0"
PR              ?= "r0"
S                = "${WORKDIR}/git"

do_unpack[network] = "1"
DEPENDS          = "openssl libxml2 libarchive lz4"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

# Disable building the tools and unit-tests, we only want the library
EXTRA_OECMAKE:append = " -DRALF_UTILS_BUILD_TOOLS:BOOL=OFF -DRALF_UTILS_BUILD_UNIT_TESTS:BOOL=OFF "

inherit cmake pkgconfig

