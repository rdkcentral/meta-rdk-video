SUMMARY = "Linux binder AIDL generator, libs, and RDK HAL AIDL interfaces for PowerManager prototype"
DESCRIPTION = "Builds AIDL compiler, binder libs from linux_binder_idl, and the DeepSleep AIDL interface used by the PowerManager ARM-first prototype."

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "1.0.11"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRC_URI = "git://github.com/rdkcentral/linux_binder_idl.git;branch=develop;protocol=https;name=binder"
SRCREV_binder = "8ed54005e11f24079ade4311fffb44375966acd9"

S = "${WORKDIR}/git"

SRC_URI += "git://github.com/rdkcentral/rdk-halif-aidl.git;branch=develop;protocol=https;destsuffix=rdk-hal-aidl;name=hal"
SRCREV_hal  = "8ab4217b36a2a614384f5276343fc67226a3a236"

RDK_HAL_S = "${WORKDIR}/rdk-hal-aidl"

BINDER_BITS = "64"

AIDL_TARGET ?= "deepsleep"
AIDL_EXTRA_TARGETS ?= "boot"
AIDL_SRC_VERSION ?= "current"

do_configure[noexec] = "1"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://0001-use-found-flexbison.patch"
SRC_URI += "file://0002-skip-build-aidl-generator-tool-on-yocto.patch"

BBCLASSEXTEND = "native"

DEPENDS += "cmake-native flex-native bison-native rdk-halif-aidl-native"

inherit cmake

do_compile:class-native() {
    export PATH="${STAGING_BINDIR_NATIVE}:$PATH"

    cd ${S}

    AIDL_INSTALL_DIR="${B}/native-install"

    . ${S}/setup-env.sh ${BINDER_BITS} "${AIDL_INSTALL_DIR}"

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

do_compile() {
    cd ${S}

    export PATH="${STAGING_BINDIR_NATIVE}:$PATH"
    export YOCTO_SKIP_AIDL=1

    . ${S}/setup-env.sh ${BINDER_BITS} ${B}/install

    export WORK_DIR="${S}"
    export ANDROID_DIR="${WORK_DIR}/android"
    export AIDL_GEN_DIR="${WORKDIR}/aidl-generator"
    export AIDL_GEN_OUT_DIR="${WORKDIR}/aidl-generator/out"

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

    export AIDL_BIN="${STAGING_BINDIR_NATIVE}/aidl"

    if [ ! -x "${AIDL_BIN}" ]; then
        bbfatal "AIDL binary not found at ${AIDL_BIN}; ensure linux-binder-idl-native is in DEPENDS"
    fi

    for aidl_target in ${AIDL_TARGET} ${AIDL_EXTRA_TARGETS}; do
        bbnote "Building RDK HAL AIDL interface: target=${aidl_target} version=${AIDL_SRC_VERSION}"

        mkdir -p ${RDK_HAL_S}/build
        cd ${RDK_HAL_S}/build

        cmake .. \
            -DAIDL_TARGET="${aidl_target}" \
            -DAIDL_SRC_VERSION="${AIDL_SRC_VERSION}" \
            -DAIDL_BIN="${AIDL_BIN}"

        cmake --build .
    done
}

do_install:class-native() {
    install -d ${D}${bindir}

    aidl_src=$(find ${B} ${S} -maxdepth 10 -type f -name "aidl" -perm -111 2>/dev/null | head -n 1 || true)

    if [ -z "${aidl_src}" ]; then
        echo "Debug: no aidl found under ${B} or ${S}"
        ls -R ${B} ${S} || true
        bbfatal "aidl binary not found under ${B} or ${S}"
    fi

    install -m 0755 "${aidl_src}" "${D}${bindir}/aidl"
}

do_install() {
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

    install -d ${D}${libdir} ${D}${bindir}

    cp -a ${B}/install/lib/*.so ${D}${libdir}/

    if [ -f ${B}/install/bin/servicemanager ]; then
        install -m 0755 ${B}/install/bin/servicemanager ${D}${bindir}/
    fi

    for aidl_target in ${AIDL_TARGET} ${AIDL_EXTRA_TARGETS}; do
        GEN_DIR="${RDK_HAL_S}/gen/${aidl_target}/${AIDL_SRC_VERSION}"

        if [ -d "${GEN_DIR}" ]; then
            install -d ${D}${datadir}/rdk/aidl/${aidl_target}/${AIDL_SRC_VERSION}
            cp -r ${GEN_DIR}/* ${D}${datadir}/rdk/aidl/${aidl_target}/${AIDL_SRC_VERSION}/
        else
            bbwarn "RDK HAL AIDL gen dir ${GEN_DIR} not found; check generator output path."
        fi

        GEN_CPP_DIR="${GEN_DIR}/cpp"

        if [ -d "${GEN_CPP_DIR}/com" ]; then
            install -d ${D}${includedir}
            cp -r ${GEN_CPP_DIR}/com ${D}${includedir}/
        else
            bbwarn "RDK HAL AIDL cpp dir ${GEN_CPP_DIR}/com not found; check generator output."
        fi

        GEN_H_DIR="${GEN_DIR}/h"

        if [ -d "${GEN_H_DIR}/com" ]; then
            install -d ${D}${includedir}
            cp -r "${GEN_H_DIR}/com" "${D}${includedir}/"
        else
            bbwarn "HAL AIDL header dir ${GEN_H_DIR}/com not found; check generator output."
        fi
    done
}

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

