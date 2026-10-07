SUMMARY = "ES1 asynchronous JSON-RPC benchmark plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=be650d9617f9f9d24bcaccf78a97b28b"

PV = "1.0.0"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "git://github.com/workkavint-ship-it/JsonRPC_Benchmark_Async;protocol=https;branch=main"
SRCREV = "06bfe17141defaf957e278a0c1ae5cbffdcb6424"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

DEPENDS += "wpeframework wpeframework-tools-native entservices-apis"
RDEPENDS:${PN} += "wpeframework"

PLUGIN_ES1BENCHMARKASYNC_AUTOSTART ?= "true"
PLUGIN_ES1BENCHMARKASYNC_MODE ?= "Local"

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DPLUGIN_ES1BENCHMARKASYNC=ON \
    -DPLUGIN_ES1BENCHMARKASYNC_AUTOSTART=${PLUGIN_ES1BENCHMARKASYNC_AUTOSTART} \
    -DPLUGIN_ES1BENCHMARKASYNC_MODE=${PLUGIN_ES1BENCHMARKASYNC_MODE} \
    -DPLUGIN_ES1BENCHMARKASYNC_CLIENT=ON \
"

do_install:append() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'es1bench', 'true', 'false', d)} == 'true'; then
        if [ -d "${D}/etc/WPEFramework/plugins" ]; then
            find ${D}/etc/WPEFramework/plugins/ -type f | xargs sed -i -r 's/"autostart"[[:space:]]*:[[:space:]]*true/"autostart":false/g'
        fi
    fi

    install -d ${D}/opt
    install -m 0644 ${S}/client/async-client.config.default ${D}/opt/es1-async.config

    install -d ${D}/opt/es1bench/log
    install -m 0644 /dev/null ${D}/opt/es1bench/log/.keep
}

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${datadir}/WPEFramework/* ${bindir}/es1asyncclient /opt/es1-async.config /opt/es1bench/log /opt/es1bench/log/.keep"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"