DEPENDS:append = " rdk-halif-aidl"

# New recipe for hdmicec - legacy and aidl
SRCREV_hdmicec = "8896a15c931e9a22cd2437305bf5c4bec6c32004"

# Add include paths for AIDL-generated HAL headers and binder headers
CFLAGS:append = " -I${STAGING_INCDIR}/com/rdk/hal/hdmicec -I${STAGING_INCDIR}/binder -I${STAGING_INCDIR}/android"
CXXFLAGS:append = " -I${STAGING_INCDIR}/com/rdk/hal/hdmicec -I${STAGING_INCDIR}/binder -I${STAGING_INCDIR}/android"

# The aidl_feature Makefile.am does not compile the AIDL-generated stub .cpp files
# that rdk-halif-aidl installs to ${STAGING_INCDIR}/com/rdk/hal/hdmicec/.
# Those files define the typeinfo/vtable symbols (IHdmiCecEventListener, BnHdmiCecEventListener,
# IHdmiCec::serviceName, etc.) that libRCEC.so requires at runtime.
# Fix: compile them into a static archive and inject into libRCEC_la_LIBADD.
do_compile:prepend() {
    OBJ_DIR="${B}/aidl_stubs"
    mkdir -p "${OBJ_DIR}"
    STUB_DIR="${STAGING_INCDIR}/com/rdk/hal/hdmicec"
    HAL_DIR="${STAGING_INCDIR}/com/rdk/hal"
    INCFLAGS="-I${STAGING_INCDIR} -I${STAGING_INCDIR}/com/rdk/hal/hdmicec -I${STAGING_INCDIR}/binder -I${STAGING_INCDIR}/android"
    for f in IHdmiCec IHdmiCecController IHdmiCecEventListener Property SendMessageStatus State; do
        ${CXX} ${CXXFLAGS} ${INCFLAGS} -fPIC \
            -c "${STUB_DIR}/${f}.cpp" -o "${OBJ_DIR}/${f}.o"
    done
    ${CXX} ${CXXFLAGS} ${INCFLAGS} -fPIC \
        -c "${HAL_DIR}/PropertyValue.cpp" -o "${OBJ_DIR}/PropertyValue.o"
    ${AR} rcs "${B}/libhdmicec_aidl_stubs.a" \
        "${OBJ_DIR}/IHdmiCec.o" \
        "${OBJ_DIR}/IHdmiCecController.o" \
        "${OBJ_DIR}/IHdmiCecEventListener.o" \
        "${OBJ_DIR}/Property.o" \
        "${OBJ_DIR}/SendMessageStatus.o" \
        "${OBJ_DIR}/State.o" \
        "${OBJ_DIR}/PropertyValue.o"
}

do_configure:append() {
    # Patch the generated Makefile to:
    #  1. link the AIDL stubs archive into libRCEC.so so typeinfo symbols are defined
    #  2. add -lbinder so android::BBinder/android::BpBinder typeinfo is resolved at
    #     runtime from libbinder.so (which rdk-halif-aidl installs)
    sed -i \
        "s|^libRCEC_la_LIBADD = .*|libRCEC_la_LIBADD = ${B}/libhdmicec_aidl_stubs.a \${top_builddir}/osal/src/libRCECOSHal.la|" \
        "${B}/ccec/src/Makefile"
    sed -i \
        "s|libRCEC_la_LDFLAGS = -lpthread|libRCEC_la_LDFLAGS = -lpthread -lbinder -lutils -llog -lbase|" \
        "${B}/ccec/src/Makefile"
}
