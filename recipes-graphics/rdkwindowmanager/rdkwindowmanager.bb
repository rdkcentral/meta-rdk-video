DESCRIPTION = "RDK Window Manager"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=9b36bf6cc7d5808435a27926d9dc6f7d"

FILESEXTRAPATHS:prepend := "${THISDIR}:"
DEPENDS = "westeros wayland essos virtual/egl rapidjson jpeg libpng curl"

DEPENDS:append = "${@bb.utils.contains_any('DISTRO_FEATURES', 'prodlog-variant prod-variant', '', 'libsoup-2.4 boost libsyswrapper', d)}"

S = "${WORKDIR}/git"
PV = "1.9.0"
PR = "r0"

SRCREV = "e6ece3d8d186d68d600710f44af8ff5bfbf59241"
SRC_URI = "${CMF_GITHUB_ROOT}/rdk-window-manager;${CMF_GITHUB_SRC_URI_SUFFIX}"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

EXTRA_OECMAKE += "-DRDK_WINDOW_MANAGER_BUILD_APP=OFF"
EXTRA_OECMAKE += "-DRDK_WINDOW_MANAGER_BUILD_TEST_APP=OFF"

inherit cmake pkgconfig

EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', 'prodlog-variant prod-variant', '-DRDK_WINDOW_MANAGER_VNC_SERVER=OFF', '-DRDK_WINDOW_MANAGER_VNC_SERVER=ON', d)}"

do_install() {
    install -d ${D}/${libdir}
    install -d ${D}${libdir}/plugins/westeros
    install -d ${D}/${includedir}

    install -m 755 ${B}/librdkwindowmanager.so ${D}/${libdir}/

    if [ -d ${B}/extensions/firebolt_shell ]; then
       install -m 755  ${B}/extensions/firebolt_shell/librdkwmextfireboltshell.so ${D}/${libdir}/
       install -m 755 ${B}/extensions/firebolt_shell/libwstplugin_rdkwmfireboltshell.so ${D}/${libdir}/plugins/westeros/
       install -m 644 ${S}/extensions/firebolt_shell/include/firebolt_shell_protocol_client.h ${D}/${includedir}
    fi
    if [ -d ${B}/extensions/firebolt_surface ]; then
       install -m 755 ${B}/extensions/firebolt_surface/librdkwmextfireboltsurface.so ${D}/${libdir}/
       install -m 755 ${B}/extensions/firebolt_surface/libwstplugin_rdkwmfireboltsurface.so ${D}/${libdir}/plugins/westeros/
       install -m 644 ${S}/extensions/firebolt_surface/include/firebolt_surface_protocol_client.h ${D}/${includedir}
    fi
    if [ -d ${B}/extensions/firebolt_wm ]; then
       install -m 755 ${B}/extensions/firebolt_wm/librdkwmextfireboltwm.so ${D}/${libdir}/
       install -m 755 ${B}/extensions/firebolt_wm/libwstplugin_rdkwmfireboltwm.so ${D}/${libdir}/plugins/westeros/
       install -m 644 ${S}/extensions/firebolt_wm/include/firebolt_wm_protocol_client.h ${D}/${includedir}
    fi

    install -d ${D}/${bindir}
    if [ -f ${B}/rdkwindowmanager ]; then
       install -m 0755 ${B}/rdkwindowmanager ${D}/${bindir}/
    fi
    if [ -f ${B}/rdkwindowmanagertest ]; then
       install -m 0755 ${B}/rdkwindowmanagertest ${D}/${bindir}/
    fi

    if [ -f ${B}/rdkwmtest ]; then
        install -m 0755 ${B}/rdkwmtest ${D}/${bindir}/
    fi

    install -d ${D}${includedir}/rdkwindowmanager/include/
    install -m 644 ${S}/include/*.h ${D}${includedir}/rdkwindowmanager/include/
}

INSANE_SKIP:${PN} = "installed-vs-shipped"
INSANE_SKIP:${PN} += "dev-so"
SOLIBS = ".so"
FILES_SOLIBSDEV = ""

FILES:${PN} += "${libdir}/librdkwindowmanager.so"
FILES:${PN} += "${libdir}/librdkwmextfireboltshell.so"
FILES:${PN} += "${libdir}/librdkwmextfireboltsurface.so"
FILES:${PN} += "${libdir}/librdkwmextfireboltwm.so"
FILES:${PN} += "${libdir}/plugins/westeros/libwstplugin_rdkwmfireboltsurface.so"
FILES:${PN} += "${libdir}/plugins/westeros/libwstplugin_rdkwmfireboltshell.so"
FILES:${PN} += "${libdir}/plugins/westeros/libwstplugin_rdkwmfireboltwm.so"
FILES:${PN} += "${bindir}/rdkwindowmanager"
FILES:${PN} += "${bindir}/rdkwindowmanagertest"
FILES:${PN} += "${bindir}/rdkwmtest"

