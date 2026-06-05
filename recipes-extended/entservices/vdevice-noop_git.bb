SUMMARY = "HDMI CEC Source stub headers"
DESCRIPTION = "Stub/mock headers for HDMI CEC Source development and testing"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2a944942e1496af1886903d274dedb13"

PV = "1.2.0"
PR = "r0"

S = "${WORKDIR}/git"

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-hdmicecsource;${CMF_GITHUB_SRC_URI_SUFFIX}"

# Release version - 1.2.0
SRCREV = "57388d5b829f5a7e8ca3f420e286dd31357fafaa"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

# This is a header-only package, no compilation needed
do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    # Install stub headers from repository
    install -d ${D}${includedir}/hdmicecsource/stubs
    install -m 0644 ${S}/stubs/*.h ${D}${includedir}/hdmicecsource/stubs/
    install -m 0644 ${S}/stubs/*.hpp ${D}${includedir}/hdmicecsource/stubs/
    
    # Create/install DeviceSettings (DS) headers expected by FindDS.cmake
    install -d ${D}${includedir}/rdk/ds
    install -d ${D}${includedir}/rdk/halif/ds-hal
    install -d ${D}${includedir}/rdk/ds-rpc
    install -d ${D}${includedir}/rdk/iarmmgrs/receiver

    install -m 0644 ${S}/stubs/manager.hpp ${D}${includedir}/rdk/ds/
    install -m 0644 ${S}/stubs/dsTypes.h ${D}${includedir}/rdk/halif/ds-hal/
    install -m 0644 ${S}/stubs/dsMgr.h ${D}${includedir}/rdk/ds-rpc/
    install -m 0644 ${S}/stubs/receiverMgr.h ${D}${includedir}/rdk/iarmmgrs/receiver/
                                       

    # HdmiCecSourceImplementation includes these directly
    install -m 0644 ${S}/stubs/host.hpp ${D}${includedir}/rdk/ds/
    install -m 0644 ${S}/stubs/videoOutputPort.hpp ${D}${includedir}/rdk/ds/
    install -m 0644 ${S}/stubs/dsDisplay.h ${D}${includedir}/rdk/ds/
    
    # Create empty stub libraries
    install -d ${D}${libdir}
    touch ${D}${libdir}/libds.so
    touch ${D}${libdir}/libdshalcli.so
    touch ${D}${libdir}/libds-hal.so
}

# Allow the package to ship headers and libraries
FILES:${PN} = "${libdir}/*.so"
FILES:${PN}-dev = "${includedir}/*"
ALLOW_EMPTY:${PN} = "1"

# Skip QA checks not relevant for stub package
INSANE_SKIP:${PN} += "dev-so ldflags"
INSANE_SKIP:${PN}-dev += "dev-elf"

