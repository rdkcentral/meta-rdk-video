SUMMARY = "WebKit for Wayland port pairs the WebKit engine with the Wayland display protocol, \
           allowing embedders to create simple and performant systems based on Web platform technologies. \
           It is designed with hardware acceleration in mind, relying on EGL, the Wayland EGL platform, and OpenGL ES."
HOMEPAGE = "http://www.webkitforwayland.org/"
LICENSE = "BSD-2-Clause & BSD-3-Clause & LGPL-2.0-only & LGPL-2.1-only"
LIC_FILES_CHKSUM = "file://Source/WebCore/LICENSE-LGPL-2.1;md5=a778a33ef338abbaf8b8a7c36b6eec80 "

CMAKE_VERBOSE = ""
DEPENDS += " \
     libwpe \
     bison-native ccache-native glib-2.0-native gperf-native libxml2-native ninja-native ruby-native chrpath-replacement-native \
     freetype glib-2.0 gnutls harfbuzz icu jpeg pcre sqlite3 zlib \
     libepoxy libpng libwebp libxml2 libxslt \
     virtual/egl virtual/libgles2 \
     libinput libepoxy libgcrypt \
"

EXTRANATIVEPATH += " chrpath-native"

S = "${WORKDIR}/git"

inherit cmake pkgconfig perlnative gettext python3native ccache add-version

PACKAGECONFIG ?= "2dcanvas mediasource encryptedmedia \
                  video westeros mediastream sampling-profiler gamepad \
                  ${@bb.utils.contains('DISTRO_FEATURES', 'opencdm', 'wpeframework_opencdm', '', d)}"

PACKAGECONFIG:remove:libc-musl = "sampling-profiler"

# WPE features
PACKAGECONFIG[2dcanvas] = "-DENABLE_ACCELERATED_2D_CANVAS=ON,-DENABLE_ACCELERATED_2D_CANVAS=OFF,"
PACKAGECONFIG[deviceorientation] = "-DENABLE_DEVICE_ORIENTATION=ON,-DENABLE_DEVICE_ORIENTATION=OFF,"
PACKAGECONFIG[fullscreenapi] = "-DENABLE_FULLSCREEN_API=ON,-DENABLE_FULLSCREEN_API=OFF,"
PACKAGECONFIG[gamepad] = "-DENABLE_GAMEPAD=ON,-DENABLE_GAMEPAD=OFF,"
PACKAGECONFIG[geolocation] = "-DENABLE_GEOLOCATION=ON,-DENABLE_GEOLOCATION=OFF,"
PACKAGECONFIG[indexeddb] = "-DENABLE_DATABASE_PROCESS=ON -DENABLE_INDEXED_DATABASE=ON,-DENABLE_DATABASE_PROCESS=OFF -DENABLE_INDEXED_DATABASE=OFF,"
PACKAGECONFIG[mediasource] = "-DENABLE_MEDIA_SOURCE=ON,-DENABLE_MEDIA_SOURCE=OFF,gstreamer1.0 gstreamer1.0-plugins-good,${RDEPS_MEDIASOURCE}"
PACKAGECONFIG[notifications] = "-DENABLE_NOTIFICATIONS=ON,-DENABLE_NOTIFICATIONS=OFF,"
PACKAGECONFIG[sampling-profiler] = "-DENABLE_SAMPLING_PROFILER=ON,-DENABLE_SAMPLING_PROFILER=OFF,"
PACKAGECONFIG[video] = "-DENABLE_VIDEO=ON,-DENABLE_VIDEO=OFF,gstreamer1.0 gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-bad,${RDEPS_VIDEO}"
PACKAGECONFIG[webaudio] = "-DENABLE_WEB_AUDIO=ON,-DENABLE_WEB_AUDIO=OFF,gstreamer1.0 gstreamer1.0-plugins-base gstreamer1.0-plugins-good,${RDEPS_WEBAUDIO}"
PACKAGECONFIG[tools] = "-DENABLE_TOOLS=ON,-DENABLE_TOOLS=OFF,,"
PACKAGECONFIG[breakpad] = "-DENABLE_BREAKPAD=ON, -DENABLE_BREAKPAD=OFF, breakpad"
PACKAGECONFIG[wpeframework_opencdm] = "-DENABLE_THUNDER=ON,-DENABLE_THUNDER=OFF,wpeframework-clientlibraries,"
PACKAGECONFIG[debugfission] = "-DDEBUG_FISSION=ON,-DDEBUG_FISSION=OFF,"
PACKAGECONFIG[uselld] = "-DTRY_USE_LD_LLD=ON -DUSE_LD_LLD=ON,,${MLPREFIX}clang-cross-${TARGET_ARCH}"

RDEPENDS:${PN} += "\
  libatomic \
"

EXTRA_OECMAKE_BUILD += " -l ${@oe.utils.parallel_make(d, False)}"

EXTRA_OECMAKE += " \
    -DCMAKE_BUILD_TYPE=Release \
    -DBUILD_SHARED_LIBS=ON \
    -DPORT=WPE \
    -G Ninja \
"

LDFLAGS:append = " -Wl,--no-keep-memory"

# don't build debug
FULL_OPTIMIZATION:remove = "-g"
FULL_OPTIMIZATION:append = " -g1"
FULL_OPTIMIZATION:remove = "-Os"
FULL_OPTIMIZATION:remove = "-O2"
FULL_OPTIMIZATION:remove = "-O3"

