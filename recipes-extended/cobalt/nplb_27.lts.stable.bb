SUMMARY = "Starboard NPLB."

BREAKPAD_BIN:append = " libnplb* elf_loader_sandbox"

COBALT_BUILD_TYPE = "devel"
COBALT_APP_DIR = "/content/data/app/nplb"
libdir = "${datadir}${COBALT_APP_DIR}/lib"

require cobalt_27.inc

do_compile[network] = "1"
do_compile() {
    do_setup_ca_certs_env
    export NINJA_STATUS='%p '
    ninja ${PARALLEL_MAKE} -C ${COBALT_OUT_DIR} nplb elf_loader_sandbox
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${COBALT_OUT_DIR}/elf_loader_sandbox ${D}${bindir}

    install -d ${D}${libdir}
    install -m 0755 ${COBALT_OUT_DIR}/libnplb.so ${D}${libdir}

    install -d ${D}${datadir}${COBALT_APP_DIR}/content
    cp -av --no-preserve=ownership ${COBALT_OUT_DIR}/ssl ${D}${datadir}${COBALT_APP_DIR}/content/
    cp -av --no-preserve=ownership ${COBALT_OUT_DIR}/test ${D}${datadir}${COBALT_APP_DIR}/content/
}

FILES:${PN}  = "${libdir}/libnplb.so"
FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"

PACKAGES =+ "${PN}-test-data"
PROVIDES += "${PN}-test-data"
FILES:${PN}-test-data = "${datadir}${COBALT_APP_DIR}/content/*"

PACKAGES =+ "${PN}-tools"
PROVIDES += "${PN}-tools"
FILES:${PN}-tools  = "${bindir}/elf_loader_sandbox"
