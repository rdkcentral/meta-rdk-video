FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://50-coredump.conf"

do_install:append() {
    install -d ${D}${sysconfdir}/sysctl.d
    install -m 0644 ${WORKDIR}/50-coredump.conf \
        ${D}${sysconfdir}/sysctl.d/50-coredump.conf
}