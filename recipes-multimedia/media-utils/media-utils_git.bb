SUMMARY = "media-utils"
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PN = "media-utils"
PV ?= "1.0.0"
PR ?= "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI = "${CMF_GITHUB_ROOT}/media_utils;${CMF_GITHUB_SRC_URI_SUFFIX}"
SRCREV ?= "bfb7481702abcaeacea7f10aaf2c679655ca4b0e"

S = "${WORKDIR}/git"

PROVIDES = "virtual/vendor-media-utils"
RPROVIDES:${PN} = "virtual/vendor-media-utils"

DEPENDS = "media-utils-headers glib-2.0"

inherit autotools pkgconfig

CFLAGS:append = " \
    -I${STAGING_INCDIR}/media-utils \
    -I${STAGING_INCDIR}/media-utils/audioCapture \
    "
