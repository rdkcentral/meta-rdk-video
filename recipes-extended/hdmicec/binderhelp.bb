# Todo: Move this into another meta layer

SUMMARY = "Linux binder libs and headers"
DESCRIPTION = "Builds binder libraries and installs binder-related headers for consumers."

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "1.0.11"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

# 1) Binder tools + libs repo
SRC_URI = "git://github.com/rdkcentral/linux_binder_idl.git;branch=develop;protocol=https;name=binder"
SRCREV_binder = "8ed54005e11f24079ade4311fffb44375966acd9"

S = "${WORKDIR}/git"

# Build bits for binder libs
BINDER_BITS = "64"

# No separate configure for linux_binder_idl
do_configure[noexec] = "1"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://0001-use-found-flexbison.patch"
SRC_URI += "file://0002-skip-build-aidl-generator-tool-on-yocto.patch"

# Binder link artifacts are staged for build-time use below. Do not declare a
# runtime dependency on a separate libbinder package here because some targets
# do not provide one as a package provider.

BBCLASSEXTEND = "native"
PROVIDES += "libbinder"

# We need cmake/flex/bison for binder builds.
DEPENDS += "cmake-native flex-native bison-native"

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
# TARGET: binder libs only
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

}

SYSROOT_PREPROCESS_FUNCS:append:class-target = " stage_binder_link_libs"

stage_binder_link_libs() {
    if [ "${PN}" = "${BPN}-native" ]; then
        return 0
    fi

    # builds still need binder link libraries in the sysroot so
    # hdmicec can link against -lbinder/-lutils/-llog/-lbase during recipe
    # builds, but these runtime files must not be packaged by rdk-halif-aidl.
    install -d ${SYSROOT_DESTDIR}${libdir}
    if [ -d ${B}/install/lib ]; then
        for staged_lib in ${B}/install/lib/*.so; do
            if [ ! -e "${staged_lib}" ]; then
                return 0
            fi
            cp -a "${staged_lib}" ${SYSROOT_DESTDIR}${libdir}/
        done
    fi
}

# Stop Yocto from putting unversioned .so into -dev
FILES_SOLIBSDEV = ""

FILES:${PN}-dev = "\
    ${includedir} \
"
