inherit features_check
REQUIRED_DISTRO_FEATURES = "enable_libsoup3 wpe-2.46"

PATCHTOOL = "git"

require wpe-webkit.inc

# Advance PR with every change in the recipe
PR  = "r26"

DEPENDS:append = " virtual/vendor-secapi2-adapter virtual/vendor-gst-drm-plugins "
DEPENDS:append = " libtasn1 unifdef-native libsoup libepoxy libgcrypt fontconfig"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

# Tip of the branch on Dec 11, 2025
SRCREV = "6b140c3eed5e0e9c21bcb67a20438c7ee1d20aba"

BASE_URI ?= "git://github.com/WebPlatformForEmbedded/WPEWebKit.git;protocol=http;branch=wpe-2.46"
SRC_URI = "${BASE_URI}"

# Drop after PR is accepted
# - none -

# Drop after westeros change is approved and released
SRC_URI += "file://2.46/comcast-RDK-58780-set-segment-position-field.patch"

# Comcast specific changes
SRC_URI += "file://2.46/comcast-RDKTV-380-disable-privileges-loss.patch"
SRC_URI += "file://2.46/comcast-WKIT-553-add-video-ave-mimetype-for-holepunc.patch"
SRC_URI += "file://2.46/comcast-AMLOGIC-628-always-initialze-volume.patch"
SRC_URI += "file://2.46/comcast-RDK-57261-Disable-optional-parser.patch"
SRC_URI += "file://2.46/comcast-RDK-57741-sleep-150-microsecs-instead-of-s.patch"
SRC_URI += "file://2.46/comcast-RDK-56287-rdkat-atspi2.patch"
SRC_URI += "file://2.46/comcast-RDK-57771-Flush-AppendPipeline-resetParserState.patch"
SRC_URI += "file://2.46/comcast-RDK-57915-Increase-html-parser-time-limit.patch"
SRC_URI += "file://2.46/comcast-RDKTV-1411-force-stop-media-on-loading-about.patch"
SRC_URI += "file://2.46/comcast-RDK-57915-Secure-minidump-path.patch"
SRC_URI += "file://2.46/comcast-RDKTV-6665-Remove-screen-saver-disabler.patch"
SRC_URI += "file://2.46/comcast-RDK-57915-Support-for-external-sink-x-dvb.patch"
SRC_URI += "file://2.46/comcast-RDK-57915-scan-decoder-elements-on-Broadcom.patch"
SRC_URI += "file://2.46/comcast-RDKTV-17281-RDKTV-17781-Workaround-for-AppleTV-rendering.patch"
SRC_URI += "file://2.46/comcast-RDKTV-18852-Restrict-inspection-of-locally-hosted-pages.patch"
SRC_URI += "file://2.46/comcast-RDK-57915-Analyze-higher-CPU-usage.patch"
SRC_URI += "file://2.46/comcast-RDK-40634-Only-support-decoders-with-hw-support-for-webrtc.patch"
SRC_URI += "file://2.46/comcast-RDK-57915-Include-HW-secure-decrypt-WidevineL1.patch"
SRC_URI += "file://2.46/comcast-RDK-58053-MSE-skip-seek-to-duration-if-player-not-loaded.patch"
SRC_URI += "file://2.46/comcast-RDK-57915-Fix-init-data-filtering.patch"
SRC_URI += "file://2.46/comcast-RDK-57915-log-HTML5-video-playback.patch"
SRC_URI += "file://2.46/comcast-RDK-57915-EME-generate-MEDIA_ERR_ENCRYPTED.patch"
SRC_URI += "file://2.46/comcast-RDK-57915-Track-encrypted-playback.patch"
SRC_URI += "file://2.46/comcast-WebRTC-keep-render-time-interpolation.patch"
SRC_URI += "file://2.46/comcast-DELIA-59087-Disable-pausing-playback-for-buf.patch"
SRC_URI += "file://2.46/comcast-RDKTV-28214-Quick-_exit.patch"
#SRC_URI += "file://2.46/comcast-RDK-37379-Mute-release-logging.patch"

