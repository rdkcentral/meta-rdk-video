SUMMARY = "This recipe provides the libpackage abstraction of DAC applicaitons"
SECTION = "rdk/libs"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "2.0.0"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

PROVIDES = "virtual/libpackage"
RPROVIDES:${PN} = "virtual/libpackage"

DEPENDS += "packager-headers"

SRCREV = "5a6a9dba78259c68611a4e4101e83af04e1bf4b9"
SRC_URI = "${CMF_GITHUB_ROOT}/libpackage;${CMF_GITHUB_SRC_URI_SUFFIX};name=lisapack"
SRCREV_FORMAT = "lisapack"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

PACKAGECONFIG ?= " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'enable_ralf', 'ralfsupport', '', d)} \
    "
PACKAGECONFIG[ralfsupport]    = "-DENABLE_RALF_SUPPORT=ON ${RALF_SUPPORT_ARGS}, -DENABLE_RALF_SUPPORT=OFF, ralf-utils jsoncpp, ralf-utils jsoncpp"
PACKAGECONFIG[depcheck]    = "-DDISABLE_DEPENDENCY_CHECK=OFF, -DDISABLE_DEPENDENCY_CHECK=ON"

DAC_APP_CERT_PATH ?= "/etc/rdk/certs"

RALF_SUPPORT_ARGS = " \
                      -DDAC_APP_PATH=${DAC_APP_PATH} \
                      -DRDK_PACKAGE_CERT_PATH=${DAC_APP_CERT_PATH} \
                      "
EXTRA_OECMAKE:append = " -DBUILD_REFERENCE=${SRCREV}"
