SUMMARY = "Mid-Field Voice (MFV) HAL plugin library (libxraudio_mfv.so)."
DESCRIPTION = "Builds and installs the mid-field voice plugin from the xr-ffv-hal-sky-llama \
repository (MFV/lib) as libxraudio_mfv.so, along with its plugin config and keyword model."
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://CMakeLists.txt;beginline=1;endline=18;md5=602a93007803a6da920064e0c075bfad"

PV = "1.0"
PR = "r0"

# TODO: pin to a real commit/tag once available.
SRCREV = "f5384312b42e4cada151edf7c83399c3aaecc0e3"
SRC_URI = "${RDKE_GITHUB_ROOT}/xr-ffv-hal-sky-llama;${RDKE_GITHUB_SRC_URI_SUFFIX};name=xr-mfv-hal"
SRCREV_FORMAT = "xr-mfv-hal"

S = "${WORKDIR}/git"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

# jansson is used for config parsing. The Comcast keyword source pulls in TensorFlow Lite
# (tensorflow/lite/c/c_api.h) and MFCC (MFCC.h) headers, which are expected to be provided by
# separate recipes staged into the sysroot.
# TODO: replace the placeholder tokens below with the real recipe/PROVIDES names.
DEPENDS = "jansson xraudio-tensorflow-lite-lib xr-dsp-algorithms"

inherit cmake pkgconfig

# The parent MFV/CMakeLists.txt carries the project() declaration and pulls in lib + test.
OECMAKE_SOURCEPATH = "${S}/MFV"

EXTRA_OECMAKE:append = " -DCMAKE_SYSROOT=${RECIPE_SYSROOT} -DCMAKE_PROJECT_VERSION=${PV}"

# OE's jansson has no CMake package config, so point find_package() at the
# pkg-config-backed Findjansson.cmake shipped with this recipe.
EXTRA_OECMAKE:append = " -DCMAKE_MODULE_PATH=${WORKDIR}"

# MFV/lib/CMakeLists.txt defines no install() rule, so install the artifacts manually.
do_install() {
    install -d ${D}${libdir}
    install -m 0755 ${B}/lib/libxraudio_mfv.so ${D}${libdir}/libxraudio_mfv.so

    install -d ${D}/opt/mfv_plugin
    install -m 0644 ${S}/MFV/lib/mfv_plugin_config.json ${D}/opt/mfv_plugin/mfv_plugin_config.json
    install -m 0644 ${S}/opt/ffv_hal/comcast_kw_model_it250716.tflite ${D}/opt/mfv_plugin/comcast_kw_model_it250716.tflite
}

FILES:${PN} += "${libdir}/libxraudio_mfv.so /opt/mfv_plugin/*"

# libxraudio_mfv.so is an unversioned plugin loaded at runtime. Treat it as a
# runtime solib and stop the -dev package from claiming the bare .so, otherwise
# packaging QA flags it (dev-elf / dev-so).
SOLIBS = ".so"
FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "dev-so"

# Only the plugin library is needed. The standalone MFV/test app is Windows-oriented
# and does not build cleanly for the target, so keep it disabled.
EXTRA_OECMAKE:append = " -DMFV_BUILD_TEST=OFF"