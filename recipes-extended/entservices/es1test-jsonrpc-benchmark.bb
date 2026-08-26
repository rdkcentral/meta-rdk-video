SUMMARY = "ES1 JSON-RPC round-trip benchmark plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=be650d9617f9f9d24bcaccf78a97b28b"

PV = "1.0.0"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "git://github.com/workkavint-ship-it/ES1Test-JSONRPC-Benchmark;protocol=https;branch=main"

SRCREV = "e08be487d18df1a45b3172931eed9601f8160051"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

DEPENDS += "wpeframework wpeframework-tools-native entservices-apis"
RDEPENDS:${PN} += "wpeframework"

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DPLUGIN_ES1BENCHMARK=ON \
    -DPLUGIN_ES1BENCHMARK_AUTOSTART=true \
    -DPLUGIN_ES1BENCHMARK_CLIENT=ON \
"

do_install:append() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'true', 'false', d)} == 'true'; then
        if [ -d "${D}/etc/WPEFramework/plugins" ]; then
            find ${D}/etc/WPEFramework/plugins/ -type f | xargs sed -i -r 's/"autostart"[[:space:]]*:[[:space:]]*true/"autostart":false/g'
        fi
    fi

    # es1client's own config, editable in place on the device without a rebuild.
    # Two separate files: es1bench.service (on-demand, after Thunder) uses
    # /opt/es1.config (mode=warm); es1bench-coldstart.service (before Thunder)
    # uses /opt/es1-coldstart.config (mode=coldstart) - they can't share one
    # file since each unit needs a different mode.
    install -d ${D}/opt
    install -m 0644 ${S}/client/es1.config.default ${D}/opt/es1.config
    install -m 0644 ${S}/client/es1-coldstart.config.default ${D}/opt/es1-coldstart.config

    install -d ${D}${localstatedir}/log/es1bench
}

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${datadir}/WPEFramework/* ${bindir}/es1client /opt/es1.config /opt/es1-coldstart.config ${localstatedir}/log/es1bench"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
