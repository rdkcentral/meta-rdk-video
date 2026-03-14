SUMMARY = "L2HalMock DeviceSettings HAL provider"
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "fd12a6fd43808086f41f24871fa33b464cbbf4d3"
SRC_URI = "git://git@github.com/rdk-e/devicesettings-emulator.git;branch=L2HalMock;protocol=ssh \
           file://dsVideoHal.patch"

S = "${WORKDIR}/git"

PROVIDES += "virtual/vendor-devicesettings-hal"
RPROVIDES:${PN} += "virtual/vendor-devicesettings-hal"

PACKAGE_ARCH = "${MACHINE_ARCH}"

DEPENDS = "devicesettings-hal-headers"

EXTRA_OEMAKE = 'CXX="${CXX}"'
CFLAGS += " -I${STAGING_INCDIR}/rdk/ds-hal -I${STAGING_INCDIR}/rdk/halif/ds-hal"

do_compile() {
    oe_runmake library
}

do_install() {
    install -d ${D}${libdir}
    install -m 0755 ${S}/libds-hal.so ${D}${libdir}/libds-hal.so.0
    ln -sf libds-hal.so.0 ${D}${libdir}/libds-hal.so
}

FILES:${PN} += "${libdir}/libds-hal.so ${libdir}/libds-hal.so.0"
INSANE_SKIP:${PN} += "dev-so"
