inherit features_check
REQUIRED_DISTRO_FEATURES = "enable_libsoup3 kirkstone wpe-2.46"

PATCHTOOL = "git"

require wpe-webkit.inc

# Advance PR with every change in the recipe
PR  = "r0"
PV .= "+git${SRCPV}"

# Temporary build fix
DEPENDS:append = " virtual/vendor-secapi2-adapter virtual/vendor-gst-drm-plugins libtasn1 unifdef-native libsoup "
DEPENDS:append = " fontconfig"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

# Tip of the branch on Apr 08, 2025
SRCREV = "4dbdf290d4bed1fbd85aab8f412cc934ee4c05d4"

BASE_URI ?= "git://github.com/WebPlatformForEmbedded/WPEWebKit.git;protocol=http;branch=wpe-2.46"
SRC_URI = "${BASE_URI}"

# Drop after PR is accepted
SRC_URI += "file://2.46/1474_SpeechSynthesis_cancel.patch"

# Comcast specific changes
SRC_URI += "file://2.46/comcast-RDKTV-380-disable-privileges-loss.patch"
SRC_URI += "file://2.46/comcast-XRE-13505-Dynamic-insertion-of-decryptor-ele.patch"
SRC_URI += "file://2.46/comcast-WKIT-553-add-video-ave-mimetype-for-holepunc.patch"
SRC_URI += "file://2.46/comcast-AMLOGIC-628-always-initialze-volume.patch"

PACKAGECONFIG[atk]                   = "-DUSE_ATK=ON,-DUSE_ATK=OFF,at-spi2-atk,"
PACKAGECONFIG[accessibility]         = "-DUSE_ATSPI=ON,-DUSE_ATSPI=OFF,tts rdkat at-spi2-core,rdkat"
PACKAGECONFIG[asan]                  = "-DENABLE_SANITIZERS=address,,gcc-sanitizers"
PACKAGECONFIG[avif]                  = "-DUSE_AVIF=ON,-DUSE_AVIF=OFF,"
PACKAGECONFIG[bubblewrapsandbox]     = "-DENABLE_BUBBLEWRAP_SANDBOX=ON,-DENABLE_BUBBLEWRAP_SANDBOX=OFF,"
PACKAGECONFIG[developermode]         = "-DDEVELOPER_MODE=ON -DENABLE_COG=OFF,-DDEVELOPER_MODE=OFF,wpebackend-fdo wayland-native,"
PACKAGECONFIG[documentation]         = "-DENABLE_DOCUMENTATION=ON,-DENABLE_DOCUMENTATION=OFF, gi-docgen-native gi-docgen"
PACKAGECONFIG[dolbyvision]           = "-DENABLE_DV=ON,-DENABLE_DV=OFF,,"
PACKAGECONFIG[encryptedmedia]        = "-DENABLE_ENCRYPTED_MEDIA=ON,-DENABLE_ENCRYPTED_MEDIA=OFF,"
PACKAGECONFIG[experimental]          = "-DENABLE_EXPERIMENTAL_FEATURES=ON,-DENABLE_EXPERIMENTAL_FEATURES=OFF,"
PACKAGECONFIG[fhd]                   = "-DVIDEO_DECODING_LIMIT=1920x1080@60,,"
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
PACKAGECONFIG[vp9_hdr]               = "-DENABLE_HDR=ON,-DENABLE_HDR=OFF,,gstreamer1.0-plugins-good-matroska"
PACKAGECONFIG[webassembly]           = "-DENABLE_WEBASSEMBLY=ON,-DENABLE_WEBASSEMBLY=OFF, "
PACKAGECONFIG[webdriver]             = "-DENABLE_WEBDRIVER=ON,-DENABLE_WEBDRIVER=OFF,"
PACKAGECONFIG[woff2]                 = "-DUSE_WOFF2=ON,-DUSE_WOFF2=OFF,woff2"
PACKAGECONFIG[wpeplatform]           = "-DENABLE_WPE_PLATFORM=ON,-DENABLE_WPE_PLATFORM=OFF -DUSE_LIBDRM=OFF -DUSE_GBM=OFF,libdrm,"
PACKAGECONFIG[wpeqtapi]              = "-DENABLE_WPE_QT_API=ON,-DENABLE_WPE_QT_API=OFF"
PACKAGECONFIG[cairo]                 = "-DUSE_CAIRO=ON -DUSE_SKIA=OFF,-DUSE_CAIRO=OFF,cairo"

PACKAGECONFIG:append = " vp9_hdr dolbyvision breakpad"
PACKAGECONFIG:append = " webdriver remoteinspector releaselog accessibility speechsynthesis native_video webaudio woff2"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'malloc_heap_breakdown', 'malloc_heap_breakdown', '', d)}"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'wpe-webkit-developer-mode', 'developermode tools', '', d)}"
PACKAGECONFIG:append = " ${@bb.utils.contains('BROWSER_MEMORYPROFILE', 'fhd', 'fhd', '', d)}"

PACKAGECONFIG:append:aarch64 = " webassembly"

PACKAGECONFIG:remove = "${@bb.utils.contains('HAS_HDR_SUPPORT', '0', 'vp9_hdr', '', d)}"
PACKAGECONFIG:remove = "${@bb.utils.contains('HAS_DOLBY_VISION_SUPPORT', '0', 'dolbyvision', '', d)}"

EXTRA_OECMAKE += " \
  -DPYTHON_EXECUTABLE=${STAGING_BINDIR_NATIVE}/python3-native/python3 \
"

FILES:${PN} += " ${libdir}/wpe-webkit-*/injected-bundle/libWPEInjectedBundle.so"
FILES:${PN}-web-inspector-plugin += " ${libdir}/wpe-webkit-*/libWPEWebInspectorResources.so"

TUNE_CCARGS:remove = "-fno-omit-frame-pointer -fno-optimize-sibling-calls"
TUNE_CCARGS:append = " -fno-delete-null-pointer-checks"

WPE_WEBKIT_LTO ??= "-flto=auto"
TARGET_CFLAGS += "${WPE_WEBKIT_LTO}"
TARGET_LDFLAGS += "${WPE_WEBKIT_LTO}"
TARGET_LDFLAGS_toolchain-clang += "-fuse-ld=lld"

def wk_use_ccache(bb,d):
    if d.getVar('CCACHE_DISABLED', True) == "1":
       return "NO"
    if bb.data.inherits_class("icecc", d) and d.getVar('ICECC_DISABLED', True) != "1":
       return "NO"
    return "YES"
export WK_USE_CCACHE="${@wk_use_ccache(bb, d)}"
