SUMMARY = "HDMI CEC Source stub headers"
DESCRIPTION = "Stub/mock headers for HDMI CEC Source development and testing"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2a944942e1496af1886903d274dedb13"

PV = "1.2.0"
PR = "r0"

S = "${WORKDIR}/git"

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-hdmicecsource;${CMF_GITHUB_SRC_URI_SUFFIX}"

SRCREV = "6c254285cc33bf255d2babb03a5f0189087cb3a2"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

# Need C++ compiler for stub implementations

# Skip configure, we'll compile stubs directly
do_configure[noexec] = "1"

do_compile() {
    # Compile DeviceSettings stub library (from repo stubs/ directory)
    ${CXX} ${CXXFLAGS} -fPIC -shared \
        -I${S}/stubs \
        ${S}/stubs/devicesettings-stub.cpp \
        -o ${B}/libds.so \
        ${LDFLAGS}
    
    # Compile DeviceSettings HAL stub library (from repo stubs/ directory)
    ${CXX} ${CFLAGS} -fPIC -shared \
        -I${S}/stubs \
        ${S}/stubs/dshal-stub.cpp \
        -o ${B}/libds-hal.so \
        ${LDFLAGS}
    
    # Create dshalcli as a copy of ds-hal (often they're linked together)
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
    install -m 0644 ${S}/stubs/dsTypes.h ${D}${includedir}/rdk/halif/ds-hal/
    install -m 0644 ${S}/stubs/dsDisplay.h ${D}${includedir}/rdk/halif/ds-hal/
    
    install -d ${D}${includedir}/rdk/ds-rpc
    install -m 0644 ${S}/stubs/dsMgr.h ${D}${includedir}/rdk/ds-rpc/
    
    # Install IARMBUS receiver stub header
    install -d ${D}${includedir}/rdk/iarmmgrs/receiver
    install -m 0644 ${S}/stubs/receiverMgr.h ${D}${includedir}/rdk/iarmmgrs/receiver/
    
    # Install compiled stub libraries
    install -d ${D}${libdir}
    install -m 0755 ${B}/libds.so ${D}${libdir}/
    install -m 0755 ${B}/libds-hal.so ${D}${libdir}/
    install -m 0755 ${B}/libdshalcli.so ${D}${libdir}/
}

# Allow the package to ship headers and libraries
FILES:${PN} = "${libdir}/*.so"
FILES:${PN}-dev = "${includedir}/*"
ALLOW_EMPTY:${PN} = "1"

# Skip QA checks not relevant for stub package
INSANE_SKIP:${PN} += "dev-so ldflags"
INSANE_SKIP:${PN}-dev += "dev-elf"

