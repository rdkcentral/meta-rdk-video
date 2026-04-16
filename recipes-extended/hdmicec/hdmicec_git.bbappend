do_install:append() {
    if [ -e "${D}${libdir}/libRCEC.so" ] && [ ! -e "${D}${libdir}/libRCECHal.so" ]; then
        ln -sf libRCEC.so ${D}${libdir}/libRCECHal.so
    fi
}

FILES:${PN}:append = " ${libdir}/libRCECHal.so"

