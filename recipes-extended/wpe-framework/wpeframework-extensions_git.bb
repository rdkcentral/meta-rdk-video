SUMMARY = "WPEFramework extensions"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "1.0.0"
PR = "r2"

S = "${WORKDIR}/wpeframework-extensions"
inherit cmake pkgconfig

BRANCH ?= "dev/thunder-extention"
SRCREV ?= "e9755aff47bde52395813ac8115094842f8cdf54"

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

# Blank FILES_SOLIBSDEV so the solib auto-assignment doesn't interfere;
# ${libdir}/* below catches everything under libdir recursively.
FILES_SOLIBSDEV = ""

FILES:${PN} += " \
    ${libdir}/* \
    ${datadir}/WPEFramework/* \
    ${sysconfdir}/WPEFramework/plugins/*.json \
"

# Headers installed by interfaces/CMakeLists.txt to
# ${includedir}/WPEFramework/extensions — assign them to ${PN}-dev.
# Listing the directory (no trailing /*) causes BitBake to recursively
# include all files inside it.
FILES:${PN}-dev += " \
    ${includedir}/WPEFramework/extensions \
    ${libdir}/cmake/* \
"

FILES:${PN}-dbg += " \
    ${libdir}/wpeframework/proxystubs/.debug/ \
    ${libdir}/wpeframework/plugins/.debug/ \
"

# dev-so: .so namelinks are intentionally in ${PN} (pulled in via ${libdir}/*)
# libdir: libraries installed to non-standard libdir subdirs
# staticdev: no separate static lib package needed
INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"

