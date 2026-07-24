FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

DEPENDS:remove = "aamp fog"
DEPENDS:append = " nettle"

# Install stub fogiarm.h into sysroot (fog package not available)
do_configure:prepend() {
    install -d ${STAGING_INCDIR}
    install -m 0644 ${WORKDIR}/fogiarm.h ${STAGING_INCDIR}/fogiarm.h
}

SRC_URI:append = " file://fogiarm.h"

CXXFLAGS:append = " -Wno-error=switch"
