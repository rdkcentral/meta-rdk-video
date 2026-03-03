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

# Tell OE's packaging machinery where the solib namelink (.so) lives so it is
# correctly assigned to ${PN}-dev.  Blanking FILES_SOLIBSDEV intercepts .so
# files before normal FILES assignment runs, causing unshipped-file QA errors.
FILES_SOLIBSDEV = "${libdir}/wpeframework/proxystubs/*.so"

# Runtime package: versioned shared libs (.so.*) and plugin .so files
# (plugin .so files are dlopen'd at runtime, not linked, hence dev-so skip)
FILES:${PN} += " \
    ${libdir}/wpeframework/plugins/*.so \
    ${libdir}/wpeframework/proxystubs/*.so.* \
    ${datadir}/WPEFramework/* \
    ${sysconfdir}/WPEFramework/plugins/*.json \
"

# Dev package: namelink (handled via FILES_SOLIBSDEV above) + headers.
# Use directory path (no trailing /*) so BitBake matches recursively.
FILES:${PN}-dev += " \
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
