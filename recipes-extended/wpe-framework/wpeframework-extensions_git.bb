SUMMARY = "WPEFramework extensions"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "1.0.0"
PR = "r0"

S = "${WORKDIR}/wpeframework-extensions"
inherit cmake pkgconfig

BRANCH ?= "dev/thunder-extensions"
SRCREV ?= "${AUTOREV}"

SRC_URI = "git://github.com/rdkcentral/ThunderNanoServices.git;protocol=ssh;branch=${BRANCH};destsuffix=wpeframework-extensions"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"

DEPENDS += "wpeframework wpeframework-tools-native "
RDEPENDS:${PN} += "wpeframework"

CXXFLAGS += " -Wall -Werror "
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DEXT_SYSTEMD_CONNECTOR=ON \
    -DEXT_PLUGIN_INITIALIZER=ON \
"



# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/*.so ${datadir}/WPEFramework/* ${sysconfdir}/WPEFramework/plugins/*.json"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
