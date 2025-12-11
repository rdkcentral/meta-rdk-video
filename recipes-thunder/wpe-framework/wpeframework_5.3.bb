SUMMARY = "Thunder Framework"

LICENSE = "Apache-2.0"
HOMEPAGE = "https://github.com/rdkcentral/Thunder"

LIC_FILES_CHKSUM = "file://LICENSE;md5=85bcfede74b96d9a58c6ea5d4b607e58"

DEPENDS = "zlib wpeframework-tools-native"
DEPENDS:append:libc-musl = " libexecinfo"
DEPENDS += "breakpad-wrapper"


PR = "r0"
PV = "5.3.0"

SRC_URI = "git://github.com/rdkcentral/Thunder.git;protocol=https;branch=R5_3;name=thunder"

SRCREV_thunder = "R5.3.0"

SRC_URI += "file://thunder.service.in \
            file://Backward-compatiblity-cmake-function-thunder-r4.patch \
           "

S = "${WORKDIR}/git"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"


inherit cmake pkgconfig systemd python3native add-version

THUNDER_PERSISTENT_PATH = "/opt/persistent/rdkservices"
THUNDER_SYSTEM_PREFIX = "OE"
THUNDER_PORT = "9999"
THUNDER_BINDING = "127.0.0.1"
THUNDER_IDLE_TIME = "0"
THUNDER_THREADPOOL_COUNT ?= "16"
THUNDER_EXIT_REASONS ?= "WatchdogExpired"


BREAKPAD_LDFLAGS:pn-thunder = "${BACKTRACE_LDFLAGS}"
EXTRA_OECMAKE:append = ' -DBREAKPAD_LDFLAGS="${BREAKPAD_LDFLAGS}"'

PACKAGECONFIG ?= " \
    release \
    virtualinput \
    websocket \
    com \
    "


# Buildtype
# Maybe we need to couple this to a Yocto feature
PACKAGECONFIG[debug]            = "-DCMAKE_BUILD_TYPE=Debug,-DCMAKE_BUILD_TYPE=Release,"
PACKAGECONFIG[release]          = "-DCMAKE_BUILD_TYPE=Release,-DCMAKE_BUILD_TYPE=Debug,"


PACKAGECONFIG[cyclicinspector]  = "-DTEST_CYCLICINSPECTOR=ON,-DTEST_CYCLICINSPECTOR=OFF,"
PACKAGECONFIG[provisionproxy]   = "-DPROVISIONPROXY=ON,-DPROVISIONPROXY=OFF,libprovision"
PACKAGECONFIG[testloader]       = "-DTEST_LOADER=ON,-DTEST_LOADER=OFF,"
PACKAGECONFIG[virtualinput]     = "-DVIRTUALINPUT=ON,-DVIRTUALINPUT=OFF,"
PACKAGECONFIG[bluetooth]        = "-DBLUETOOTH_SUPPORT=ON,-DBLUETOOTH_SUPPORT=OFF,"

PACKAGECONFIG[processcontainers]          = "-DPROCESSCONTAINERS=ON,-DPROCESSCONTAINERS=OFF,"
PACKAGECONFIG[processcontainers_dobby]    = "-DPROCESSCONTAINERS_DOBBY=ON,,dobby"

# FIXME
# The Thunder also needs limited Plugin info in order to determine what to put in the "resumes" configuration
# it feels a bit the other way around but lets set at least webserver and webkit
PACKAGECONFIG[websource]       = "-DPLUGIN_WEBSERVER=ON,,"
PACKAGECONFIG[webkitbrowser]   = "-DPLUGIN_WEBKITBROWSER=ON,,"
PACKAGECONFIG[websocket]       = "-DWEBSOCKET=ON,,"

PACKAGECONFIG[com] = "-DCOM=ON,,,"

EXTRA_OECMAKE += " \
    -DINSTALL_HEADERS_TO_TARGET=ON \
    -DEXTERN_EVENTS="${THUNDER_EXTERN_EVENTS}" \
    -DEXCEPTIONS_ENABLE=ON \  
    -DBUILD_REFERENCE=${SRCREV} \
    -DTREE_REFERENCE=${SRCREV_thunder} \
    -DPORT=${THUNDER_PORT} \
    -DBINDING=${THUNDER_BINDING} \
    -DENABLED_TRACING_LEVEL=2 \
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
    install -m 0644 ${WORKDIR}/thunder.service.in  ${D}${systemd_unitdir}/system/thunder.service
}

SYSTEMD_SERVICE:${PN} = "thunder.service"

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so ${datadir}/Thunder/* ${PKG_CONFIG_DIR}/*.pc"
FILES:${PN} += "${includedir}/cdmi.h"
FILES:${PN}-dev += "${libdir}/cmake/*"
FILES:${PN}-dbg += "${libdir}/thunder/proxystubs/.debug/"

# ----------------------------------------------------------------------------

INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"

# ----------------------------------------------------------------------------

RDEPENDS:${PN}_rpi = "userland"
RDEPENDS:${PN} += "thunder-hang-recovery"

RDEPENDS:${PN}:append:rpi = " ${@bb.utils.contains('DISTRO_FEATURES', 'vc4graphics', '', 'userland', d)}"

inherit breakpad-logmapper syslog-ng-config-gen logrotate_config

SYSLOG-NG_FILTER = "thunder"
SYSLOG-NG_SERVICE_thunder = "thunder.service"
SYSLOG-NG_DESTINATION_thunder = "thunder.log"
SYSLOG-NG_LOGRATE_thunder = "high"

LOGROTATE_NAME="thunder"
LOGROTATE_LOGNAME_thunder="thunder.log"
LOGROTATE_SIZE_thunder="1572864"
LOGROTATE_ROTATION_thunder="3"
LOGROTATE_SIZE_MEM_thunder="1572864"
LOGROTATE_ROTATION_MEM_thunder="3"

# Breakpad processname and logfile mapping
BREAKPAD_LOGMAPPER_PROCLIST = "Thunder,WorkerPool::Thr,Monitor::IResou"
BREAKPAD_LOGMAPPER_LOGLIST = "thunder.log"

# Ensure we'll get the Thunder version  into the versions.txt file part of the build image
do_add_version () {
    echo "THUNDER-VERSION=${THUNDER_RELEASE_TAG_NAME}" > ${EXTRA_VERSIONS_PATH}/${PN}.txt
}

