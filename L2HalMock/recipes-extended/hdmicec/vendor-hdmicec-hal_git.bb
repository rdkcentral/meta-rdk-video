SUMMARY = "L2HalMock HDMI CEC vendor HAL provider"
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

SRCREV = "f0c27ca6627d00fb3f87d96dfe1624ce00bb0f29"
SRC_URI = "git://github.com/rdkcentral/hdmicec.git;branch=aidl_feature;protocol=https"

S = "${WORKDIR}/git"

PROVIDES += "virtual/vendor-hdmicec-hal"
RPROVIDES:${PN} += "virtual/vendor-hdmicec-hal"

PACKAGE_ARCH = "${MACHINE_ARCH}"

DEPENDS = "hdmicecheader jsoncpp curl libwebsockets"

inherit pkgconfig

do_configure() {
    :
}

do_compile() {
    oe_runmake library -C ${S}/soc/L2HalMock/common \
        CXX="${CXX}" \
        CXXFLAGS="-std=c++14 -g -fPIC -D_REENTRANT -Wall \
            -I${S}/ccec/drivers/include/ccec/drivers \
            -I${S}/soc/L2HalMock/common \
            $(${PKG_CONFIG} --cflags jsoncpp libcurl libwebsockets)" \
        LDFLAGS="-lpthread -Wl,--no-as-needed \
            $(${PKG_CONFIG} --libs jsoncpp libcurl libwebsockets)"
}

do_install() {
    install -d ${D}${libdir}
    install -m 0755 ${S}/soc/L2HalMock/common/install/lib/libRCECHal.so ${D}${libdir}/libRCECHal.so
}

FILES:${PN} += "${libdir}/libRCECHal.so"
INSANE_SKIP:${PN} += "dev-so"
