SUMMARY = "Linux binder AIDL generator, libs, and RDK HAL AIDL interfaces"
DESCRIPTION = "Builds AIDL compiler, binder libs from linux_binder_idl, and RDK HAL AIDL interfaces."

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

# 1) Binder tools + libs repo
SRC_URI = "git://github.com/rdkcentral/linux_binder_idl.git;branch=develop;protocol=https;name=binder"
SRCREV_binder = "8ed54005e11f24079ade4311fffb44375966acd9"

S = "${WORKDIR}/git"

# 2) RDK HAL AIDL CMake project
SRC_URI += "git://github.com/rdkcentral/rdk-halif-aidl.git;branch=develop;protocol=https;destsuffix=rdk-hal-aidl;name=hal"
SRCREV_hal  = "8ab4217b36a2a614384f5276343fc67226a3a236"

# Directory containing CMakeLists.txt and build 6.sh originally
RDK_HAL_S = "${WORKDIR}/rdk-hal-aidl"

# Build bits for binder libs
BINDER_BITS = "64"

# Parameters matching the shell script defaults
AIDL_TARGET      ?= "hdmicec"
AIDL_SRC_VERSION ?= "current"

# No separate configure for linux_binder_idl
do_configure[noexec] = "1"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://0001-use-found-flexbison.patch"
SRC_URI += "file://0002-skip-build-aidl-generator-tool-on-yocto.patch"

BBCLASSEXTEND = "native"

# We need cmake/flex/bison for binder, plus the native aidl tool for RDK HAL
DEPENDS += "cmake-native flex-native bison-native rdk-halif-aidl-native"

inherit cmake

do_compile:class-native() {
    export PATH="${STAGING_BINDIR_NATIVE}:$PATH"

    cd ${S}

    # Choose a dedicated install dir for the native aidl tool
    AIDL_INSTALL_DIR="${B}/native-install"

    # Set up env for native build, using our chosen install dir
    . ${S}/setup-env.sh ${BINDER_BITS} "${AIDL_INSTALL_DIR}"

    # Make sure all paths are based on the source tree, not ${WORKDIR}/temp
    export WORK_DIR="${S}"
    export ANDROID_DIR="${WORK_DIR}/android"
    export AIDL_GEN_DIR="${WORK_DIR}/aidl-generator"
    export AIDL_GEN_OUT_DIR="${B}/aidl-generator-out"

    clone_android_binder_repo
    if [ $? -ne 0 ]; then
        bbfatal "Failed to clone Android binder repos (native)"
    fi

    build_aidl_generator_tool
    if [ $? -ne 0 ]; then
        bbfatal "Failed to build AIDL generator tool (native)"
    fi
}

# =========
# TARGET: binder libs + RDK HAL AIDL
# =========
do_compile() {
    cd ${S}

    export PATH="${STAGING_BINDIR_NATIVE}:$PATH"

    # Build binder libs as you already do
    export YOCTO_SKIP_AIDL=1

    . ${S}/setup-env.sh ${BINDER_BITS} ${B}/install

    export WORK_DIR="${S}"
    export ANDROID_DIR="${WORK_DIR}/android"
    export AIDL_GEN_DIR="${WORK_DIR}/aidl-generator"
    export AIDL_GEN_OUT_DIR="${WORK_DIR}/aidl-generator/out"

    export CMAKE="cmake \
       -DFLEX_EXECUTABLE=${STAGING_BINDIR_NATIVE}/flex \
       -DBISON_EXECUTABLE=${STAGING_BINDIR_NATIVE}/bison"

    clone_android_binder_repo
    if [ $? -ne 0 ]; then
        bbfatal "Failed to clone Android binder repos"
    fi

    mkdir -p "${B}"
    cd "${B}"

    cmake \
      -DCMAKE_INSTALL_PREFIX="${B}/install" \
      -DBUILD_ENV_HOST=OFF \
      -DBUILD_ENV_YOCTO=ON \
      -DTARGET_LIB64_VERSION=ON \
      "${S}"

    cmake --build . --target install -- -j${BB_NUMBER_THREADS}

    # =========
    # Now build the RDK HAL AIDL interfaces (equivalent of build 6.sh)
    # =========
    bbnote "Building RDK HAL AIDL interface: target=${AIDL_TARGET} version=${AIDL_SRC_VERSION}"

    # Use the native aidl tool from linux-binder-idl-native
    export AIDL_BIN="${STAGING_BINDIR_NATIVE}/aidl"

    if [ ! -x "${AIDL_BIN}" ]; then
        bbfatal "AIDL binary not found at ${AIDL_BIN}; ensure linux-binder-idl-native is in DEPENDS"
    fi

    mkdir -p ${RDK_HAL_S}/build
    cd ${RDK_HAL_S}/build

    cmake .. \
        -DAIDL_TARGET="${AIDL_TARGET}" \
        -DAIDL_SRC_VERSION="${AIDL_SRC_VERSION}" \
        -DAIDL_BIN="${AIDL_BIN}"

    cmake --build .
}

