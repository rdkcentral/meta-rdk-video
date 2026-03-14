# Standalone L2HalMock build does not include the full telemetry stack.
DEPENDS:remove = "telemetry"
RDEPENDS:${PN}:remove = "telemetry"
# L2HalMock uses the AIDL-enabled hdmicec implementation.
PR:append = ".l2halmock1"
SRCREV_hdmicec = "f0c27ca6627d00fb3f87d96dfe1624ce00bb0f29"
SRC_URI = "${CMF_GITHUB_ROOT}/hdmicec;protocol=${CMF_GIT_PROTOCOL};branch=aidl_feature;name=hdmicec"
