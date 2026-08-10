SUMMARY = "Thunder Extensions"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig python3native

SRC_URI = "git://github.com/rdkcentral/ThunderExtensions;protocol=https;nobranch=1"

SRCREV ?= "R${PV}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

DEPENDS += "${THUNDER_NAMESPACE_LC} ${THUNDER_NAMESPACE_LC}-tools-native python3-jsonref-native"
RDEPENDS:${PN} += "${THUNDER_NAMESPACE_LC}"

# Explicitly stage the proxystubs subdir and headers into the sysroot so that
# recipes which DEPEND on this recipe can find libWPEFrameworkExtensionsMarshalling.
SYSROOT_DIRS:append = " ${libdir}/${THUNDER_NAMESPACE_LC}/proxystubs ${includedir}/${THUNDER_NAMESPACE}/extensions"

CXXFLAGS += " -Wall -Werror "
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"
PLUGIN_MAXPARALLEL ?= "8"

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DPLUGIN_PLUGININITIALIZERSERVICE_MAXPARALLEL=${PLUGIN_MAXPARALLEL} \
    -DEXT_PLUGIN_INITIALIZER=ON \
    -DEXT_MESSAGING_CONTROL=ON \
    -DPLUGIN_MESSAGECONTROL_AUTOSTART=false \
"

# Blank FILES_SOLIBSDEV so the solib auto-assignment doesn't interfere;
# ${libdir}/* below catches everything under libdir recursively.
FILES_SOLIBSDEV = ""

FILES:${PN} += " \
    ${libdir}/* \
    ${datadir}/${THUNDER_NAMESPACE}/* \
    ${sysconfdir}/${THUNDER_NAMESPACE}/extensions/*.json \
"

# Headers installed by interfaces/CMakeLists.txt to
# ${includedir}/${THUNDER_NAMESPACE}/extensions — assign them to ${PN}-dev.
# Listing the directory (no trailing /*) causes BitBake to recursively
# include all files inside it.
FILES:${PN}-dev += " \
    ${includedir}/${THUNDER_NAMESPACE}/extensions \
    ${libdir}/cmake/* \
"

FILES:${PN}-dbg += " \
    ${libdir}/${THUNDER_NAMESPACE_LC}/proxystubs/.debug/ \
    ${libdir}/${THUNDER_NAMESPACE_LC}/extensions/.debug/ \
"

# dev-so: .so namelinks are intentionally in ${PN} (pulled in via ${libdir}/*)
# libdir: libraries installed to non-standard libdir subdirs
# staticdev: no separate static lib package needed
INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"

