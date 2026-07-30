SUMMARY = "ES1 JSON-RPC round-trip benchmark plugin (ThunderNanoServices)"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d8927f3331d2b3e321b7dd1925166d25"

# --------------------------------------------------------------------------
# Version — bump PR when recipe changes, bump PV + SRCREV when source changes
# --------------------------------------------------------------------------
PV = "1.0.0"
PR = "r0"

# --------------------------------------------------------------------------
# Source — ThunderNanoServices on GitHub
# TODO: update SRCREV to the commit that includes the ES1Benchmark plugin
#       once the code is merged / tagged in the ThunderNanoServices repo.
# --------------------------------------------------------------------------
SRC_URI = "git://github.com/rdkcentral/ThunderNanoServices.git;protocol=https;branch=dev/es1benchmark;name=thundernanoservices"

SRCREV_thundernanoservices = "e7178ea91d03a42545b45cf5d8dbb8893f042566"

S = "${WORKDIR}/git"

# --------------------------------------------------------------------------
# Build
# --------------------------------------------------------------------------
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"

inherit cmake pkgconfig

DEPENDS = " \
    wpeframework \
    wpeframework-tools-native \
"

RDEPENDS:${PN} += "wpeframework"

# --------------------------------------------------------------------------
# CMake flags
# Only ES1Benchmark is switched ON.  Every other plugin is explicitly OFF so
# the build is fast and nothing unrelated gets compiled.
# --------------------------------------------------------------------------
EXTRA_OECMAKE += " \
    -DCMAKE_SYSROOT=${STAGING_DIR_HOST} \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    \
    -DPLUGIN_ES1BENCHMARK=ON \
    -DPLUGIN_ES1BENCHMARK_CLIENT=ON \
    \
    -DPLUGIN_AVS=OFF \
    -DPLUGIN_BACKOFFICE=OFF \
    -DPLUGIN_BLUETOOTH=OFF \
    -DPLUGIN_BLUETOOTHREMOTECONTROL=OFF \
    -DPLUGIN_BLUETOOTHAUDIOSINK=OFF \
    -DPLUGIN_CECCONTROL=OFF \
    -DPLUGIN_COBALT=OFF \
    -DPLUGIN_COMMANDER=OFF \
    -DPLUGIN_COMPOSITOR=OFF \
    -DPLUGIN_DHCPSERVER=OFF \
    -DPLUGIN_DIALSERVER=OFF \
    -DPLUGIN_DICTIONARY=OFF \
    -DPLUGIN_DOGGO=OFF \
    -DPLUGIN_FILETRANSFER=OFF \
    -DPLUGIN_FIRMWARECONTROL=OFF \
    -DPLUGIN_INPUTSWITCH=OFF \
    -DPLUGIN_IOCONNECTOR=OFF \
    -DPLUGIN_LANGUAGEADMINISTRATOR=OFF \
    -DPLUGIN_NETWORKCONTROL=OFF \
    -DPLUGIN_OUTOFPROCESS=OFF \
    -DPLUGIN_PERFORMANCEMONITOR=OFF \
    -DPLUGIN_POWER=OFF \
    -DPLUGIN_PROCESSMONITOR=OFF \
    -DPLUGIN_REMOTECONTROL=OFF \
    -DPLUGIN_RESOURCEMONITOR=OFF \
    -DPLUGIN_RUSTBRIDGE=OFF \
    -DPLUGIN_SECURESHELLSERVER=OFF \
    -DPLUGIN_SNAPSHOT=OFF \
    -DPLUGIN_SPARK=OFF \
    -DPLUGIN_STREAMER=OFF \
    -DPLUGIN_SUBSYSTEMCONTROLLER=OFF \
    -DPLUGIN_SVALBARD=OFF \
    -DPLUGIN_SWITCHBOARD=OFF \
    -DPLUGIN_SYSTEMCOMMANDS=OFF \
    -DPLUGIN_TIMESYNC=OFF \
    -DPLUGIN_VOLUMECONTROL=OFF \
    -DPLUGIN_WEBPA=OFF \
    -DPLUGIN_WEBPROXY=OFF \
    -DPLUGIN_WEBSERVER=OFF \
    -DPLUGIN_WEBSHELL=OFF \
    -DPLUGIN_WIFICONTROL=OFF \
"

# --------------------------------------------------------------------------
# Installed files
#   plugin .so  → /usr/lib/wpeframework/plugins/
#   config JSON → /etc/WPEFramework/plugins/
#   client bin  → /usr/bin/
# --------------------------------------------------------------------------
FILES_SOLIBSDEV = ""

FILES:${PN} += " \
    ${libdir}/wpeframework/plugins/libWPEFrameworkES1Benchmark.so \
    ${sysconfdir}/WPEFramework/plugins/ES1Benchmark.json \
    ${bindir}/ES1BenchmarkClient \
"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
