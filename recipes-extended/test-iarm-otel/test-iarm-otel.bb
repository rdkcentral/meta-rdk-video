SUMMARY = "IARM-Bus OpenTelemetry Context Propagation POC Test"
DESCRIPTION = "Two-process test (publisher + subscriber) that validates \
automatic W3C traceparent injection and handler-controlled child-span creation across IARM \
event broadcasts and RPC calls. Uses dlsym-based optional tracing hooks in libIARMBus. \
Run via /usr/bin/run_iarm_otel_test.sh on the target."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRC_URI = " \
    file://iarm_otel_test_pub.c \
    file://iarm_otel_test_sub.c \
    file://iarm_otel_test2_pub.c \
    file://iarm_otel_test2_sub.c \
    file://CMakeLists.txt \
    file://run_iarm_otel_test.sh \
    file://run_iarm_otel_test2.sh \
"

S = "${WORKDIR}"

inherit cmake

DEPENDS = " \
    iarmbus \
    opentelemetry-cpp \
"

RDEPENDS:${PN} = " \
    iarmbus \
    rdk-otel-collector \
"

EXTRA_OECMAKE = ""

FILES:${PN} += " \
    ${bindir}/iarm_otel_test_pub \
    ${bindir}/iarm_otel_test_sub \
    ${bindir}/iarm_otel_test2_pub \
    ${bindir}/iarm_otel_test2_sub \
    ${bindir}/run_iarm_otel_test.sh \
    ${bindir}/run_iarm_otel_test2.sh \
"
