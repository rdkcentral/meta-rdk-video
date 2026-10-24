DEPENDS:append:x86-64 = " rdk-halif-aidl libbinder"

# Force the x86/vdevice build to fetch the AIDL-based hdmicec source instead of the
# legacy recipe-pinned SHA from meta-rdk-video.
SRCREV_hdmicec:x86-64 = "e36b4909b25dda88eef12091c03d8c54b5b5fd8b"

# Add include paths for AIDL-generated HAL headers and binder headers
CFLAGS:append:x86-64 = " -I${STAGING_INCDIR}/com/rdk/hal/hdmicec -I${STAGING_INCDIR}/binder -I${STAGING_INCDIR}/android"
CXXFLAGS:append:x86-64 = " -I${STAGING_INCDIR}/com/rdk/hal/hdmicec -I${STAGING_INCDIR}/binder -I${STAGING_INCDIR}/android"

# The aidl_feature Makefile.am does not compile the AIDL-generated stub .cpp files
# that rdk-halif-aidl installs to ${STAGING_INCDIR}/com/rdk/hal/hdmicec/.
# Those files define the typeinfo/vtable symbols (IHdmiCecEventListener, BnHdmiCecEventListener,
# IHdmiCec::serviceName, etc.) that libRCEC.so requires at runtime.
# Fix: compile them into a static archive and inject into libRCEC_la_LIBADD.

do_configure:append:x86-64() {
    # Patch the generated Makefile to:
    #  1. link the AIDL stubs archive into libRCEC.so so typeinfo symbols are defined
    #  2. add -lbinder so android::BBinder/android::BpBinder typeinfo is resolved at
    #     runtime from libbinder.so (which rdk-halif-aidl installs)
    sed -i \
      's|libRCEC_la_LIBADD = .*libRCECOSHal.*|libRCEC_la_LIBADD = -lhal_aidl ${top_builddir}/osal/src/libRCECOSHal.la|' \
      ${B}/ccec/src/Makefile

    sed -i \
      's|libRCEC_la_LDFLAGS = -lpthread|libRCEC_la_LDFLAGS = -lpthread -lbinder -lutils -llog -lbase|' \
      ${B}/ccec/src/Makefile
}

# entservices-hdmicecsource still looks for the legacy HAL soname.
# On x86 we only build libRCEC/libRCECOSHal, so provide a compatibility symlink.
do_install:append:x86-64() {
        if [ -e "${D}${libdir}/libRCEC.so" ] && [ ! -e "${D}${libdir}/libRCECHal.so" ]; then
                ln -sf libRCEC.so ${D}${libdir}/libRCECHal.so
        fi
}

FILES:${PN}:append:x86-64 = " ${libdir}/libRCECHal.so"