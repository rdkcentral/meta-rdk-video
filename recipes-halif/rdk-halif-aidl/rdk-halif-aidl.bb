SUMMARY = "HAL interface definitions for RDK-E (AIDL) + generated C++ headers + libhal_aidl.so"
DESCRIPTION = "Fetches rdk-halif-aidl AIDL definitions, generates C++ sources/headers using the native aidl compiler, and builds libhal_aidl.so using an integrated Makefile."

HOMEPAGE = "https://github.com/rdkcentral/rdk-halif-aidl"

LICENSE = "Apache-2.0"

LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327 \
    file://NOTICE;md5=72a4c03c01acaed8abfdc37f08efad93 \
"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI = " \
    ${CMF_GITHUB_ROOT}/rdk-halif-aidl;${CMF_GITHUB_SRC_URI_SUFFIX} \
    file://Makefile \
"

PV = "0.13.0"
SRCREV = "047e0ffa0347fe4eadd18043b407366abb8f65ee"

S = "${WORKDIR}/git"

inherit cmake

PROVIDES += "rdk-halif-aidl-mw"

DEPENDS += " \
    libbinderrdk \
    libbinderrdk-native \
"

AIDL_BIN ?= "${STAGING_BINDIR_NATIVE}/aidl"

AIDL_SRC_VERSION ?= "current"

HAL_AIDL_MODULES ?= " \
    hdmicec \
"

AIDL_GEN_DIR = "${B}/current"

AIDL_LIB_SONAME_MAJOR ?= "1"
AIDL_LIB_VERSION ?= "1.0.0"

do_configure() {

    rm -rf ${AIDL_GEN_DIR}
    install -d ${AIDL_GEN_DIR}

    export LD_LIBRARY_PATH="${RECIPE_SYSROOT_NATIVE}${libdir}/mw:${RECIPE_SYSROOT_NATIVE}${libdir}:${LD_LIBRARY_PATH}"

    for m in ${HAL_AIDL_MODULES}; do
        cmake \
            -S ${S} \
            -B ${B}/cmake_${m} \
            -DAIDL_BIN=${AIDL_BIN} \
            -DAIDL_TARGET=${m} \
            -DAIDL_SRC_VERSION=${AIDL_SRC_VERSION} \
            -DAIDL_GEN_DIR=${AIDL_GEN_DIR}
    done
}

do_compile() {

    install -d ${B}/aidl_lib
    install -d ${B}/lib

    install -m 0644 \
        ${WORKDIR}/Makefile \
        ${B}/aidl_lib/Makefile

    oe_runmake -C ${B}/aidl_lib \
        CXX="${CXX}" \
        AIDL_GEN_DIR="${AIDL_GEN_DIR}" \
        AIDL_LIB_PATH="${B}/lib/libhal_aidl.so.${AIDL_LIB_VERSION}" \
        AIDL_LIB_INC_FLAGS=" \
            -I${AIDL_GEN_DIR}/h \
            -I${RECIPE_SYSROOT}${includedir}/mw \
        " \
        AIDL_LIB_CFLAGS="${CPPFLAGS} ${CXXFLAGS} -std=c++17 -fPIC -Wall -Wextra" \
        AIDL_LIB_LDFLAGS="${LDFLAGS} \
            -L${RECIPE_SYSROOT}${libdir}/mw \
            -L${RECIPE_SYSROOT}${libdir} \
            -Wl,-rpath-link=${RECIPE_SYSROOT}${libdir}/mw \
            -Wl,-rpath-link=${RECIPE_SYSROOT}${libdir} \
            -Wl,-soname,libhal_aidl.so.${AIDL_LIB_SONAME_MAJOR}" \
        AIDL_LIB_LD_LIBS="-lbinderrdk -lutilsrdk"
}

do_install() {

    #
    # Install generated headers under /usr/include/mw
    #
    install -d ${D}${includedir}/mw

    if [ -d ${AIDL_GEN_DIR}/h ]; then
        cp -apr ${AIDL_GEN_DIR}/h/* \
            ${D}${includedir}/mw/
    fi

    #
    # Install generated cpp sources
    #
    if [ -d ${AIDL_GEN_DIR}/cpp ]; then
        install -d ${D}${datadir}/mw/rdk-halif-aidl/cpp
        cp -apr ${AIDL_GEN_DIR}/cpp/* \
            ${D}${datadir}/mw/rdk-halif-aidl/cpp/
    fi

    #
    # Install original AIDL files under /usr/include/mw
    #
    for d in ${HAL_AIDL_MODULES} common; do
        if [ -d ${S}/${d}/${AIDL_SRC_VERSION} ]; then
            tar --no-same-owner -cpf - \
                -C ${S}/${d}/${AIDL_SRC_VERSION} . \
            | tar --no-same-owner -xpf - \
                -C ${D}${includedir}/mw
        fi
    done

    #
    # Remove unnecessary CMake files
    #
    find ${D}${includedir}/mw/hal/aidl -name CMakeLists.txt -delete || true

    #
    # Install library into /usr/lib/mw
    #
    install -d ${D}${libdir}/mw

    install -m 0755 \
        ${B}/lib/libhal_aidl.so.${AIDL_LIB_VERSION} \
        ${D}${libdir}/mw/

    ln -sf libhal_aidl.so.${AIDL_LIB_VERSION} \
        ${D}${libdir}/mw/libhal_aidl.so.${AIDL_LIB_SONAME_MAJOR}

    ln -sf libhal_aidl.so.${AIDL_LIB_SONAME_MAJOR} \
        ${D}${libdir}/mw/libhal_aidl.so
}

FILES:${PN} += " \
    ${libdir}/mw/libhal_aidl.so.* \
"

FILES:${PN}-dev += " \
    ${includedir}/mw/hal/h \
    ${includedir}/mw/hal/aidl \
    ${libdir}/mw/libhal_aidl.so \
    ${datadir}/mw/rdk-halif-aidl \
"
