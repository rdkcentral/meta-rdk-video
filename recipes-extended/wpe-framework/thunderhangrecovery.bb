# Recipe metadata
SUMMARY = "Thunder Hang Recovery Application"
DESCRIPTION = "Application to recovery thunder when it hangs by restarting thunder wpeframework"

# The source file to compile
SRC_URI = "file://thunderHangRecovery.cpp \
           file://thunderHangRecovery.service \
          "

inherit systemd

DEPENDS = "rbus curl cjson"
RDEPENDS:${PN} = "rbus curl cjson"
LDFLAGS = " -lrbus -lcurl -lcjson"
CXXFLAGS = " -I${includedir}/rbus -I${includedir}/curl -I=${includedir}/cjson"

# Define build and install steps
do_compile:append() {
    ${CXX} ${CXXFLAGS} ${LDFLAGS} -o ${WORKDIR}/thunderHangRecovery ${WORKDIR}/thunderHangRecovery.cpp
}

do_install:append() {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/thunderHangRecovery ${D}${bindir}
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/thunderHangRecovery.service ${D}${systemd_unitdir}/system
}

SYSTEMD_SERVICE:${PN} += "thunderHangRecovery.service"

# Specify where to install the executable
FILES:${PN} = "${bindir}/thunderHangRecovery"
FILES:${PN} += "${systemd_unitdir}/system/thunderHangRecovery.service"
