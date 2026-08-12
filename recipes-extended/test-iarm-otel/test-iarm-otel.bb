SUMMARY = "IARM-Bus OpenTelemetry Context Propagation POC Test"
DESCRIPTION = "Two-process test (publisher + subscriber) that validates \
automatic W3C traceparent injection and child-span creation across IARM \
event broadcasts and RPC calls.  Uses direct linking against librdk_otlp.so. \
Run via /usr/bin/run_iarm_otel_test.sh on the target."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRC_URI = " \
    file://iarm_otel_test_pub.c \
    file://iarm_otel_test_sub.c \
    file://CMakeLists.txt \
    file://run_iarm_otel_test.sh \
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

EXTRA_OECMAKE = " \
    -DIARM_INCLUDE_DIR=${STAGING_INCDIR} \
    -DOTEL_INSTRUMENTATION_DIR=${STAGING_INCDIR}/rdk_otlp \
"

FILES:${PN} += " \
    ${bindir}/iarm_otel_test_pub \
    ${bindir}/iarm_otel_test_sub \
    ${bindir}/run_iarm_otel_test.sh \
"
