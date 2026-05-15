DESCRIPTION = "JavaScriptCore"
LICENSE = "LGPL-2.1-only"
LIC_FILES_CHKSUM = "file://Source/WebCore/LICENSE-LGPL-2.1;md5=a778a33ef338abbaf8b8a7c36b6eec80 "

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit cmake pkgconfig perlnative ${@bb.utils.contains("DISTRO_FEATURES", "kirkstone", "python3native", "pythonnative", d)} gettext

SRCREV = "fc1703ed69006e92c6d014d1de7d1ea7b9d2f915"
SRC_URI = "git://github.com/WebPlatformForEmbedded/WPEWebKit.git;protocol=http;branch=wpe-2.38"
SRC_URI += " file://0001-wpe-2.38-changes-update.patch"

S = "${WORKDIR}/git"

DEPENDS += " \
     glib-2.0-native gperf-native ninja-native ruby-native \
     glib-2.0 gnutls icu pcre sqlite3 zlib curl \
"

OECMAKE_GENERATOR = "Ninja"

EXTRA_OECMAKE += " \
    -DCMAKE_BUILD_TYPE=Release \
    -DENABLE_STATIC_JSC=OFF \
    -DCMAKE_EXPORT_COMPILE_COMMANDS=ON \
    -DPORT=JSCOnly \
    -DUSE_CAPSTONE=OFF \
    -G Ninja \
    -DENABLE_JIT=ON \
    -DUSE_LD_GOLD=OFF \
    -DCMAKE_COLOR_MAKEFILE=OFF \
    -DENABLE_FTL_JIT=ON \
    -DUSE_THIN_ARCHIVES=OFF \
    -DENABLE_WEBASSEMBLY=ON \
    -DENABLE_API_TESTS=OFF \
"

# don't build debug
FULL_OPTIMIZATION:remove = "-g"
FULL_OPTIMIZATION:append = " -g1"
FULL_OPTIMIZATION:remove = "-Os"
FULL_OPTIMIZATION:remove = "-O2"
FULL_OPTIMIZATION:remove = "-O3"
#
WPE_WEBKIT_OPTIMIZATION ?= "-O2"
#
## Prevent a compile-time crash
SELECTED_OPTIMIZATION:remove = "-pipe"
SELECTED_OPTIMIZATION:append = " ${WPE_WEBKIT_OPTIMIZATION}"
#
SELECTED_OPTIMIZATION:remove = "-g"
SELECTED_OPTIMIZATION:append = " -g1 "

#Optimize for size
SELECTED_OPTIMIZATION:append = " -Os"
SELECTED_OPTIMIZATION:append = " -fdata-sections -ffunction-sections"
SELECTED_OPTIMIZATION:append = " -flto"
SELECTED_OPTIMIZATION:append = " -fstack-usage"

TUNE_CCARGS:remove = "-fno-omit-frame-pointer -fno-optimize-sibling-calls"
TUNE_CCARGS:append = " -fno-delete-null-pointer-checks"

COMPATIBLE_MACHINE:mipsel = "(.*)"
LDFLAGS:append = " -Wl,--no-keep-memory,--strip-all"

do_install() {
   install -d ${D}/${libdir}
   install -d ${D}/${libdir}/javascriptcore
   rm -f ${B}/lib/libJavaScriptCore.so*.ltrans*
   cp -a ${B}/lib/libJavaScriptCore.so* ${D}/${libdir}/javascriptcore/.

   install -d ${D}${includedir}
   mkdir -p ${D}${includedir}/JavaScriptCore
   mkdir -p ${D}${includedir}/wtf


   cp -R ${B}/bmalloc/Headers/* ${D}${includedir}/.
   cp -R ${B}/JavaScriptCore/Headers/* ${D}${includedir}/.
   cp -R ${B}/JavaScriptCore/PrivateHeaders/JavaScriptCore/* ${D}${includedir}/JavaScriptCore/.
   cp -R ${B}/WTF/Headers/* ${D}${includedir}/.
}

FILES:${PN} += " ${libdir}/javascriptcore/libJavaScriptCore.so*"
FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "dev-so staticdev"
INSANE_SKIP:${PN}:append:morty = " ldflags"
INSANE_SKIP:${PN} += "already-stripped"
INSANE_SKIP:${PN}:append:morty = " ldflags"
DEBIAN_NOAUTONAME:${PN} = "1"
BBCLASSEXTEND = "native"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
