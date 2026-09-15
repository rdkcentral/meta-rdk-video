SUMMARY = "IARM-Bus set/get traceparent pass-through test"
DESCRIPTION = "Two-process test (publisher + subscriber) validating the \
IARM_Bus_SetTraceparent()/IARM_Bus_GetTraceparent() model built on the \
iarmbus2 tree: IARM only stores and transports an opaque, format-validated \
traceparent string across RPC calls and event broadcasts. IARM itself has \
no dependency on any tracing library - both the caller and the receiver \
integrate with rdk_otlp on their own. \
Run via /usr/bin/run_iarm_tp_test.sh on the target."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRC_URI = " \
    file://iarm_tp_test_pub.c \
    file://iarm_tp_test_sub.c \
    file://CMakeLists.txt \
    file://run_iarm_tp_test.sh \
"

S = "${WORKDIR}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit cmake

# NOTE: points at the iarmbus2 tree (IARM_Bus_SetTraceparent/GetTraceparent),
# not the original iarmbus/librdk_otlp-dlsym based recipe used by test-iarm-otel.
# IARM_Bus_SetTraceparent()/IARM_Bus_GetTraceparent() are no-ops unless the
# iarmbus2 recipe itself is built with EXTRA_OECONF += "--enable-otel-tp".
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
    ${bindir}/iarm_tp_test_pub \
    ${bindir}/iarm_tp_test_sub \
    ${bindir}/run_iarm_tp_test.sh \
"