PACKAGECONFIG[atk]                   = "-DUSE_ATK=ON,-DUSE_ATK=OFF,at-spi2-atk,"
PACKAGECONFIG[accessibility]         = "-DUSE_ATSPI=ON,-DUSE_ATSPI=OFF,rdkat-atspi2,rdkat-atspi2"
PACKAGECONFIG[asan]                  = "-DENABLE_SANITIZERS=address,,gcc-sanitizers"
PACKAGECONFIG[avif]                  = "-DUSE_AVIF=ON,-DUSE_AVIF=OFF,"
PACKAGECONFIG[bubblewrapsandbox]     = "-DENABLE_BUBBLEWRAP_SANDBOX=ON,-DENABLE_BUBBLEWRAP_SANDBOX=OFF,"
PACKAGECONFIG[developermode]         = "-DDEVELOPER_MODE=ON -DENABLE_COG=OFF,-DDEVELOPER_MODE=OFF,wpebackend-fdo wayland-native,"
PACKAGECONFIG[documentation]         = "-DENABLE_DOCUMENTATION=ON,-DENABLE_DOCUMENTATION=OFF, gi-docgen-native gi-docgen"
PACKAGECONFIG[encryptedmedia]        = "-DENABLE_ENCRYPTED_MEDIA=ON,-DENABLE_ENCRYPTED_MEDIA=OFF,"
PACKAGECONFIG[experimental]          = "-DENABLE_EXPERIMENTAL_FEATURES=ON,-DENABLE_EXPERIMENTAL_FEATURES=OFF,"
PACKAGECONFIG[fhd]                   = "-DVIDEO_DECODING_LIMIT=1920x1080@60,-DVIDEO_DECODING_LIMIT=3840x2160@60,"
PACKAGECONFIG[gstreamergl]           = "-DUSE_GSTREAMER_GL=ON,-DUSE_GSTREAMER_GL=OFF,"
PACKAGECONFIG[gstwebrtc]             = "-DUSE_GSTREAMER_WEBRTC=ON,-DUSE_GSTREAMER_WEBRTC=OFF, "
PACKAGECONFIG[introspection]         = "-DENABLE_INTROSPECTION=ON,-DENABLE_INTROSPECTION=OFF, gobject-introspection-native"
PACKAGECONFIG[journaldlog]           = "-DENABLE_JOURNALD_LOG=ON,-DENABLE_JOURNALD_LOG=OFF,"
PACKAGECONFIG[jpegxl]                = "-DUSE_JPEGXL=ON,-DUSE_JPEGXL=OFF,"
PACKAGECONFIG[lcms]                  = "-DUSE_LCMS=ON,-DUSE_LCMS=OFF, "
PACKAGECONFIG[logs]                  = "-DENABLE_LOGS=ON,,"
PACKAGECONFIG[malloc_heap_breakdown] = "-DENABLE_MALLOC_HEAP_BREAKDOWN=ON,-DENABLE_MALLOC_HEAP_BREAKDOWN=OFF,malloc-zone, malloc-zone"
PACKAGECONFIG[mathml]                = "-DENABLE_MATHML=ON,-DENABLE_MATHML=OFF,"
PACKAGECONFIG[mediarecoder]          = "-DENABLE_MEDIA_RECORDER=ON,-DENABLE_MEDIA_RECORDER=OFF,"
PACKAGECONFIG[mediastream]           = "-DENABLE_MEDIA_STREAM=ON -DENABLE_WEB_RTC=ON,-DENABLE_MEDIA_STREAM=OFF -DENABLE_WEB_RTC=OFF,libevent libopus libvpx alsa-lib,libevent"
PACKAGECONFIG[native_audio]          = "-DUSE_GSTREAMER_NATIVE_AUDIO=ON, -DUSE_GSTREAMER_NATIVE_AUDIO=OFF,"
PACKAGECONFIG[native_video]          = "-DUSE_GSTREAMER_NATIVE_VIDEO=ON, -DUSE_GSTREAMER_NATIVE_VIDEO=OFF,"
PACKAGECONFIG[pdfjs]                 = "-DENABLE_PDFJS=ON,-DENABLE_PDFJS=OFF,,"
PACKAGECONFIG[releaselog]            = "-DENABLE_RELEASE_LOG=ON,"
PACKAGECONFIG[remoteinspector]       = "-DENABLE_REMOTE_INSPECTOR=ON,-DENABLE_REMOTE_INSPECTOR=OFF,"
PACKAGECONFIG[speechsynthesis]       = "-DENABLE_SPEECH_SYNTHESIS=ON -DUSE_FLITE=OFF -DUSE_TTS_CLIENT=ON,-DENABLE_SPEECH_SYNTHESIS=OFF,tts"
PACKAGECONFIG[touchevents]           = "-DENABLE_TOUCH_EVENTS=ON,-DENABLE_TOUCH_EVENTS=OFF,"
PACKAGECONFIG[video]                 = "-DENABLE_VIDEO=ON,-DENABLE_VIDEO=OFF,gstreamer1.0 gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-bad,${RDEPS_VIDEO}"
PACKAGECONFIG[webassembly]           = "-DENABLE_WEBASSEMBLY=ON,-DENABLE_WEBASSEMBLY=OFF, "
PACKAGECONFIG[webdriver]             = "-DENABLE_WEBDRIVER=ON,-DENABLE_WEBDRIVER=OFF,"
PACKAGECONFIG[woff2]                 = "-DUSE_WOFF2=ON,-DUSE_WOFF2=OFF,woff2"
PACKAGECONFIG[wpeframework_opencdm]  = "-DENABLE_THUNDER=ON,-DENABLE_THUNDER=OFF,wpeframework-clientlibraries,"
PACKAGECONFIG[wpeplatform]           = "-DENABLE_WPE_PLATFORM=ON,-DENABLE_WPE_PLATFORM=OFF -DUSE_LIBDRM=OFF -DUSE_GBM=OFF,libdrm,"
PACKAGECONFIG[wpeqtapi]              = "-DENABLE_WPE_QT_API=ON,-DENABLE_WPE_QT_API=OFF"
PACKAGECONFIG[cairo]                 = "-DUSE_CAIRO=ON -DUSE_SKIA=OFF,-DUSE_CAIRO=OFF,cairo"
PACKAGECONFIG[externalholepunch]     = "-DUSE_EXTERNAL_HOLEPUNCH=ON,-DUSE_EXTERNAL_HOLEPUNCH=OFF,"
PACKAGECONFIG[ftrace]                = "-DUSE_LINUX_FTRACE=ON,-DUSE_LINUX_FTRACE=OFF,"

