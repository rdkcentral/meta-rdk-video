DESCRIPTION = "JavaScriptCore"
LICENSE = "LGPL-2.1-only"
LIC_FILES_CHKSUM = "file://Source/WebCore/LICENSE-LGPL-2.1;md5=a778a33ef338abbaf8b8a7c36b6eec80 "

inherit cmake pkgconfig perlnative ${@bb.utils.contains("DISTRO_FEATURES", "kirkstone", "python3native", "pythonnative", d)} gettext

SRCREV = "583d02964d606c0f600ce5a3df98e017c8712931"
SRC_URI = "git://github.com/WebPlatformForEmbedded/WPEWebKit.git;protocol=http;branch=wpe-2.28"
SRC_URI += " file://jsconly_buildissues.diff"
SRC_URI += " file://es6support.diff"
SRC_URI += " file://0001-fix-build-error.diff"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

S = "${WORKDIR}/git"

DEPENDS += " \
     glib-2.0-native gperf-native ninja-native ruby-native \
     glib-2.0 gnutls icu pcre sqlite3 zlib curl \
"

OECMAKE_GENERATOR = "Ninja"

EXTRA_OECMAKE += " \
    -DCMAKE_BUILD_TYPE=Release \
    -DENABLE_STATIC_JSC=ON \
    -DCMAKE_EXPORT_COMPILE_COMMANDS=ON \
    -DPORT=JSCOnly \
    -DUSE_CAPSTONE=OFF \
    -G Ninja \
    -DENABLE_JIT=ON \
    -DUSE_LD_GOLD=OFF \
    -DCMAKE_COLOR_MAKEFILE=OFF \
    -DENABLE_FTL_JIT=ON \
    -DUSE_THIN_ARCHIVES=OFF \
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

TUNE_CCARGS:remove = "-fno-omit-frame-pointer -fno-optimize-sibling-calls"
TUNE_CCARGS:append = " -fno-delete-null-pointer-checks"

COMPATIBLE_MACHINE:mipsel = "(.*)"
LDFLAGS:append = " -Wl,--no-keep-memory"

do_install() {
   install -d ${D}/${libdir}
   cp -a ${B}/lib/libJavaScriptCore.a ${D}/${libdir}
   cp -a ${B}/lib/libbmalloc.a ${D}/${libdir}
   cp -a ${B}/lib/libWTF.a ${D}/${libdir}
   cp -a ${B}/lib/libjsc_lib.so ${D}/${libdir}

   install -d ${D}${includedir}
   mkdir -p ${D}${includedir}/JavaScriptCore
   mkdir -p ${D}${includedir}/wtf

   cp -R ${B}/DerivedSources/ForwardingHeaders/JavaScriptCore/*.h ${D}${includedir}/JavaScriptCore/.
   cp -R ${B}/DerivedSources/ForwardingHeaders/wtf/* ${D}${includedir}/wtf/.
}

FILES:${PN} += "${libdir}/*.so"
#FILES:${PN} += "${libdir}/libbmalloc.a"
#FILES:${PN} += "${libdir}/libWTF.a"
#FILES:${PN} += "${libdir}/libJavaScriptCore.a"
FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "dev-so staticdev"
INSANE_SKIP:${PN}:append:morty = " ldflags"
INSANE_SKIP:${PN} += "already-stripped"
INSANE_SKIP:${PN}:append:morty = " ldflags"
DEBIAN_NOAUTONAME:${PN} = "1"
BBCLASSEXTEND = "native"
