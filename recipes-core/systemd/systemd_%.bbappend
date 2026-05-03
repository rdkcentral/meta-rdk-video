FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://90-coredump.conf"

do_install:append() {
    install -d ${D}${sysconfdir}/systemd/system.conf.d
    install -m 0644 ${WORKDIR}/90-coredump.conf \
        ${D}${sysconfdir}/systemd/system.conf.d/90-coredump.conf
}