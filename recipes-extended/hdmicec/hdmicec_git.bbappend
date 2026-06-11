DEPENDS += " rdk-halif-aidl"

# Keep arm aligned with the newer AIDL-based hdmicec source used on x86.
SRCREV_hdmicec = "1e1d6961c0dc7ef7dd37b2d71e50157a3f20b94c"

# Add include paths for AIDL-generated HAL headers and binder headers.
CFLAGS:append = " -I${STAGING_INCDIR}/com/rdk/hal/hdmicec -I${STAGING_INCDIR}/binder -I${STAGING_INCDIR}/android"
CXXFLAGS:append = " -I${STAGING_INCDIR}/com/rdk/hal/hdmicec -I${STAGING_INCDIR}/binder -I${STAGING_INCDIR}/android"

# The aidl_feature Makefile.am does not compile the AIDL-generated stub .cpp files
# that rdk-halif-aidl installs to ${STAGING_INCDIR}/com/rdk/hal/hdmicec/.
# Those files define the typeinfo/vtable symbols (IHdmiCecEventListener, BnHdmiCecEventListener,
# IHdmiCec::serviceName, etc.) that libAidlHal.so requires at runtime.
# Fix: compile them into a static archive and inject into libAidlHAL_la_LIBADD.
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
	#  1. link the AIDL stubs archive into libAidlHal.so so typeinfo symbols are defined
	#  2. add -lbinder so android::BBinder/android::BpBinder typeinfo is resolved at
	#     runtime from libbinder.so
	sed -i '/^libAidlHAL_la_LIBADD = /d' "${B}/ccec/src/Makefile"
	sed -i \
		'/^libAidlHAL_la_LDFLAGS = -lpthread$/a libAidlHAL_la_LIBADD = '"${B}"'/libhdmicec_aidl_stubs.a -lbinder -lutils -llog' \
		"${B}/ccec/src/Makefile"
}

# entservices-hdmicecsource still looks for the legacy HAL soname.
# Provide a compatibility symlink for the new layout as on x86.
do_install:append() {
	if [ -e "${D}${libdir}/libRCEC.so" ] && [ ! -e "${D}${libdir}/libRCECHal.so" ]; then
		ln -sf libRCEC.so ${D}${libdir}/libRCECHal.so
	fi
}

FILES:${PN}:append = " ${libdir}/libRCECHal.so"

