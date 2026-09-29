SUMMARY = "WPEFramework extensions"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "4.4.7"
PR = "r1"

S = "${WORKDIR}/git"
inherit cmake pkgconfig python3native

BRANCH ?= "R4_4"
SRCREV ?= "95e24b4b03c4aab1794200bc47b502436cd682fe"

SRC_URI = "git://github.com/rdkcentral/ThunderExtensions.git;protocol=https;branch=${BRANCH}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"

DEPENDS += "wpeframework wpeframework-tools-native python3-jsonref-native telemetry"
RDEPENDS:${PN} += "wpeframework telemetry"

# Explicitly stage the proxystubs subdir and headers into the sysroot so that
# recipes which DEPEND on this recipe can find libWPEFrameworkExtensionsMarshalling.
SYSROOT_DIRS:append = " ${libdir}/wpeframework/proxystubs ${includedir}/WPEFramework/extensions"

CXXFLAGS += " -Wall -Werror "
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"
PLUGIN_MAXPARALLEL ?= "16"

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DEXT_PLUGININITIALIZERSERVICE_MAXPARALLEL=${PLUGIN_MAXPARALLEL} \
    -DEXT_PLUGIN_INITIALIZER=ON \
    -DEXT_MESSAGING_CONTROL=ON \
    -DEXT_MESSAGINGCONTROL_AUTOSTART=true \
    -DEXT_MESSAGINGCONTROL_TELEMETRY_T2=ON \
    -DEXT_MESSAGINGCONTROL_TELEMETRY_COMPONENT="entertainmentservices" \
"



# ----------------------------------------------------------------------------

# Blank FILES_SOLIBSDEV so the solib auto-assignment doesn't interfere;
# ${libdir}/* below catches everything under libdir recursively.
FILES_SOLIBSDEV = ""

FILES:${PN} += " \
    ${libdir}/* \
    ${datadir}/WPEFramework/* \
    ${sysconfdir}/WPEFramework/extensions/*.json \
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
    ${libdir}/wpeframework/extensions/.debug/ \
"

# dev-so: .so namelinks are intentionally in ${PN} (pulled in via ${libdir}/*)
# libdir: libraries installed to non-standard libdir subdirs
# staticdev: no separate static lib package needed
INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"

