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
NETWORKMANAGER_LOGLEVEL ?= "4"

PR = "r0"
PV = "0.23.0"
S = "${WORKDIR}/git"

SRC_URI = "git://github.com/rdkcentral/networkmanager.git;protocol=https;branch=topic/RDKEMW-7094
"

# Jul 30, 2025
SRCREV = "f7b63691e56ce04e9d3d9f0eb6dcddba1d78c1e4"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DEPENDS = " openssl rdk-logger zlib boost curl glib-2.0 wpeframework entservices-apis wpeframework-tools-native libsoup-2.4 gupnp gssdp telemetry  ${@bb.utils.contains('DISTRO_FEATURES', 'ENABLE_NETWORKMANAGER', ' networkmanager ', ' iarmbus iarmmgrs ', d)} "
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

# Configure Logging for the Router Discovery Tool
inherit syslog-ng-config-gen logrotate_config
SYSLOG-NG_FILTER = "routerDiscovery"
SYSLOG-NG_SERVICE_routerDiscovery = "routerDiscovery@wlan0.service routerDiscovery@eth0.service"
SYSLOG-NG_DESTINATION_routerDiscovery = "routerInfo.log"
SYSLOG-NG_LOGRATE_routerDiscovery = "low"

LOGROTATE_NAME="routerDiscovery"
LOGROTATE_LOGNAME_routerDiscovery="routerInfo.log"
LOGROTATE_SIZE_routerDiscovery="512000"
LOGROTATE_ROTATION_routerDiscovery="3"
LOGROTATE_SIZE_MEM_routerDiscovery="512000"
LOGROTATE_ROTATION_MEM_routerDiscovery="3"

#Restrict debian package renaming
DEBIAN_NOAUTONAME:${PN} = "1"
DEBIAN_NOAUTONAME:${PN}-dev = "1"
DEBIAN_NOAUTONAME:${PN}-dbg = "1"

# Configure RootFS stuff
FILES:${PN} += "${bindir}/* "
FILES:${PN} += "${libdir}/* "
FILES:${PN} += "${sysconfdir}/* "
FILES:${PN} += "${systemd_unitdir}/system/* "
FILES:${PN} += "${base_libdir}/rdk/*"
FILES:${PN}-dev += "${includedir}/*"
