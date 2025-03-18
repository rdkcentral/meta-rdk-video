SUMMARY = "NetworkManager Thunder Plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

# connectivity configuration
NETWORKMANAGER_CONN_ENDPOINT_1 ?= "http://clients3.google.com/generate_204"
NETWORKMANAGER_CONN_MONITOR_INTERVAL ?= "60"

# stun configuration
NETWORKMANAGER_STUN_ENDPOINT ?= "stun.l.google.com"
NETWORKMANAGER_STUN_PORT ?= "19302"

# Default Loglevel configuration
NETWORKMANAGER_LOGLEVEL ?= "3"

PR = "r0"
PV = "0.11.0"
S = "${WORKDIR}/git"

SRC_URI = "git://github.com/rdkcentral/networkmanager.git;protocol=https;branch=develop"

# Mar 10, 2025
SRCREV = "d134cbdf0cadbbe951087883b7fb7003e33494a6"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DEPENDS = " openssl rdk-logger zlib boost curl glib-2.0 wpeframework rdkservices-apis wpeframework-tools-native ${@bb.utils.contains('DISTRO_FEATURES', 'ENABLE_NETWORKMANAGER', ' networkmanager ', ' iarmbus iarmmgrs ', d)} "
RDEPENDS:${PN} += " wpeframework rdk-logger curl ${@bb.utils.contains('DISTRO_FEATURES', 'ENABLE_NETWORKMANAGER', ' networkmanager ', ' iarmbus iarmmgrs ', d)} "

inherit cmake pkgconfig python3native

# Specify any options you want to pass to cmake using EXTRA_OECMAKE:
EXTRA_OECMAKE += " \
                -DCMAKE_SYSROOT=${STAGING_DIR_HOST} \
                -DPLUGIN_NETWORKMANAGER_CONN_ENDPOINT_1="${NETWORKMANAGER_CONN_ENDPOINT_1}" \
                -DPLUGIN_NETWORKMANAGER_STUN_ENDPOINT="${NETWORKMANAGER_STUN_ENDPOINT}" \
                -DPLUGIN_NETWORKMANAGER_STUN_PORT="${NETWORKMANAGER_STUN_PORT}" \
                -DPLUGIN_NETWORKMANAGER_LOGLEVEL="${NETWORKMANAGER_LOGLEVEL}" \
                -DPLUGIN_NETWORKMANAGER_CONN_MONITOR_INTERVAL="${NETWORKMANAGER_CONN_MONITOR_INTERVAL}" \
                ${@bb.utils.contains('DISTRO_FEATURES', 'ENABLE_NETWORKMANAGER', '-DENABLE_GNOME_NETWORKMANAGER=ON', '', d)}  \
                -DPLUGIN_BUILD_REFERENCE="${SRCREV}" \
                -DCMAKE_VERBOSE_MAKEFILE:BOOL=ON    \
                "

FILES:${PN} = "${libdir}/* ${sysconfdir}/*"
#Restrict debian package renaming
DEBIAN_NOAUTONAME:${PN} = "1"
DEBIAN_NOAUTONAME:${PN}-dev = "1"
DEBIAN_NOAUTONAME:${PN}-dbg = "1"

do_install() {
   install -d ${D}${includedir}/WPEFramework/interfaces
   install -d ${D}${libdir}/wpeframework/plugins
   install -d ${D}/etc/WPEFramework/plugins
   install -m 0644 ${S}/INetworkManager.h ${D}${includedir}/WPEFramework/interfaces
   install -m 0644 ${B}/libWPEFramework*.so ${D}${libdir}/wpeframework/plugins
   install -m 0644 ${B}/config/NetworkManager.json ${D}/etc/WPEFramework/plugins
   install -m 0644 ${B}/config/LegacyPlugin*.json ${D}/etc/WPEFramework/plugins
   if ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'true', 'false', d)} == 'true'; then
       if [ -d "${D}/etc/WPEFramework/plugins" ]; then
           find ${D}/etc/WPEFramework/plugins/ -type f | xargs sed -i -r 's/"autostart"[[:space:]]*:[[:space:]]*true/"autostart":false/g'
       fi
   fi
}

FILES:${PN} += "${libdir}/* ${sysconfdir}/*"
FILES:${PN}-dev += "${includedir}/WPEFramework/interfaces/INetworkManager.h"