WPE_WEBKIT_OPTIMIZATION ?= "-O2"

# Prevent a compile-time crash
SELECTED_OPTIMIZATION:remove = "-pipe"
SELECTED_OPTIMIZATION:append = " ${WPE_WEBKIT_OPTIMIZATION}"

PACKAGES =+ "${PN}-web-driver"
PACKAGES =+ "${PN}-web-inspector-plugin"

FILES:${PN}-web-driver += "${bindir}/WPEWebDriver"
FILES:${PN}-web-inspector-plugin += "${libdir}/libWPEWebInspectorResources.so"
INSANE_SKIP:${PN}-web-driver += "file-rdeps"
INSANE_SKIP:${PN}-web-inspector-plugin = "dev-so"
INSANE_SKIP:${PN}:append = " rpaths"

DEBIAN_NOAUTONAME:${PN} = "1"
DEBIAN_NOAUTONAME:${PN}-dbg = "1"
DEBIAN_NOAUTONAME:${PN}-dev = "1"
DEBIAN_NOAUTONAME:${PN}-web-driver = "1"
DEBIAN_NOAUTONAME:${PN}-web-inspector-plugin = "1"

RDEPS_MEDIASOURCE = " \
    gstreamer1.0-plugins-good-isomp4 \
"

RDEPS_VIDEO = " \
    gstreamer1.0-plugins-base-app \
    gstreamer1.0-plugins-base-playback \
    gstreamer1.0-plugins-good-soup \
    gstreamer1.0-plugins-bad-opusparse \
    gstreamer1.0-plugins-good-matroska \
    gstreamer1.0-plugins-bad-subenc \
"

RDEPS_WEBAUDIO = " \
    gstreamer1.0-plugins-good-wavparse \
"

RDEPS_EXTRA = " \
    gstreamer1.0-plugins-base-audioconvert \
    gstreamer1.0-plugins-base-audioresample \
    gstreamer1.0-plugins-base-gio \
    gstreamer1.0-plugins-base-videoconvert \
    gstreamer1.0-plugins-base-videoscale \
    gstreamer1.0-plugins-base-volume \
    gstreamer1.0-plugins-base-typefindfunctions \
    gstreamer1.0-plugins-good-audiofx \
    gstreamer1.0-plugins-good-audioparsers \
    gstreamer1.0-plugins-good-autodetect \
    gstreamer1.0-plugins-good-avi \
    gstreamer1.0-plugins-good-deinterlace \
    gstreamer1.0-plugins-good-interleave \
    gstreamer1.0-plugins-bad-dash \
    gstreamer1.0-plugins-bad-mpegtsdemux \
    gstreamer1.0-plugins-bad-smoothstreaming \
    gstreamer1.0-plugins-bad-videoparsersbad \
"

# The RDEPS_EXTRA plugins are all required for certain media playback use cases,
# but have not yet been classified as being specific dependencies for video,
# webaudio or mediasource. Until that classification is done, add them all to
# each of the three groups...

RDEPS_MEDIASOURCE += "${RDEPS_EXTRA}"
RDEPS_VIDEO += "${RDEPS_EXTRA}"
RDEPS_WEBAUDIO += "${RDEPS_EXTRA}"

RRECOMMENDS:${PN} += "ca-certificates shared-mime-info wpe-backend-rdk-platform-plugin"
PACKAGE_DEBUG_SPLIT_STYLE = "debug-without-src"

# Ensure we'll get the recipe version and SRCRV into the versions.txt file part of the build image
do_add_version () {
    echo "WPE_WEBKIT_VERSION=${PV}-${PR}" > ${EXTRA_VERSIONS_PATH}/${PN}.txt
}


# Build wpewebkit with split-dwarf, see for more info:
#   https://gcc.gnu.org/wiki/DebugFission
#   https://www.tweag.io/blog/2023-11-23-debug-fission/
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'wpe-webkit-debugfission', 'debugfission uselld','', d)}"
SELECTED_OPTIMIZATION:remove =  "${@bb.utils.contains('DISTRO_FEATURES', 'wpe-webkit-debugfission', '-g1', '-g', d)}"
SELECTED_OPTIMIZATION:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'wpe-webkit-debugfission', '-g', '-g1', d)}"
WPE_WEBKIT_LTO:remove = "${@bb.utils.contains('DISTRO_FEATURES', 'wpe-webkit-debugfission', '-flto=auto', '', d)}"
do_install:append() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'wpe-webkit-debugfission', 'true', 'false', d)}; then
       rm -rf ${D}/usr/src/debug/${PN}-dwo.tgz
       install -d ${D}/usr/src/debug/
       # unfortunately dwp crashes when used with wpewebkit binaries. pack *.dwo files in tarball instead.
       (cd ${B}; find . -name '*.dwo' -print0 | tar -czvf ${D}/usr/src/debug/${PN}-dwo.tgz --null -T -)
    fi
}

WPE_LIB_RUNPATH ?= "\$ORIGIN:\$ORIGIN/../../lib"
EXTRA_OECMAKE:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'wpe-webkit-enable-new-dtags-runpath', ' -DCMAKE_INSTALL_RPATH=${WPE_LIB_RUNPATH} -DDISABLE_NEW_DTAGS=OFF', '', d)}"
SYSROOT_DIRS:append = " ${libexecdir}"