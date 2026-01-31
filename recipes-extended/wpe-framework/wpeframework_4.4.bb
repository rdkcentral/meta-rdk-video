SUMMARY = "Thunder Framework"

LICENSE = "Apache-2.0"
HOMEPAGE = "https://github.com/rdkcentral/Thunder"

LIC_FILES_CHKSUM = "file://LICENSE;md5=85bcfede74b96d9a58c6ea5d4b607e58"

DEPENDS = "zlib wpeframework-tools-native rfc thunder-hang-recovery"
DEPENDS:append:libc-musl = " libexecinfo"
DEPENDS += "breakpad-wrapper"

# Need gst-svp-ext which is an abstracting lib for metadata
DEPENDS +=  "${@bb.utils.contains('DISTRO_FEATURES', 'rdk_svp', 'gst-svp-ext', '', d)}"

PR = "r39"
PV = "4.4.3"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI = "git://github.com/rdkcentral/Thunder.git;protocol=https;branch=R4_4;name=thunder"

SRCREV_thunder = "19100433e5517c743738bb2a9ed8ce2f79c10eaf"

SRC_URI += "file://wpeframework-init \
           file://wpeframework.service.in \
           file://r4.4/Library_version_matched_with_release_tag.patch \
           file://r4.4/Remove_versioning_for_executables.patch \
           file://r4.4/wpeframework_version.patch \
           file://r4.4/0001_Remove_DEBUG_Macro_Definition.patch \
           file://r4.4/0003-OCDM-increase-RPC-comm-timeout.patch \
           file://r4.4/wpeframework_added_optimization_flag_improvement.patch \
           file://r4.4/LLAMA-2254_fix_netlink_buffer_size_error.patch \
           file://r4.4/1001-Disable-MessageDispatcher-Enable-stderr.patch \
           file://r4.4/1002-Update-CMake-Module-Path.patch \
           file://r4.4/Use-Monotonic-Clock-Time-Now.patch \
           file://r4.4/RDKTV-15803-WPEFramework-crash-malloc-printerr.patch \
           file://r4.4/DELIA-54331-Do-not-set-the-receive-buffer.patch \
           file://r4.4/RDKTV-16992-Added_timeout-and-synchronisation-when-stopping-containers.patch \
           file://r4.4/0001-WPEFramework-Regex-Removal-r4.4.1.patch \
           file://r4.4/crash_debug_callstack_R4_4.patch \
           file://r4.4/wpeframework_persistentpathchanges.patch \
           file://r4.4/1004-Add-support-for-project-dir.patch \
           file://r4.4/Enable_Thunder_Logging_R4.4.1.patch \
           file://r4.4/Thunder_FirmwareUpdate_USB_Mount_Error_codes.patch \
           file://r4.4/R4-wpeframework-sd_notify.patch \
           file://r4.4/RDKEMW-733-Add-ENTOS-IDS.patch \
           file://r4.4/Update-Trace-Level-Logging-Logic.patch \
           file://r4.4/Activating_plugins_Logs_COMRPC.patch \
           file://r4.4/Removed_Autostart_Check_From_WPEFramework.patch \
           file://r4.4/Append_WorkerPool_Info.patch \
           file://r4.4/Revert_PR-665_support_JSON_Parsing.patch \
           "

