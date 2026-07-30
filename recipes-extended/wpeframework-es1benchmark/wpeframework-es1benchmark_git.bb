SUMMARY = "ES1 JSON-RPC round-trip benchmark plugin (ThunderNanoServices)"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5af0d167017273075d866c98a6159659"

# --------------------------------------------------------------------------
# Version — bump PR when recipe changes, bump PV + SRCREV when source changes
# --------------------------------------------------------------------------
PV = "1.0.0"
PR = "r0"

# --------------------------------------------------------------------------
# Source — ThunderNanoServices, branch dev/es1benchmark
# Update SRCREV when new commits are added to the branch.
# --------------------------------------------------------------------------
SRC_URI = "git://github.com/rdkcentral/ThunderNanoServices.git;protocol=https;branch=dev/es1benchmark"

# Pinned to the single commit that adds ES1Benchmark (branched from R4_4 @ 81776f5b)
SRCREV = "e7178ea91d03a42545b45cf5d8dbb8893f042566"

S = "${WORKDIR}/git"

# --------------------------------------------------------------------------
# Build
# --------------------------------------------------------------------------
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"

inherit cmake pkgconfig python3native

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
# Activation — make autostart:true in the generated plugin JSON so WPEFramework
# loads ES1Benchmark automatically on boot.
# If the distro uses thunder_startup_services (a separate startup manager that
# activates plugins), flip autostart to false to avoid double-activation.
# --------------------------------------------------------------------------
do_install:append() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'true', 'false', d)} == 'true'; then
        if [ -d "${D}${sysconfdir}/WPEFramework/plugins" ]; then
            find ${D}${sysconfdir}/WPEFramework/plugins/ -name "ES1Benchmark.json" \
                | xargs sed -i -r 's/"autostart"[[:space:]]*:[[:space:]]*true/"autostart":false/g'
        fi
    fi
}

# --------------------------------------------------------------------------
# Installed files
#   plugin .so  → /usr/lib/wpeframework/plugins/
#   config JSON → /etc/WPEFramework/plugins/  (write_config() installs here)
#   client bin  → /usr/bin/
# --------------------------------------------------------------------------
#Restrict debian package renaming
DEBIAN_NOAUTONAME:${PN} = "1"
DEBIAN_NOAUTONAME:${PN}-dev = "1"
DEBIAN_NOAUTONAME:${PN}-dbg = "1"

FILES_SOLIBSDEV = ""

FILES:${PN} += "${bindir}/* "
FILES:${PN} += "${libdir}/* "
FILES:${PN} += "${sysconfdir}/* "
FILES:${PN} += "${datadir}/WPEFramework/* "

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
