SUMMARY = "This recipe compiles and installs xdial component."
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d7a8c87b0741f248c5139ca80a783231"

S = "${WORKDIR}/git"

PV = "1.0.1"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRCREV = "ad73b443e800b541b2c52a8efe8ee35c597d30bc"
SRC_URI = "${CMF_GITHUB_ROOT}/xdialserver;${CMF_GITHUB_SRC_URI_SUFFIX}"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

EXTRA_OEMAKE+= "PLATFORM_FLAGS="-DPLATFORM=-DNETFLIX_CALLSIGN_0=1""

# Enable DISABLE_SECURITY_TOKEN
EXTRA_OEMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'wpe_security_util_disable', '"DISABLE_SECURITY_TOKEN="-DDISABLE_SECURITY_TOKEN=1""', ' ', d)}"

#libsoup3 not compatible with gdial
#DEPENDS:append =  " ${@bb.utils.contains('DISTRO_FEATURES', 'enable_libsoup3', ' libsoup ', ' libsoup-2.4 ', d)}"
DEPENDS:append = " libsoup-2.4"
DEPENDS:append = " gssdp"
DEPENDS:append = " cmake-native"
EXTRANATIVEPATH += "cmake-native"

CFLAGS += "-fcommon"


DEPENDS += "gssdp openssl c-ares curl util-linux glib-2.0 cmake-native wpeframework wpeframework-clientlibraries entservices-apis iarmmgrs"

inherit logrotate_config pkgconfig

LOGROTATE_NAME="xdial"
LOGROTATE_LOGNAME_xdial="xdial.log"
LOGROTATE_SIZE_xdial="1572864"
LOGROTATE_ROTATION_xdial="3"
LOGROTATE_SIZE_MEM_xdial="1572864"
LOGROTATE_ROTATION_MEM_xdial="3"

do_compile () {
    oe_runmake -C ${S}
}

do_install () {
    install -d ${D}${libdir}
    install -d ${D}${includedir}

    install -m0755 ${S}/server/include/gdialservice.h ${D}${includedir}/
    install -m0755 ${S}/server/include/gdialservicecommon.h ${D}${includedir}/

#   GDIAL
    install -m0755 ${S}/server/libgdial-server.so ${D}${libdir}/libgdial-server.so
    ln -s libgdial-server.so ${D}${libdir}/libgdial-server.so.0
    install -m0755 ${S}/server/plat/libgdial-plat.so ${D}${libdir}/libgdial-plat.so
    ln -s libgdial-plat.so ${D}${libdir}/libgdial-plat.so.0
}

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*"
TARGET_CC_ARCH += "${LDFLAGS}"
INSANE_SKIP:${PN}-dev += "dev-deps rpaths dev-elf"
INSANE_SKIP:${PN} += "rpaths"
