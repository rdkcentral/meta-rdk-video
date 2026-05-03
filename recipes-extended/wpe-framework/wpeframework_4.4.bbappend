FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://10-corelimit.conf"

FILES:${PN} += "${systemd_unitdir}/system/wpeframework.service.d/10-corelimit.conf"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system/wpeframework.service.d
    install -m 0644 ${WORKDIR}/10-corelimit.conf \
        ${D}${systemd_unitdir}/system/wpeframework.service.d/10-corelimit.conf
}