SRC_URI += "file://r4.4/PR-1369-Wait-for-Open-in-Communication-Channel.patch \
            file://r4.4/PR-1756-Remove-Recursive-Function-From-SmartLinkType.patch \
            file://r4.4/PR-1586-Use-Monotonic-Clocks.patch\
            file://r4.4/PR-1533-Refernce-counted-Library-COMRPC-objects.patch  \
            file://r4.4/PR-1751-Load-Library-ThunderR4.patch \
            file://r4.4/PR-1785-Reduce_scope_of_adminlock.patch \
            file://r4.4/PR-1791-Thunder-hung-SocketPort-close-Delete-channel.patch \
            file://r4.4/PR-1797-SocketPort-Closed.patch \
            file://r4.4/PR1832-Thunder-ABBA-Deadlock-Fix.patch \
            file://r4.4/0001-DELIA-65784-Hibernation-fixes-for-R4.4.patch \
            file://r4.4/0001-SmarkLink-Crash-Fix.patch \
            file://r4.4/Jsonrpc_dynamic_error_handling.patch \
            file://r4.4/PR-1923-RDKEMW-6261-to-improve-system-shutdown-time-upon-R4.4.3.patch \
            file://r4.4/rdkemw-124-Link-Breakpad-wrapper.patch \
            file://r4.4/RDKEMW-8889-Avoid-LoadMeta-On-Boot.patch \
            file://r4.4/0001-To-handle-truncated-UTF-code-on-parsing-empty-null-t.patch \
            file://r4.4/0002-Print-Log-Upon-Time-ComRPC-Timeout.patch \
            file://r4.4/RDKEMW-10951_WPEFramework_Config_Override.patch \
            file://r4.4/RDKTV-39688-fix-sysinfo-mem-unit-fallback.patch \
           "

S = "${WORKDIR}/git"
TOOLCHAIN = "gcc"


inherit cmake pkgconfig systemd python3native add-version

WPEFRAMEWORK_PERSISTENT_PATH = "/opt/persistent/rdkservices"
WPEFRAMEWORK_SYSTEM_PREFIX = "OE"
WPEFRAMEWORK_PORT = "9998"
WPEFRAMEWORK_BINDING = "127.0.0.1"
WPEFRAMEWORK_IDLE_TIME = "0"
WPEFRAMEWORK_THREADPOOL_COUNT ?= "16"
WPEFRAMEWORK_EXIT_REASONS ?= "WatchdogExpired"


BREAKPAD_LDFLAGS:pn-wpeframework = "${BACKTRACE_LDFLAGS}"
EXTRA_OECMAKE:append = ' -DBREAKPAD_LDFLAGS="${BREAKPAD_LDFLAGS}"'
LDFLAGS:remove:pn-wpeframework = "${@LOG_BACKTRACE == 'y' and BACKTRACE_LDFLAGS or ''}"

PACKAGECONFIG ?= " \
    release \
    virtualinput \
    websocket \
    "

PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'com', '', d)}"

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
# The WPEFramework also needs limited Plugin info in order to determine what to put in the "resumes" configuration
# it feels a bit the other way around but lets set at least webserver and webkit
PACKAGECONFIG[websource]       = "-DPLUGIN_WEBSERVER=ON,,"
PACKAGECONFIG[webkitbrowser]   = "-DPLUGIN_WEBKITBROWSER=ON,,"
PACKAGECONFIG[websocket]       = "-DWEBSOCKET=ON,,"

PACKAGECONFIG[com] = "-DCOM=ON,,,"

# FIXME, determine this a little smarter
# Provision event is required for libprovision and provision plugin
# Location event is required for locationsync plugin
# Time event is required for timesync plugin
# Identifier event is required for Compositor plugin
# Internet event is provided by the LocationSync plugin
# WebSource event is provided by the WebServer plugin

WPEFRAMEWORK_EXTERN_EVENTS ?= "\
Decryption \
${@bb.utils.contains('PACKAGECONFIG', 'websource', 'WebSource ', '', d)}\
Location Time Internet Provisioning \
${@bb.utils.contains('DISTRO_FEATURES', 'thunder_security_disable', '', 'Security ', d)}\
"

