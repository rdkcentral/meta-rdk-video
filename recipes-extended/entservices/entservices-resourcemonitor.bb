SUMMARY = "ResourceMonitor Thunder Plugin"
DESCRIPTION = "Plugin to monitor PSI, swap, memory, and flash space"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "1.0"
PR = "r0"

# local path to your plugin sources
SRCREV = "${PV}"
SRC_URI = "${CMF_GITHUB_ROOT}/entservices-resourcemonitor;${CMF_GITHUB_SRC_URI_SUFFIX}"
S = "${WORKDIR}/git"

inherit cmake pkgconfig

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"

DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

DEPENDS += "wpeframework wpeframework-tools-native wpeframework-clientlibraries rfc entservices-apis"
RDEPENDS:${PN} += "wpeframework rfc"
DEPENDS += "packager-headers gtest googletest"

CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/wdmp-c/ "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/trower-base64/ "
CXXFLAGS += " -DRFC_ENABLED "

CXXFLAGS += " -DNET_DEFINED_INTERFACES_ONLY -DNET_NO_LINK_LOCAL_ANNOUNCE "
CXXFLAGS += " -Wall -Werror "
CXXFLAGS:remove_morty = " -Wall -Werror "
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"

PACKAGECONFIG ?= "resourcemonitor erm"
PACKAGES += "${PN}-l1tests"

EXTRA_OEMAKE = "VERBOSE=1"
EXTRA_OECMAKE += "-DCMAKE_VERBOSE_MAKEFILE=ON"
EXTRA_OECMAKE += " \
    -DPLUGIN_RESOURCEMONITOR=ON \
    -DRDK_SERVICES_L1_TEST=ON \
"

inherit features_check
PACKAGECONFIG[resourcemonitor] = "-DPLUGIN_RESOURCEMONITOR=ON,-DPLUGIN_RESOURCEMONITOR=OFF,"

do_install:append() {
    install -d ${D}${sysconfdir}/rfcdefaults
    if ${@bb.utils.contains_any("DISTRO_FEATURES", "rdkshell_ra second_form_factor", "true", "false", d)}
    then
      install -m 0644 ${WORKDIR}/rdkservices.ini ${D}${sysconfdir}/rfcdefaults/
    fi
    if ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'true', 'false', d)} == 'true'; then
        if [ -d "${D}/etc/WPEFramework/plugins" ]; then
            find ${D}/etc/WPEFramework/plugins/ -type f | xargs sed -i -r 's/"autostart"[[:space:]]*:[[:space:]]*true/"autostart":false/g'
        fi
    fi
}

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/libWPEFrameworkResourceMonitor.so \
                ${libdir}/wpeframework/plugins/*.so \
                ${sysconfdir}/wpeframework/plugins/*.conf \
                ${datadir}/WPEFramework/* \
                "
FILES:${PN}-l1tests += "\
    ${libdir}/libWPEFrameworkL1TestsIN.so \
"
INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
