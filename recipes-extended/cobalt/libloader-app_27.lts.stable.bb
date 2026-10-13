SUMMARY = "Evergreen Cobalt loader_app built as a library."

require cobalt_27.inc

do_compile:append() {
    ninja ${PARALLEL_MAKE} -C ${COBALT_OUT_DIR} libloader_app.so
}

do_install:append() {
    install -d ${D}${libdir}
    install -m 0755 ${COBALT_OUT_DIR}/libloader_app.so ${D}${libdir}
}

FILES:${PN} += "${libdir}/libloader_app.so"
