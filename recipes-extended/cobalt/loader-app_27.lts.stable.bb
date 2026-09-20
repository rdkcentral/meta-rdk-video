SUMMARY = "Evergreen Cobalt loader_app built as an executable."

require cobalt_27.inc

GN_ARGS_EXTRA:append = " starboard_level_final_executable_type="executable""
BREAKPAD_BIN:append = " loader_app"

do_compile:append() {
    ninja ${PARALLEL_MAKE} -C ${COBALT_OUT_DIR} loader_app
}

do_install:append() {
    install -d ${D}${bindir}
    install -m 0755 ${COBALT_OUT_DIR}/loader_app ${D}${bindir}
}

FILES:${PN} += "${bindir}/loader_app"

