SUMMARY = "RDK commonutilities"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=24691c8ce48996ecd1102d29eab1216e"

# To have a possibility to override SRC_URI later, we are introducing the following workaround:
SRCREV = "b75c844bf0da6c856dc98f3c13a8e3c8910fc96f"
SRC_URI = "${CMF_GITHUB_ROOT}/common_utilities;module=.;${CMF_GITHUB_SRC_URI_SUFFIX}"


DEPENDS +=" cjson curl rdk-logger rdkcertconfig"
#RDEPENDS:{PN} += " rfc"

#uncomment the following line to turn on debugging
#CFLAGS:append = " -DCURL_DEBUG"
# or enable this distro feature
CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'debug_curl_cdl', ' -DCURL_DEBUG', '', d)}"

DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec', " ", d)}"
CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec',  ' `pkg-config --cflags libsafec`', '-fPIC', d)}"
CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"
LDFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --libs libsafec`', '', d)}"

# Disable all warnings as errors
LDFLAGS:append = " -lsafec -lsecure_wrapper"
CFLAGS:append = " -Wno-error"

CFLAGS:append = " -DRDK_LOGGER"

PV = "1.5.3"
PR = "r0"
S = "${WORKDIR}/git"

inherit autotools pkgconfig coverity
