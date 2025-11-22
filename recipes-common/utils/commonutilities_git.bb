SUMMARY = "RDK commonutilities"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=24691c8ce48996ecd1102d29eab1216e"

# To have a possibility to override SRC_URI later, we are introducing the following workaround:
SRCREV = "071361f284ba9049bf7d8cb9a75b583b9b1e353b"
SRC_URI = "${CMF_GITHUB_ROOT}/common_utilities;module=.;${CMF_GITHUB_SRC_URI_SUFFIX}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DEPENDS +=" cjson curl rdk-logger"

#uncomment the following line to turn on debugging
#CFLAGS:append = " -DCURL_DEBUG"
# or enable this distro feature
CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'debug_curl_cdl', ' -DCURL_DEBUG', '', d)}"

CFLAGS:append = " -DRDK_LOGGER"

PV ?= "1.1.0"
PR ?= "r0"

S = "${WORKDIR}/git"

inherit autotools pkgconfig coverity

