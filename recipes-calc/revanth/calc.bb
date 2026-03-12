DESCRIPTION = "This is the mock implementation for calculator app."

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${MANIFEST_PATH_META_RDK}/licenses/Apache-2.0;md5=3b83ef96387f14655fc854ddc3c6bd57"


PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"


SRC_URI = "file://calc_new"

S = "${WORKDIR}/calc_new"


do_compile() {
    oe_runmake
}

#do_install() {
#    install -d ${D}${bindir}
#    install -m 0755 ${S}/main ${D}${bindir}
#    # Install shared library
#    install -d ${D}${libdir}
#    install -m 0644 ${S}/lib/libcalc.so ${D}${libdir}
#}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/main ${D}${bindir}

    install -d ${D}${libdir}

    # install real runtime library
    install -m 0644 ${S}/lib/libcalc.so.1.0.0 ${D}${libdir}

    # recreate symlinks inside ${D}
    ln -sf libcalc.so.1.0.0 ${D}${libdir}/libcalc.so.1
    ln -sf libcalc.so.1 ${D}${libdir}/libcalc.so

    #Including Calculator.service file in the location /etc/systemd/system
    install -d ${D}/etc/systemd/system
    install -m 644 ${S}/Calculator.service ${D}/etc/systemd/system/Calculator.service


    #include instructions file in the /etc/Calculator directory
    install -d ${D}${sysconfdir}/Calculator
    install -m 644 ${S}/instructions.txt ${D}${sysconfdir}/Calculator/instructions.txt
}

FILES:${PN} += "${bindir}/main ${libdir}/libcalc.so.*"
FILES:${PN}-dev += "${libdir}/libcalc.so"
FILES:${PN} += "${sysconfdir}/systemd/system/Calculator.service"
FILES:${PN} += "${sysconfdir}/Calculator/instructions.txt"

inherit systemd
SYSTEMD_SERVICE:${PN} = "Calculator.service"
SYSTEMD_AUTO_ENABLE = "enable"

