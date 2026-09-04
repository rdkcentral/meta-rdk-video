SUMMARY = "TensorFlow Lite C Library"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

TENSORFLOW_RELEASE_BRANCH ?= "r2.13"

SRC_URI        = "git://github.com/tensorflow/tensorflow;protocol=https;branch=${TENSORFLOW_RELEASE_BRANCH};name=xraudio-tensorflow-lite-lib"
SRCREV         = "7598e84989f70a75070534cb51ef49aaef927379"

S = "${WORKDIR}/git/tensorflow/lite/c"
FILES:${PN}:append      = " /vendor/lib/libtensorflowlite_c.so"
FILES:${PN}-dev:append  = " /vendor/include"
INHIBIT_PACKAGE_STRIP   = "0"
SOLIBS                  = ".so"
FILES_SOLIBSDEV         = ""
SYSROOT_DIRS:append     = " /vendor/lib /vendor/include"

ARM_VERSION ?= "armv7"

inherit cmake

do_install () {
   # Copy Library
   install -d ${D}/vendor/lib/
   cp ${B}/libtensorflowlite_c.so               ${D}/vendor/lib
   # RDK-20060: Full stripping of ELF files
   if [ "x${INHIBIT_PACKAGE_STRIP}" != "x1" ]; then
      ${TARGET_PREFIX}strip --strip-unneeded --remove-section=.comment ${D}/vendor/lib/libtensorflowlite_c.so || true
   fi

   # Copy headers
   cur=$(pwd)
   cd ${S}/..
   install -d ${D}/vendor/include/tensorflow/lite/
   cp $(find . -maxdepth 1 -name "*.h*")           ${D}/vendor/include/tensorflow/lite/
   cp --parents $(find ./c -name "*.h*")           ${D}/vendor/include/tensorflow/lite/
   cp --parents $(find ./core/ -name "*.h*")       ${D}/vendor/include/tensorflow/lite/
   cp --parents $(find ./delegates/ -name "*.h*")  ${D}/vendor/include/tensorflow/lite/
   cd ${cur}
}

do_correct_toolchain_file() {
   sed -i "s/CMAKE_SYSTEM_PROCESSOR arm/CMAKE_SYSTEM_PROCESSOR ${ARM_VERSION}/g" ${WORKDIR}/toolchain.cmake
}

EXTRA_OECMAKE     = " -DTFLITE_ENABLE_XNNPACK=ON"
CFLAGS:append     = " -O3 -mfp16-format=ieee"
CXXFLAGS:append   = " -O3 -mfp16-format=ieee"
FC                = ""

LDFLAGS:append    = " -lpthread"

addtask correct_toolchain_file after do_generate_toolchain_file before do_configure

do_configure:prepend:kirkstone() {

    cmake -DCMAKE_CROSSCOMPILING=OFF -DCMAKE_C_FLAGS="${BUILD_CFLAGS}" -DCMAKE_C_COMPILER="${BUILD_CC}" -DCMAKE_CXX_COMPILER="${BUILD_CXX}" -DCMAKE_CXX_FLAGS="${BUILD_CXX_FLAGS}" -S ${S} -B ${WORKDIR}/build
    cmake --build ${WORKDIR}/build
    rm -rf ${WORKDIR}/build/CMakeCache.txt
    rm -rf ${WORKDIR}/build/Makefile
    rm -rf ${WORKDIR}/build/cmake_install.cmake
    rm -rf ${WORKDIR}/build/CMakeFiles
}
# Kirkstone... Network access from tasks is now disabled by default on kernels which support this feature
do_configure[network] = "1"

INSANE_SKIP:${PN}:append:kirkstone = " already-stripped"
