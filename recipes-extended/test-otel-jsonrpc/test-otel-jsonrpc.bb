SUMMARY = "OTEL Thunder JSON-RPC Trace Propagation Test"
DESCRIPTION = "Test application to validate OpenTelemetry trace context \
propagation through Thunder JSON-RPC calls. Tests parent-child span \
correlation across multiple invocations."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRC_URI = " \
    file://main.cpp \
    file://CMakeLists.txt \
"

S = "${WORKDIR}"

inherit cmake pkgconfig

DEPENDS = " \
    opentelemetry-cpp \
"

RDEPENDS:${PN} = " \
    rdk-otel-collector \
    curl \
"

# Ensure Thunder and rdk_otlp headers/libs are found
EXTRA_OECMAKE = ""

FILES:${PN} += "${bindir}/otel-jsonrpc-test"
