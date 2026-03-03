SUMMARY = "WPEFramework extensions"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "1.0.0"
PR = "r0"

S = "${WORKDIR}/wpeframework-extensions"
inherit cmake pkgconfig

BRANCH ?= "dev/thunder-extensions"
SRCREV ?= "4e800f089faf884822c3c51bf64247c626e8a230"

SRC_URI = "git://github.com/rdkcentral/ThunderNanoServices.git;protocol=ssh;branch=${BRANCH};destsuffix=wpeframework-extensions"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"

DEPENDS += "wpeframework wpeframework-tools-native "
RDEPENDS:${PN} += "wpeframework"

# Explicitly stage the proxystubs subdir and headers into the sysroot so that
# recipes which DEPEND on this recipe can find libWPEFrameworkExtensionsMarshalling.
SYSROOT_DIRS:append = " ${libdir}/wpeframework/proxystubs ${includedir}/WPEFramework/extensions"

CXXFLAGS += " -Wall -Werror "
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"
PLUGIN_MAXPARALLEL ?= "8"

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DPLUGIN_PLUGININITIALIZERSERVICE_MAXPARALLEL=${PLUGIN_MAXPARALLEL} \
    -DEXT_PLUGIN_INITIALIZER=ON \
"



# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/wpeframework/proxystubs/*.so.* ${libdir}/*.so ${datadir}/WPEFramework/* ${sysconfdir}/WPEFramework/plugins/*.json"

FILES:${PN}-dev += "\
    ${libdir}/wpeframework/proxystubs/*.so \
    ${includedir}/WPEFramework/extensions \
"

# Ensure the namelink .so is always present for linker use (CMake NAMELINK_COMPONENT
# can cause it to be skipped when cmake class installs only the Runtime component).
do_install:append() {
    # Re-run install for the Development component to capture the namelink
    cmake --install ${B} --prefix ${D}${prefix} --component WPEFramework_Development
}

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
