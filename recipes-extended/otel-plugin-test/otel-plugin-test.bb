SUMMARY = "OTEL Thunder JSON-RPC Trace Propagation Plugin Test"
DESCRIPTION = "A Thunder plugin that tests OpenTelemetry trace context \
propagation through in-process JSON-RPC Invoke() calls. \
Validates that a plugin-to-plugin call using JSONRPC::SmartLinkType \
correctly propagates traceparent to the downstream handler."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRC_URI = " \
    file://OtelPluginTest.cpp \
    file://CMakeLists.txt \
    file://OtelPluginTest.json \
"

S = "${WORKDIR}"

inherit cmake
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

DEPENDS = " \
    wpeframework \
    wpeframework-tools-native \
    opentelemetry-cpp \
"

RDEPENDS:${PN} = " \
    wpeframework \
    rdk-otel-collector \
"

EXTRA_OECMAKE = " \
    -DNAMESPACE=WPEFramework \
    -DCMAKE_INSTALL_SYSCONFDIR=${sysconfdir} \
"

FILES:${PN} += " \
    ${libdir}/wpeframework/plugins/libWPEFrameworkOtelPluginTest.so \
    ${sysconfdir}/WPEFramework/plugins/OtelPluginTest.json \
"