do_install:class-native() {
    install -d ${D}${bindir}

    # Look for a built 'aidl' binary under this recipe's build/source trees
    aidl_src=$(find ${B} ${S} -maxdepth 10 -type f -name "aidl" -perm -111 2>/dev/null | head -n 1 || true)

    if [ -z "${aidl_src}" ]; then
        echo "Debug: no aidl found under ${B} or ${S}"
        ls -R ${B} ${S} || true
        bbfatal "aidl binary not found under ${B} or ${S}"
    fi

    echo "Installing aidl from ${aidl_src} to ${D}${bindir}/aidl"
    install -m 0755 "${aidl_src}" "${D}${bindir}/aidl"
}

do_install() {
    # Existing binder headers
    install -d \
        ${D}${includedir}/binder \
        ${D}${includedir}/android \
        ${D}${includedir}/android-base \
        ${D}${includedir}/utils \
        ${D}${includedir}/log \
        ${D}${includedir}/cutils

    cp -a ${S}/android/native/libs/binder/include/binder/* \
        ${D}${includedir}/binder/

    cp -a ${S}/android/native/libs/binder/ndk/include_cpp/android/* \
        ${D}${includedir}/android/

    cp -a ${S}/android/libbase/include/android-base/* \
        ${D}${includedir}/android-base/

    cp -a ${S}/android/core/libutils/include/utils/* \
        ${D}${includedir}/utils/

    cp -a ${S}/android/logging/liblog/include/log/* \
        ${D}${includedir}/log/

    cp -a ${S}/android/logging/liblog/include/android/* \
        ${D}${includedir}/android/

    cp -a ${S}/android/core/libcutils/include/cutils/* \
        ${D}${includedir}/cutils/

    # Binder libs + servicemanager
    install -d ${D}${libdir} ${D}${bindir}

    cp -a ${B}/install/lib/*.so ${D}${libdir}/

    if [ -f ${B}/install/bin/servicemanager ]; then
        install -m 0755 ${B}/install/bin/servicemanager ${D}${bindir}/
    fi

    # =========
    # Install RDK HAL AIDL generated files (as data)
    # matches: gen/<target>/<version>/
    # =========
    GEN_DIR="${RDK_HAL_S}/gen/${AIDL_TARGET}/${AIDL_SRC_VERSION}"

    if [ -d "${GEN_DIR}" ]; then
        install -d ${D}${datadir}/rdk/aidl/${AIDL_TARGET}/${AIDL_SRC_VERSION}
        cp -r ${GEN_DIR}/* ${D}${datadir}/rdk/aidl/${AIDL_TARGET}/${AIDL_SRC_VERSION}/
    else
        bbwarn "RDK HAL AIDL gen dir ${GEN_DIR} not found; check CMake output path."
    fi

    # =========
    # Install RDK HAL AIDL C++ headers for consumers like hdmicec
    # IHdmiCec.h is under: gen/.../cpp/com/rdk/hal/hdmicec/IHdmiCec.h
    # =========
    GEN_CPP_DIR="${GEN_DIR}/cpp"

    if [ -d "${GEN_CPP_DIR}/com" ]; then
        install -d ${D}${includedir}
        cp -r ${GEN_CPP_DIR}/com ${D}${includedir}/
    else
        bbwarn "RDK HAL AIDL cpp dir ${GEN_CPP_DIR}/com not found; check generator output."
    fi

    GEN_H_DIR="${GEN_DIR}/h"

    if [ -d "${GEN_H_DIR}/com" ]; then
        bbnote "Installing HAL AIDL headers from ${GEN_H_DIR}/com to ${D}${includedir}/com"
        install -d ${D}${includedir}
        cp -r "${GEN_H_DIR}/com" "${D}${includedir}/"
    else
        bbwarn "HAL AIDL header dir ${GEN_H_DIR}/com not found; check generator output."
    fi
}

# Stop Yocto from putting unversioned .so into -dev
FILES_SOLIBSDEV = ""

FILES:${PN} += "\
    ${libdir}/libbase.so \
    ${libdir}/libbinder.so \
    ${libdir}/libcutils.so \
    ${libdir}/libcutils_sockets.so \
    ${libdir}/liblog.so \
    ${libdir}/libutils.so \
    ${datadir}/rdk/aidl \
"

FILES:${PN}-dev = "\
    ${includedir} \
"

FILES:${PN}-dev += " ${includedir}/com "
