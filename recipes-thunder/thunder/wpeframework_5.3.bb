SUMMARY = "Thunder Framework"

LICENSE = "Apache-2.0"
HOMEPAGE = "https://github.com/rdkcentral/Thunder"

LIC_FILES_CHKSUM = "file://LICENSE;md5=85bcfede74b96d9a58c6ea5d4b607e58"

DEPENDS = "zlib wpeframework-tools-native"
DEPENDS:append:libc-musl = " libexecinfo"
DEPENDS += "breakpad-wrapper"


PR = "r2"
PV = "5.3.0"

SRC_URI = "git://github.com/rdkcentral/Thunder.git;protocol=https;branch=R5_3;name=thunder"

SRCREV_thunder = "R5.3.0"

SRC_URI += "file://thunder.service.in \
            file://0001-Backward-compatiblity-cmake-function-thunder-r4.patch \
            file://0002-PersistentPath-thunder-r5.patch \
           "

S = "${WORKDIR}/git"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit cmake pkgconfig systemd python3native add-version

THUNDER_PERSISTENT_PATH = "/tmp/rdkservices"
THUNDER_SYSTEM_PREFIX = "OE"
THUNDER_PORT = "9998"
THUNDER_BINDING = "127.0.0.1"
THUNDER_IDLE_TIME = "0"
THUNDER_THREADPOOL_COUNT ?= "32"
THUNDER_EXIT_REASONS ?= "WatchdogExpired"


BREAKPAD_LDFLAGS:pn-thunder = "${BACKTRACE_LDFLAGS}"
EXTRA_OECMAKE:append = ' -DBREAKPAD_LDFLAGS="${BREAKPAD_LDFLAGS}"'

PACKAGECONFIG ?= " \
    release \
    "


# Buildtype & other packages
PACKAGECONFIG[debug]                        = "-DCMAKE_BUILD_TYPE=Debug,-DCMAKE_BUILD_TYPE=Release,"
PACKAGECONFIG[release]                      = "-DCMAKE_BUILD_TYPE=Release,-DCMAKE_BUILD_TYPE=Debug,"
PACKAGECONFIG[cyclicinspector]              = "-DTEST_CYCLICINSPECTOR=ON,-DTEST_CYCLICINSPECTOR=OFF,"
PACKAGECONFIG[provisionproxy]               = "-DPROVISIONPROXY=ON,-DPROVISIONPROXY=OFF,libprovision"
PACKAGECONFIG[bluetooth]                    = "-DBLUETOOTH_SUPPORT=ON,-DBLUETOOTH_SUPPORT=OFF,"
PACKAGECONFIG[processcontainers]            = "-DPROCESSCONTAINERS=ON,-DPROCESSCONTAINERS=OFF,"
PACKAGECONFIG[processcontainers_dobby]      = "-DPROCESSCONTAINERS_DOBBY=ON,,dobby"
PACKAGECONFIG[websocket]                    = "-DWEBSOCKET=ON,,"
PACKAGECONFIG[warningreporting]             = "-DWARNING_REPORTING=ON,-DWARNING_REPORTING=OFF,"
PACKAGECONFIG[profiler]                     = "-DPROFILER=ON,-DPROFILER=OFF,"
PACKAGECONFIG[securesocket]                 = "-DSECURE_SOCKET=ON,-DSECURE_SOCKET=OFF,openssl"


EXTRA_OECMAKE += " \
    -DINSTALL_HEADERS_TO_TARGET=ON \
    -DEXTERN_EVENTS="${THUNDER_EXTERN_EVENTS}" \
    -DEXCEPTIONS_ENABLE=ON \  
    -DBUILD_REFERENCE=${SRCREV} \
    -DTREE_REFERENCE=${SRCREV_thunder} \
    -DPORT=${THUNDER_PORT} \
    -DBINDING=${THUNDER_BINDING} \
    -DENABLED_TRACING_LEVEL=3 \
    -DPERSISTENT_PATH=${THUNDER_PERSISTENT_PATH} \
    -DSYSTEM_PREFIX=${THUNDER_SYSTEM_PREFIX} \
    -DIDLE_TIME=${THUNDER_IDLE_TIME} \
    -DTHREADPOOL_COUNT=${THUNDER_THREADPOOL_COUNT} \
    -DHIDE_NON_EXTERNAL_SYMBOLS=OFF \
    -DEXIT_REASONS=${THUNDER_EXIT_REASONS} \
    -DMESSAGING=ON \
    -DCMAKE_SYSROOT=${STAGING_DIR_HOST} \
    -DPOSTMORTEM_PATH=/opt/secure/minidumps \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/thunder.service.in  ${D}${systemd_unitdir}/system/wpeframework.service
}

SYSTEMD_SERVICE:${PN} = "wpeframework.service"

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so ${datadir}/Thunder/* ${PKG_CONFIG_DIR}/*.pc"
FILES:${PN}-dev += "${libdir}/cmake/*"
FILES:${PN}-dbg += "${libdir}/thunder/proxystubs/.debug/"

FILES:${PN} += " \
    ${libdir}/thunder/proxystubs/ \
"
# ----------------------------------------------------------------------------

INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"

# ----------------------------------------------------------------------------

RDEPENDS:${PN}_rpi = "userland"
RDEPENDS:${PN}:append:rpi = " ${@bb.utils.contains('DISTRO_FEATURES', 'vc4graphics', '', 'userland', d)}"

inherit breakpad-logmapper syslog-ng-config-gen logrotate_config

SYSLOG-NG_FILTER = "thunder"
SYSLOG-NG_SERVICE_thunder = "wpeframework.service"
SYSLOG-NG_DESTINATION_thunder = "wpeframework.log"
SYSLOG-NG_LOGRATE_thunder = "high"

LOGROTATE_NAME="thunder"
LOGROTATE_LOGNAME_thunder="wpeframework.log"
LOGROTATE_SIZE_thunder="1572864"
LOGROTATE_ROTATION_thunder="5"
LOGROTATE_SIZE_MEM_thunder="1572864"
LOGROTATE_ROTATION_MEM_thunder="5"

# Breakpad processname and logfile mapping
BREAKPAD_LOGMAPPER_PROCLIST = "Thunder,WorkerPool::Thr,Monitor::IResou,ThunderPlugin"
BREAKPAD_LOGMAPPER_LOGLIST = "wpeframework.log"


