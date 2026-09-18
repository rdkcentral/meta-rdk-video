SUMMARY = "HDMI CEC Source stub headers"
DESCRIPTION = "Stub/mock headers for HDMI CEC Source development and testing"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2a944942e1496af1886903d274dedb13"

PV = "1.2.0"
PR = "r0"

S = "${WORKDIR}/git"

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-hdmicecsource;${CMF_GITHUB_SRC_URI_SUFFIX}"

SRCREV = "7c4f0136ffa3ebe90b0bb4e7044080653ea92fa1"

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

    if grep -Eq 'STUB\(ds(Hdmi|HDMI)In|[[:space:]]ds(Hdmi|HDMI)In[A-Za-z0-9_]*[[:space:]]*\(' \
        ${S}/stubs/dshal-stub.cpp; then
        bbfatal "HDMI input must be provided by rdk-halif-aidl, not vdevice-noop"
    fi

    # Keep the HAL no-op ABI equivalent to the previously working C stub recipe.
    ${CC} ${CFLAGS} -fPIC -x c -I${S}/stubs \
        -c ${S}/stubs/dshal-stub.cpp \
        -o ${B}/dshal-stub.o

    # Compile DeviceSettings stub library (from repo stubs/ directory)
    ${CXX} ${CXXFLAGS} -fPIC -shared \
        -I${S}/stubs \
        ${S}/stubs/devicesettings-stub.cpp \
        ${B}/dshal-stub.o \
        -o ${B}/libds.so \
        ${LDFLAGS}

    # Compile DeviceSettings HAL stub library (from repo stubs/ directory)
    ${CC} ${CFLAGS} -fPIC -shared \
        ${B}/dshal-stub.o \
        -o ${B}/libds-hal.so \
        ${LDFLAGS}

    # Create compatibility aliases expected by downstream consumers.
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
    install -m 0755 ${B}/libdshal.so ${D}${libdir}/
    install -m 0755 ${B}/libdshalcli.so ${D}${libdir}/
}

# Allow the package to ship headers and libraries
FILES:${PN} = "${libdir}/libds.so ${libdir}/libds-hal.so ${libdir}/libdshal.so ${libdir}/libdshalcli.so"
FILES:${PN}-dev = "${includedir}/*"
ALLOW_EMPTY:${PN} = "1"

# Skip QA checks not relevant for stub package
INSANE_SKIP:${PN} += "dev-so ldflags"
INSANE_SKIP:${PN}-dev += "dev-elf"
