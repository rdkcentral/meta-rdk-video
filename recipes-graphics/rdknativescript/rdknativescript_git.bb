DESCRIPTION = "JSRuntime"
HOMEPAGE = ""

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${THISDIR}/files/Apache-2.0;md5=3b83ef96387f14655fc854ddc3c6bd57"

DEPENDS = "westeros essos rapidjson rtcore libuv gstreamer1.0 uwebsockets javascriptcore aamp websocketpp"
DEPENDS:append = " virtual/egl"
RDEPENDS:${PN}:append = " essos gstreamer1.0 uwebsockets"

inherit cmake pkgconfig perlnative ${@bb.utils.contains("DISTRO_FEATURES", "kirkstone", "python3native", "pythonnative", d)} gettext

S = "${WORKDIR}/git"

PV ?= "2.0.0"
PR ?= "r1"

SRC_URI = "${CMF_GITHUB_ROOT}/rdkNativeScript;${CMF_GITHUB_SRC_URI_SUFFIX}"

#Release 1.0.1
SRCREV = "bbe641cd29d8f90384c7df735fd75c8e2c827ed8"

OECMAKE_GENERATOR = "Ninja"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
EXTRA_OECMAKE += " -DPKG_CONFIG_SYSROOT_DIR=${PKG_CONFIG_SYSROOT_DIR}"

EXTRA_OECMAKE += " -DJSRUNTIME_ENGINE_NAME=jsc"
EXTRA_OECMAKE += " -DBUILD_JSRUNTIME_DESKTOP=OFF"
EXTRA_OECMAKE += " -DENABLE_JSRUNTIME_ESSOS=ON"
EXTRA_OECMAKE += " -DENABLE_AAMP_JSBINDINGS=ON"
EXTRA_OECMAKE += " -DENABLE_AAMP_JSBINDINGS_STATIC=ON"
EXTRA_OECMAKE += " -DENABLE_AAMP_JSBINDINGS_DYNAMIC=OFF"
EXTRA_OECMAKE += " -DENABLE_JSRUNTIME_PLAYER=ON"
EXTRA_OECMAKE += " \
    -G Ninja \
    -DENABLE_REMOTE_INSPECTOR=ON \
"
BUILD_CLIENT="1"
ENABLE_SERVER="1"

EXTRA_OECMAKE += "${@' -DBUILD_JSRUNTIME_CLIENT=ON' if d.getVar('BUILD_CLIENT') == '1' else ''}"
EXTRA_OECMAKE += "${@' -DENABLE_JSRUNTIME_SERVER=ON' if d.getVar('ENABLE_SERVER') == '1' else ''}"

do_install() {
   install -d ${D}/home/root/modules
   install -d ${D}/home/root

   if [ "${BUILD_CLIENT}" = "1" ]; then
      cp -a ${B}/JSRuntimeClient ${D}/home/root/JSRuntimeClient
   fi

   cp -a ${B}/JSRuntimeJSC ${D}/home/root/JSRuntimeJSC

   cp -a ${S}/utils/xhr.js ${D}/home/root/modules/.
   cp -a ${S}/utils/punycode.js ${D}/home/root/modules/.
   cp -a ${S}/utils/http.js ${D}/home/root/modules/.
   cp -a ${S}/utils/https.js ${D}/home/root/modules/.
   cp -a ${S}/utils/ws.js ${D}/home/root/modules/.
   cp -a ${S}/utils/utils.js ${D}/home/root/modules/.
   cp -a ${S}/utils/buffer.js ${D}/home/root/modules/.
   cp -a ${S}/utils/process.js ${D}/home/root/modules/.
   cp -a ${S}/src/jsc/modules/event.js ${D}/home/root/modules/.
   cp -a ${S}/src/jsc/modules/wsenhanced.js ${D}/home/root/modules/.
   cp -a ${S}/src/jsc/modules/linkedjsdom.js ${D}/home/root/modules/.
   cp -a ${S}/src/jsc/modules/linkedjsdomwrapper.js ${D}/home/root/modules/.
   cp -a ${S}/src/jsc/modules/node-fetch.js ${D}/home/root/modules/.
   cp -a ${S}/src/jsc/modules/windowwrapper.js ${D}/home/root/modules/.
   cp -a ${S}/src/jsc/modules/lib ${D}/home/root/modules/.
   cp -a ${S}/src/jsc/modules/video.js ${D}/home/root/modules/. 

   install -d ${D}/${libdir}
   cp -a ${B}/libJSRuntimeJSC.so ${D}/${libdir}
   cp -a ${B}/libjsclib.so ${D}/${libdir}


   install -d ${D}/${libdir}
   install -d ${D}${includedir}
   mkdir -p ${D}${includedir}/jsruntime

   install -m 0644 ${S}/include/*.h ${D}${includedir}/jsruntime
}

FILES:${PN} += "${libdir}/*.so"
FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "dev-so staticdev"
INSANE_SKIP:${PN}:append:morty = " ldflags"
INSANE_SKIP:${PN} += "already-stripped"
INSANE_SKIP:${PN}:append:morty = " ldflags"
DEBIAN_NOAUTONAME:${PN} = "1"
BBCLASSEXTEND = "native"

FILES:${PN} += "${@'/home/root/JSRuntimeClient' if d.getVar('BUILD_CLIENT') == '1' else ''}"
FILES:${PN} += "/home/root/JSRuntimeJSC"
FILES:${PN} += "/home/root/modules"
FILES:${PN} += "${libdir}/libJSRuntimeJSC.so"
