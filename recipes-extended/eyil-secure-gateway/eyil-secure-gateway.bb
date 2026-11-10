# Copyright 2026 RDK Management
# SPDX-License-Identifier: Apache-2.0

SUMMARY = "Eyil secure Gateway and independent C client library"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

PV = "1.0.0"
PR = "r0"
SRCREV = "4311278b537da72b1e7422135ef5e30dab281aa1"
SRC_URI = "${CMF_GITHUB_ROOT}/eyil-secure-gateway;protocol=${CMF_GITHUB_PROTOCOL};branch=topic/RDKEMW-27976"

S = "${WORKDIR}/git"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"

inherit cmake pkgconfig python3native

DEPENDS = "wpeframework wpeframework-tools-native entservices-apis protobuf protobuf-native zeromq"

EYILGATEWAY_CRYPTO_CALLSIGN ?= ""
EYILGATEWAY_WORKERS ?= "4"
PACKAGECONFIG ??= "${@bb.utils.contains('DISTRO_FEATURES', 'debug-variant', 'dev-tools', '', d)}"
PACKAGECONFIG[dev-tools] = "-DBUILD_EYIL_DEV_TOOLS=ON,-DBUILD_EYIL_DEV_TOOLS=OFF"

EXTRA_OECMAKE += " \
    -DPLUGIN_EYILGATEWAY=ON \
    -DBUILD_EYILCLIENT=ON \
    -DBUILD_TESTING=OFF \
    -DENABLE_COVERAGE=OFF \
    -DPLUGIN_EYILGATEWAY_MODE=Off \
    -DPLUGIN_EYILGATEWAY_AUTOSTART=false \
    -DPLUGIN_EYILGATEWAY_CRYPTO_CALLSIGN=${EYILGATEWAY_CRYPTO_CALLSIGN} \
    -DPLUGIN_EYILGATEWAY_WORKERS=${EYILGATEWAY_WORKERS} \
    -DEYIL_CONFIGURATION_INCLUDE_DIR=${STAGING_INCDIR}/WPEFramework \
    -DEYIL_CRYPTO_INCLUDE_DIR=${STAGING_INCDIR}/WPEFramework \
    -DProtobuf_PROTOC_EXECUTABLE=${STAGING_BINDIR_NATIVE}/protoc \
    -DPython3_EXECUTABLE=${PYTHON} \
    -DEYIL_DEV_SYSROOT=${RECIPE_SYSROOT} \
    -DCMAKE_READELF=${READELF} \
"

# Fail the build against stock headers or a library lacking the draft monitor
# symbol. The runtime additionally verifies the patch's metadata and behavior.
do_configure:prepend() {
    cat > ${T}/eyil-zmq-check.c <<'EOF'
#define ZMQ_BUILD_DRAFT_API
#include <zmq.h>
#if ZMQ_VERSION != ZMQ_MAKE_VERSION(4, 3, 4)
#error Eyil requires the validated libzmq 4.3.4 build
#endif
#if !defined(ZMQ_ROUTER_EXPECTED_CONNECTION_ID) || ZMQ_ROUTER_EXPECTED_CONNECTION_ID != 10001
#error Eyil requires zmq_connection_lifetime.patch
#endif
#ifndef ZMQ_MSG_PROPERTY_TRANSPORT_FD
#error Missing trusted transport FD metadata
#endif
int main(void) { return zmq_socket_monitor_versioned(0, 0, 0, 2, ZMQ_PAIR); }
EOF
    ${CC} ${CPPFLAGS} ${CFLAGS} ${T}/eyil-zmq-check.c ${LDFLAGS} -lzmq -o ${T}/eyil-zmq-check
}

PACKAGE_BEFORE_PN += "${PN}-tools ${PN}-client ${PN}-protocol"
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${sysconfdir}/WPEFramework/plugins/EyilGateway.json"
FILES:${PN}-client = "${libdir}/libeyilclient.so.*"
FILES:${PN}-protocol = "${libdir}/libeyilprotocol.so.*"
FILES:${PN}-tools = "${bindir}/eyil-client ${libdir}/eyil"
# Consumer build metadata belongs to the SDK/development package, not runtime.
FILES:${PN}-dev += "${includedir}/eyilclient/EyilClient.h \
                    ${libdir}/cmake/EyilClient ${libdir}/pkgconfig/eyilclient.pc"
RDEPENDS:${PN} += "wpeframework entservices-apis ${PN}-client"
RDEPENDS:${PN}:append = "${@bb.utils.contains('PACKAGECONFIG', 'dev-tools', ' ${PN}-tools', '', d)}"
RDEPENDS:${PN}-tools += "${PN}-client ${PN}-protocol"
INSANE_SKIP:${PN} += "dev-so"

# Dependencies copied from the target sysroot may already be stripped by their
# owning recipes. Exclude only those private, pre-stripped copies from another
# debug split. Keep normal QA/stripping for Eyil's own libraries and executable.
PACKAGE_PREPROCESS_FUNCS += "eyil_skip_stripped_bundle_libraries"
python eyil_skip_stripped_bundle_libraries() {
    import os
    import oe.package

    directory = d.expand('${PKGD}${libdir}/eyil')
    if not os.path.isdir(directory):
        return
    for name in sorted(os.listdir(directory)):
        if '.so' not in name or name.startswith(('libeyilclient.so', 'libeyilprotocol.so')):
            continue
        path = os.path.join(directory, name)
        if not os.path.isfile(path) or os.path.islink(path):
            continue
        _, elf_type = oe.package.is_elf(path)
        if elf_type & 1 and elf_type & 2:
            d.appendVar('INHIBIT_PACKAGE_STRIP_FILES', ' ' + path)
}

# The container bundle contains private copies of its runtime dependencies.
# They must not replace the rootfs libraries as shlib providers.
python populate_packages:prepend() {
    import os
    directory = d.expand('${D}${libdir}/eyil')
    if os.path.isdir(directory):
        libraries = ' '.join(sorted(name for name in os.listdir(directory) if '.so' in name))
        d.setVar('PRIVATE_LIBS:' + d.getVar('PN') + '-tools', libraries)
}
