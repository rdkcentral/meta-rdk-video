SUMMARY = "HDMI CEC driver interface header for L2HalMock"
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

SRCREV = "f0c27ca6627d00fb3f87d96dfe1624ce00bb0f29"
SRC_URI = "git://github.com/rdkcentral/hdmicec.git;branch=aidl_feature;protocol=https"

S = "${WORKDIR}/git"

do_configure() {
    :
}

do_compile() {
    :
}

do_install() {
    install -d ${D}${includedir}/ccec/drivers
    install -m 0644 ${S}/ccec/drivers/include/ccec/drivers/hdmi_cec_driver.h ${D}${includedir}/ccec/drivers/
}

FILES:${PN} += "${includedir}/ccec/drivers"