# Config options are no longer available in 2.46
PACKAGECONFIG[2dcanvas]     = ""
PACKAGECONFIG[indexeddb]    = ""
PACKAGECONFIG[subtlecrypto] = ""
PACKAGECONFIG[westeros]     = ""

PACKAGECONFIG:append = " webdriver remoteinspector releaselog accessibility speechsynthesis native_video webaudio woff2 externalholepunch"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'malloc_heap_breakdown', 'malloc_heap_breakdown', '', d)}"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'wpe-webkit-developer-mode', 'developermode tools', '', d)}"
PACKAGECONFIG:append = " ${@bb.utils.contains('BROWSER_MEMORYPROFILE', 'fhd', 'fhd', '', d)}"

PACKAGECONFIG:append:aarch64 = " webassembly"
PACKAGECONFIG:append:toolchain-clang = " uselld"

EXTRA_OECMAKE += " \
  -DPYTHON_EXECUTABLE=${STAGING_BINDIR_NATIVE}/python3-native/python3 \
"
INSANE_SKIP:${PN} += "rpaths"
WPE_LIB_RUNPATH ?= "\$ORIGIN/../../lib"
EXTRA_OECMAKE:append = " -DCMAKE_INSTALL_RPATH=${WPE_LIB_RUNPATH}"
SYSROOT_DIRS:append = " ${libexecdir}"
EXTRA_OECMAKE:append = " -DCMAKE_SHARED_LINKER_FLAGS='${LDFLAGS}-Wl,--enable-new-dtags'"
EXTRA_OECMAKE:append = " -DCMAKE_EXE_LINKER_FLAGS='${LDFLAGS}-Wl,--enable-new-dtags'"

FILES:${PN} += " ${libdir}/wpe-webkit-*/injected-bundle/libWPEInjectedBundle.so"
FILES:${PN}-web-inspector-plugin += " ${libdir}/wpe-webkit-*/libWPEWebInspectorResources.so"

TUNE_CCARGS:remove = "${@bb.utils.contains('DISTRO_FEATURES', 'wpe-webkit-debugfission', '','-fno-omit-frame-pointer -fno-optimize-sibling-calls', d)}"
TUNE_CCARGS:append = " -fno-delete-null-pointer-checks"

WPE_WEBKIT_LTO ??= "-flto=auto"
TARGET_CFLAGS += "${WPE_WEBKIT_LTO}"
TARGET_LDFLAGS += "${WPE_WEBKIT_LTO}"
TARGET_LDFLAGS:toolchain-clang += "-fuse-ld=lld"

def wk_use_ccache(bb,d):
    if d.getVar('CCACHE_DISABLED', True) == "1":
       return "NO"
    if bb.data.inherits_class("icecc", d) and d.getVar('ICECC_DISABLED', True) != "1":
       return "NO"
    return "YES"
export WK_USE_CCACHE="${@wk_use_ccache(bb, d)}"
