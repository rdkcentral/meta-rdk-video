######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

SUMMARY = "Rialto"
LICENSE  = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=327e572d11c37963726ba0b02d30cf2c"

require rialto_revision.inc

SRCREV = "4a076e05ae0567253a12d7bb7cfb943ee4d0188c"

SRC_URI = "${CMF_GITHUB_ROOT}/rialto;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GITHUB_MASTER_BRANCH}"
SRC_URI += "file://0001-link-rdkgstreamerutilsplatform.patch"

DEPENDS = "openssl jsoncpp protobuf protobuf-native"
DEPENDS:append = " virtual/vendor-rdk-gstreamer-utils-platform "

S = "${WORKDIR}/git"
inherit pkgconfig cmake coverity features_check

EXTRA_OECMAKE += " ${@bb.utils.contains("IMAGE_FEATURES", "prod", "-DRIALTO_BUILD_TYPE=Release", "-DRIALTO_BUILD_TYPE=Debug", d)} "

PACKAGES =+ "${PN}-client ${PN}-server ${PN}-servermanager-lib ${PN}-servermanager ${PN}-client-dev ${PN}-server-dev ${PN}-servermanager-lib-dev ${PN}-servermanager-dev "

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
PACKAGECONFIG[server] = "-DENABLE_SERVER=ON,-DENABLE_SERVER=OFF,wpeframework-clientlibraries gstreamer1.0 gstreamer1.0-plugins-base glib-2.0 virtual/vendor-rdk-gstreamer-utils-platform rdk-gstreamer-utils"
PACKAGECONFIG[servermanager] = "-DENABLE_SERVER_MANAGER=ON,-DENABLE_SERVER_MANAGER=OFF,mongoose,"

# The 'servermanager' package config has a runtime
# dependency on the 'RialtoServer' executable and as such
# requires the 'server' package config to be enabled as well.
PACKAGECONFIG ??= "server servermanager"

RDEPENDS:${PN} += "protobuf mongoose"
RDEPENDS:${PN}-server += " virtual/vendor-rdk-gstreamer-utils-platform rdk-gstreamer-utils"
RDEPENDS:${PN}-servermanager += "${PN}-server"
RDEPENDS:${PN}-servermanager-lib += " virtual/vendor-rdk-gstreamer-utils-platform rdk-gstreamer-utils"

FILES:${PN}-client += "${libdir}/libRialtoClient.so.*"
FILES:${PN}-server += "${bindir}/RialtoServer"
FILES:${PN}-servermanager += "${bindir}/RialtoServerManagerSim"
FILES:${PN}-servermanager-lib += "${libdir}/libRialtoServerManager.so.*"
FILES:${PN}-client-dev += " ${libdir}/libRialtoClient.so"
FILES:${PN}-servermanager-lib-dev += " ${includedir}/rialto ${libdir}/libRialtoServerManager.so"

REQUIRED_DISTRO_FEATURES = "enable_rialto"

#Needed for Kirkstone packagegroup error
DEBIAN_NOAUTONAME_${PN} = "1"
DEBIAN_NOAUTONAME_${PN}-dev = "1"
DEBIAN_NOAUTONAME_${PN}-client-dev = "1"
DEBIAN_NOAUTONAME_${PN}-server-dev = "1"
DEBIAN_NOAUTONAME_${PN}-servermanager-dev = "1"
DEBIAN_NOAUTONAME_${PN}-servermanager-lib-dev = "1"
DEBIAN_NOAUTONAME_${PN}-dbg = "1"
DEBIAN_NOAUTONAME_${PN}-client = "1" 
DEBIAN_NOAUTONAME_${PN}-server = "1"
DEBIAN_NOAUTONAME_${PN}-servermanager-lib = "1"
DEBIAN_NOAUTONAME_${PN}-servermanager = "1"