EXTRA_OECMAKE += " \
    -DINSTALL_HEADERS_TO_TARGET=ON \
    -DEXTERN_EVENTS="${WPEFRAMEWORK_EXTERN_EVENTS}" \
    -DEXCEPTIONS_ENABLE=ON \  
    -DBUILD_SHARED_LIBS=ON \
    -DRPC=ON \
    -DBUILD_REFERENCE=${SRCREV} \
    -DTREE_REFERENCE=${SRCREV_thunder} \
    -DPORT=${WPEFRAMEWORK_PORT} \
    -DBINDING=${WPEFRAMEWORK_BINDING} \
    -DENABLED_TRACING_LEVEL=2 \
    -DPERSISTENT_PATH=${WPEFRAMEWORK_PERSISTENT_PATH} \
    -DSYSTEM_PREFIX=${WPEFRAMEWORK_SYSTEM_PREFIX} \
    -DIDLE_TIME=${WPEFRAMEWORK_IDLE_TIME} \
    -DTHREADPOOL_COUNT=${WPEFRAMEWORK_THREADPOOL_COUNT} \
    -DHIDE_NON_EXTERNAL_SYMBOLS=OFF \
    -DEXIT_REASONS=${WPEFRAMEWORK_EXIT_REASONS} \
    -DMESSAGING=ON \
    -DCMAKE_SYSROOT=${STAGING_DIR_HOST} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'RDKTV_APP_HIBERNATE', ' -DHIBERNATESUPPORT=ON -DHIBERNATE_CHECKPOINTSERVER=ON','',d)} \
"

EXTRA_OECMAKE += " -DLEGACY_CONFIG_GENERATOR=OFF"

EXTRA_OECMAKE:append = ' -DPOSTMORTEM_PATH=/opt/secure/minidumps'

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/wpeframework.service.in  ${D}${systemd_unitdir}/system/wpeframework.service
}

SYSTEMD_SERVICE:${PN} = "wpeframework.service"

# ----------------------------------------------------------------------------

PACKAGES =+ "${PN}-initscript"

FILES:${PN}-initscript = "${sysconfdir}/init.d/wpeframework"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so ${datadir}/WPEFramework/* ${PKG_CONFIG_DIR}/*.pc"
FILES:${PN} += "${includedir}/cdmi.h"
FILES:${PN} += "${systemd_unitdir}/system/wpeframework.service.d/network_manager_migration.conf"
FILES:${PN}-dev += "${libdir}/cmake/*"
FILES:${PN}-dbg += "${libdir}/wpeframework/proxystubs/.debug/"

# ----------------------------------------------------------------------------

INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"

# ----------------------------------------------------------------------------

RDEPENDS:${PN}_rpi = "userland"
RDEPENDS:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'rdk_svp', 'gst-svp-ext', '', d)} thunder-hang-recovery"
# Should be able to remove this when generic rdk_svp flag
RDEPENDS:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'sage_svp', 'gst-svp-ext', '', d)}"

RDEPENDS:${PN}:append:rpi = " ${@bb.utils.contains('DISTRO_FEATURES', 'vc4graphics', '', 'userland', d)}"

inherit breakpad-logmapper syslog-ng-config-gen logrotate_config

SYSLOG-NG_FILTER = "wpeframework"
SYSLOG-NG_SERVICE_wpeframework = "wpeframework.service thunderHangRecovery.service"
SYSLOG-NG_DESTINATION_wpeframework = "wpeframework.log"
SYSLOG-NG_LOGRATE_wpeframework = "high"

LOGROTATE_NAME="wpeframework"
LOGROTATE_LOGNAME_wpeframework="wpeframework.log"
LOGROTATE_SIZE_wpeframework="1572864"
LOGROTATE_ROTATION_wpeframework="3"
LOGROTATE_SIZE_MEM_wpeframework="1572864"
LOGROTATE_ROTATION_MEM_wpeframework="3"

# Breakpad processname and logfile mapping
BREAKPAD_LOGMAPPER_PROCLIST = "WPEFramework,WorkerPool::Thr,WPEProcess,WPEWebProcess,startWPE,WPENetworkProcess,WideVine.drm,PlayReady.drm,NetworkManager,Monitor::IResou"
BREAKPAD_LOGMAPPER_LOGLIST = "wpeframework.log"

# Ensure we'll get the Thunder version  into the versions.txt file part of the build image
do_add_version () {
    echo "WPEFRAMEWORK-VERSION=${THUNDER_RELEASE_TAG_NAME}" > ${EXTRA_VERSIONS_PATH}/${PN}.txt
}

