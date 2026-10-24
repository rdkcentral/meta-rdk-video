SUMMARY = "HDMI CEC Source stub headers"
DESCRIPTION = "Stub/mock headers for HDMI CEC Source development and testing"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2a944942e1496af1886903d274dedb13"

PV = "1.2.0"
PR = "r0"

S = "${WORKDIR}/git"

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-hdmicecsource;${CMF_GITHUB_SRC_URI_SUFFIX}"

SRCREV = "da5f0ac650d669ad2d43a152f4a8fe283e920517"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

PROVIDES += "virtual/vendor-devicesettings-hal"
RPROVIDES:${PN} += "virtual/vendor-devicesettings-hal"

# Need C++ compiler for stub implementations

# Skip configure, we'll compile stubs directly
do_configure[noexec] = "1"

do_compile() {
    for header in \
        dsAudio.h \
        dsAVDTypes.h \
        dsCompositeIn.h \
        dsCompositeInTypes.h \
        dsDisplay.h \
        dsError.h \
        dsFPD.h \
        dsFPDTypes.h \
        dsHdmiIn.h \
        dsHdmiInTypes.h \
        dsHost.h \
        dsTypes.h \
        dsUtl.h \
        dsVideoDevice.h \
        dsVideoDeviceTypes.h \
        dsVideoPort.h; do
        if [ ! -s "${S}/stubs/${header}" ]; then
            bbfatal "Incomplete vdevice-noop source revision: missing stubs/${header}"
        fi
    done

    grep -q "dsCompositeInConnectCB_t" ${S}/stubs/dsCompositeIn.h || \
        bbfatal "Incomplete dsCompositeIn.h in vdevice-noop source revision"
    grep -q "dsAudioSADList_t" ${S}/stubs/dsAVDTypes.h || \
        bbfatal "Incomplete dsAVDTypes.h in vdevice-noop source revision"
    grep -q "dsVideoFormatUpdateCB_t" ${S}/stubs/dsVideoPort.h || \
        bbfatal "Incomplete dsVideoPort.h in vdevice-noop source revision"

    ${CC} ${CFLAGS} -fPIC -shared \
        -x c -I${S}/stubs \
        ${S}/stubs/dshal-stub.cpp \
        -o ${B}/libds-hal.so \
        -Wl,-z,nodelete \
        ${LDFLAGS}

    # All four names were the same binary in the known-good package.
    cp ${B}/libds-hal.so ${B}/libds.so
    cp ${B}/libds-hal.so ${B}/libdshal.so
    cp ${B}/libds-hal.so ${B}/libdshalcli.so
}

do_install() {
    # Install stub headers from repository
    install -d ${D}${includedir}/hdmicecsource/stubs
    install -m 0644 ${S}/stubs/*.h ${D}${includedir}/hdmicecsource/stubs/
    install -m 0644 ${S}/stubs/*.hpp ${D}${includedir}/hdmicecsource/stubs/

    # Install DeviceSettings stub headers to expected paths
    install -d ${D}${includedir}/rdk/ds
    install -m 0644 ${S}/stubs/manager.hpp ${D}${includedir}/rdk/ds/
    install -m 0644 ${S}/stubs/host.hpp ${D}${includedir}/rdk/ds/
    install -m 0644 ${S}/stubs/videoOutputPort.hpp ${D}${includedir}/rdk/ds/
    install -m 0644 ${S}/stubs/exception.hpp ${D}${includedir}/rdk/ds/
    install -m 0644 ${S}/stubs/hdmiIn.hpp ${D}${includedir}/rdk/ds/
    install -m 0644 ${S}/stubs/dsError.h ${D}${includedir}/rdk/ds/

    install -d ${D}${includedir}/rdk/halif/ds-hal
    install -m 0644 \
        ${S}/stubs/dsAudio.h \
        ${S}/stubs/dsAVDTypes.h \
        ${S}/stubs/dsCompositeIn.h \
        ${S}/stubs/dsCompositeInTypes.h \
        ${S}/stubs/dsDisplay.h \
        ${S}/stubs/dsError.h \
        ${S}/stubs/dsFPD.h \
        ${S}/stubs/dsFPDTypes.h \
        ${S}/stubs/dsHdmiIn.h \
        ${S}/stubs/dsHdmiInTypes.h \
        ${S}/stubs/dsHost.h \
        ${S}/stubs/dsTypes.h \
        ${S}/stubs/dsUtl.h \
        ${S}/stubs/dsVideoDevice.h \
        ${S}/stubs/dsVideoDeviceTypes.h \
        ${S}/stubs/dsVideoPort.h \
        ${D}${includedir}/rdk/halif/ds-hal/

    install -d ${D}${includedir}/rdk/ds-rpc
    install -m 0644 ${S}/stubs/dsMgr.h ${D}${includedir}/rdk/ds-rpc/

    # Install IARMBUS receiver stub header
    install -d ${D}${includedir}/rdk/iarmmgrs/receiver
    install -m 0644 ${S}/stubs/receiverMgr.h ${D}${includedir}/rdk/iarmmgrs/receiver/

    # Install compiled stub libraries
    install -d ${D}${libdir}
    install -m 0755 ${B}/libds.so ${D}${libdir}/
    install -m 0755 ${B}/libds-hal.so ${D}${libdir}/
    ln -sf libds-hal.so ${D}${libdir}/libds-hal.so.0
    install -m 0755 ${B}/libdshal.so ${D}${libdir}/
    install -m 0755 ${B}/libdshalcli.so ${D}${libdir}/
}

# Allow the package to ship headers and libraries
FILES:${PN} = "${libdir}/libds.so ${libdir}/libds-hal.so ${libdir}/libds-hal.so.0 ${libdir}/libdshal.so ${libdir}/libdshalcli.so"
FILES:${PN}-dev = "${includedir}/*"
ALLOW_EMPTY:${PN} = "1"

# Skip QA checks not relevant for stub package
INSANE_SKIP:${PN} += "dev-so ldflags"
INSANE_SKIP:${PN}-dev += "dev-elf"
