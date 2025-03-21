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

PR = "r3"
PV = "0.11.0"
S = "${WORKDIR}/git"

SRC_URI = "git://github.com/rdkcentral/networkmanager.git;protocol=https;branch=develop"

# Mar 13, 2025
SRCREV = "341ac43aab9ec7321177fb9d99a62986fc5c0b64"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DEPENDS = " openssl rdk-logger zlib boost curl glib-2.0 wpeframework rdkservices-apis wpeframework-tools-native libsoup-2.4 gupnp gssdp telemetry  ${@bb.utils.contains('DISTRO_FEATURES', 'ENABLE_NETWORKMANAGER', ' networkmanager ', ' iarmbus iarmmgrs ', d)} "
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
                -DUSE_TELEMETRY=ON \
                -DENABLE_ROUTER_DISCOVERY_TOOL=ON \
                "
inherit syslog-ng-config-gen logrotate_config
SYSLOG-NG_FILTER = "routerDiscovery"
SYSLOG-NG_SERVICE_routerDiscovery = "routerDiscovery@wlan0.service routerDiscovery@eth0.service"
SYSLOG-NG_DESTINATION_routerDiscovery = "routerInfo.log"
SYSLOG-NG_LOGRATE_routerDiscovery = "low"

LOGROTATE_NAME="routerDiscovery"
LOGROTATE_LOGNAME_routerDiscovery="routerInfo.log"
#HDD_ENABLE
LOGROTATE_SIZE_routerDiscovery="512000"
LOGROTATE_ROTATION_routerDiscovery="3"
#HDD_DISABLE
LOGROTATE_SIZE_MEM_routerDiscovery="512000"
LOGROTATE_ROTATION_MEM_routerDiscovery="3"

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
   install -d ${D}${bindir}
   install -m 0755 ${B}/upnp/routerDiscovery ${D}${bindir}
   install -d ${D}${systemd_unitdir}/system
   install -m 0644 ${S}/upnp/scripts/routerDiscovery@.service ${D}${systemd_unitdir}/system
   install -d ${D}${base_libdir}/rdk/
   install -m 0755 ${S}/upnp/scripts/getRouterInfo-NMdispatcher.sh ${D}${base_libdir}/rdk/getRouterInfo.sh
   install -m 0755 ${S}/upnp/scripts/readyToGetRouterInfo.sh ${D}${base_libdir}/rdk/
}

FILES:${PN} += "${bindir}/routerDiscovery"
FILES:${PN} += "${systemd_unitdir}/system/routerDiscovery@.service"
FILES:${PN} += "${base_libdir}/rdk/*"
FILES:${PN} += "${libdir}/* ${sysconfdir}/*"
FILES:${PN}-dev += "${includedir}/WPEFramework/interfaces/INetworkManager.h"
