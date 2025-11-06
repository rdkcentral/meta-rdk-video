PATCHTOOL = "git"

require wpe-webkit.inc

# Advance PR with every change in the recipe
PR  = "r9"

# Temporary build fix
DEPENDS:append = " virtual/vendor-secapi2-adapter virtual/vendor-gst-drm-plugins "
DEPENDS:append = " fontconfig"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

# Tip of the branch on Dec 19, 2024
SRCREV = "680d0afd323e533a8ed07749eccd091996c66e2c"

BASE_URI ?= "git://github.com/WebPlatformForEmbedded/WPEWebKit.git;protocol=http;branch=wpe-2.38"
SRC_URI = "${BASE_URI}"

# Drop after a PR is approved or different fix is available in wpe-2.38 branch
SRC_URI += "file://2.38.2/1196.patch"
SRC_URI += "file://2.38.6/1384.patch"
SRC_URI += "file://2.38.7/1410.patch"

# Drop after issue is addressed and a corresponding PR is merged
SRC_URI += "file://2.38.8/1456-RDKTV-35082-Workaround-premature-finishSeek.patch"

# Drop after tip of branch has been revised
SRC_URI += "file://2.38.8/1423-revert.patch"
SRC_URI += "file://2.38.8/1531.patch"
SRC_URI += "file://2.38.8/cmake-Fix-recompilation-on-rebuild-without-changes.patch"

# Drop after libwpe upgrade
SRC_URI += "file://2.38.8/RDK-54304-Fix-build-with-an-older-libpwe.patch"

# Comcast specific changes
SRC_URI += "file://2.38.7/comcast-DELIA-60920-Malloc-Heap-Breakdown.patch"
SRC_URI += "file://2.38/comcast-RDK-38169-fix-build-for-Broadcom-platform.patch"
SRC_URI += "file://2.38.5/comcast-XRE-15382-libwebrtc-fake-encoder.patch"
SRC_URI += "file://2.38/comcast-WKIT-553-add-video-ave-mimetype-for-holepunc.patch"
SRC_URI += "file://2.38/comcast-RDK-37379-Mute-release-logging.patch"
SRC_URI += "file://2.38/comcast-RDKTV-380-disable-privileges-loss.patch"
SRC_URI += "file://2.38.5/comcast-DELIA-24951-log-HTML5-video-playback.patch"
SRC_URI += "file://2.38/comcast-XRE-13593-EME-generate-MEDIA_ERR_ENCRYPTED-f.patch"
SRC_URI += "file://2.38/comcast-XRE-13851-Increase-html-parser-time-limit.patch"
SRC_URI += "file://2.38/comcast-AMLOGIC-628-always-initialze-volume.patch"
SRC_URI += "file://2.38/comcast-RDKTV-1411-force-stop-media-on-loading-about.patch"
SRC_URI += "file://2.38/comcast-RDKTV-6665-Remove-screen-saver-disabler.patch"
SRC_URI += "file://2.38/comcast-LLAMA-2184-Support-for-external-sink-for-x-d.patch"
SRC_URI += "file://2.38/comcast-XRE-13799-XRE-13989-Track-encrypted-playback.patch"
SRC_URI += "file://2.38.4/comcast-RDK-28954-SERXIONE-4574-Minidump-exception-h.patch"
SRC_URI += "file://2.38/comcast-RDKTV-17737-play-pause-mapping.patch"
SRC_URI += "file://2.38.5/comcast-XRE-15382-XIONE-4595-RDKTV-17736-HDR-DV-conf.patch"
SRC_URI += "file://2.38/comcast-RDKTV-17281-RDKTV-17781-Workaround-for-AppleTV-rende.patch"
SRC_URI += "file://2.38/comcast-RDKTV-18852-Restrict-inspection-of-locally-h.patch"
SRC_URI += "file://2.38/comcast-LLAMA-8030-Fix-init-data-filtering.patch"
SRC_URI += "file://2.38/comcast-LLAMA-8558-vttcue-middle-align-keyword-compa.patch"
SRC_URI += "file://2.38/comcast-DELIA-59087-Disable-pausing-playback-for-buf.patch"
SRC_URI += "file://2.38.2/comcast-AMLOGIC-3262-SERXIONE-4051-disable-scaletempto.patch"
SRC_URI += "file://2.38/comcast-DELIA-60055-Analyze-higher-CPU-usage-of-Web-Network-.patch"
SRC_URI += "file://2.38/comcast-DELIA-60613-WebRTC-streaming-fails-with-test.patch"
SRC_URI += "file://2.38/comcast-RDK-40567-Speech-Synthesis.patch"
SRC_URI += "file://2.38.5/comcast-RDK-40689-Add-RDKAT-support.patch"
SRC_URI += "file://2.38/comcast-RDK-41913-Don-t-fail-playback-with-closed-caption-ce.patch"
SRC_URI += "file://2.38/comcast-RDK-40634-Only-support-decoders-with-hw-support-for-webrtc.patch"
SRC_URI += "file://2.38.2/comcast-AMLOGIC-3262-Initial-support-for-instant-rat.patch"
SRC_URI += "file://2.38.1/comcast-LLAMA-12502-Disable-Permissions-API-for-radioplayer.org.patch"
SRC_URI += "file://2.38.2/comcast-LLAMA-12282-Add-quirk-for-RTLPlay-mini-player.patch"
SRC_URI += "file://2.38.7/comcast-RDKTV-28214-Quick-_exit.patch"
SRC_URI += "file://2.38.5/comcast-SERXIONE-4428-scan-decoder-elements-on-Broad.patch"
SRC_URI += "file://2.38.5/comcast-WebRTC-keep-render-time-interpolation.patch"
SRC_URI += "file://2.38.5/comcast-RDK-48799-disable-service-worker-by-default.patch"
SRC_URI += "file://2.38.8/comcast-DELIA-57933-Increase-minor-version-or-WPE-lib.patch"
SRC_URI += "file://2.38.8/comcast-LLAMA-15112-sleep-150-microsecs-instead-of-s.patch"
SRC_URI += "file://2.38.8/comcast-DELIA-67128-GCHeap-snapshot.patch"
SRC_URI += "file://2.38.8/comcast-LLAMA-16805-Include-HW-secure-decrypt-decode-in-robu.patch"
SRC_URI += "file://2.38.8/comcast-RDKEMW-2744-BitmapTextureGL-Check-EGL-context.patch"
SRC_URI += "file://2.38.8/comcast-dynamic-insertion-of-decryptor.patch"
SRC_URI += "file://2.38.8/comcast-DELIA-68848-webrtc-improvements.patch"

PACKAGECONFIG[wpeqtapi]          = "-DENABLE_WPE_QT_API=ON,-DENABLE_WPE_QT_API=OFF"
PACKAGECONFIG[westeros]          = "-DUSE_WPEWEBKIT_PLATFORM_WESTEROS=ON -DUSE_GSTREAMER_HOLEPUNCH=ON -DUSE_EXTERNAL_HOLEPUNCH=ON -DUSE_WESTEROS_SINK=ON,,westeros virtual/vendor-westeros-sink"
PACKAGECONFIG[encryptedmedia]    = "-DENABLE_ENCRYPTED_MEDIA=ON,-DENABLE_ENCRYPTED_MEDIA=OFF,"
PACKAGECONFIG[mathml]            = "-DENABLE_MATHML=ON,-DENABLE_MATHML=OFF,"
PACKAGECONFIG[touchevents]       = "-DENABLE_TOUCH_EVENTS=ON,-DENABLE_TOUCH_EVENTS=OFF,"
PACKAGECONFIG[remoteinspector]   = "-DENABLE_REMOTE_INSPECTOR=ON,-DENABLE_REMOTE_INSPECTOR=OFF,"
PACKAGECONFIG[vp9_hdr]           = "-DENABLE_HDR=ON,-DENABLE_HDR=OFF,,gstreamer1.0-plugins-good-matroska"
PACKAGECONFIG[gstreamergl]       = "-DUSE_GSTREAMER_GL=ON,-DUSE_GSTREAMER_GL=OFF,"
PACKAGECONFIG[mediastream]       = "-DENABLE_MEDIA_STREAM=ON -DENABLE_WEB_RTC=ON,-DENABLE_MEDIA_STREAM=OFF -DENABLE_WEB_RTC=OFF,libevent libopus libvpx alsa-lib,libevent"
PACKAGECONFIG[asan]              = "-DENABLE_SANITIZERS=address,,gcc-sanitizers"
PACKAGECONFIG[dolbyvision]       = "-DENABLE_DV=ON,-DENABLE_DV=OFF,,"
PACKAGECONFIG[developermode]     = "-DDEVELOPER_MODE=ON -DENABLE_COG=OFF,-DDEVELOPER_MODE=OFF,wpebackend-fdo wayland-native,"
PACKAGECONFIG[accessibility]     = "-DENABLE_ACCESSIBILITY=ON,-DENABLE_ACCESSIBILITY=OFF,atk tts rdkat,rdkat"
PACKAGECONFIG[speechsynthesis]   = "-DENABLE_SPEECH_SYNTHESIS=ON,-DENABLE_SPEECH_SYNTHESIS=OFF,tts"
PACKAGECONFIG[woff2]             = "-DUSE_WOFF2=ON,-DUSE_WOFF2=OFF,woff2"
PACKAGECONFIG[openjpeg]          = "-DUSE_OPENJPEG=ON,-DUSE_OPENJPEG=OFF,"
PACKAGECONFIG[webcrypto]         = "-DENABLE_WEB_CRYPTO=ON,-DENABLE_WEB_CRYPTO=OFF,libtasn1"
PACKAGECONFIG[bubblewrapsandbox] = "-DENABLE_BUBBLEWRAP_SANDBOX=ON,-DENABLE_BUBBLEWRAP_SANDBOX=OFF,"
PACKAGECONFIG[webdriver]         = "-DENABLE_WEBDRIVER=ON,-DENABLE_WEBDRIVER=OFF,"
PACKAGECONFIG[serviceworker]     = "-DENABLE_SERVICE_WORKER=ON,-DENABLE_SERVICE_WORKER=OFF,"
PACKAGECONFIG[experimental]      = "-DENABLE_EXPERIMENTAL_FEATURES=ON,-DENABLE_EXPERIMENTAL_FEATURES=OFF,"
PACKAGECONFIG[releaselog]        = "-DENABLE_RELEASE_LOG=ON,"
PACKAGECONFIG[usesoup2]          = "-DUSE_SOUP2=ON,-DUSE_SOUP2=OFF,libsoup-2.4"
PACKAGECONFIG[usesoup3]          = "-DUSE_SOUP2=OFF,,libsoup"
PACKAGECONFIG[native_audio]      = "-DUSE_GSTREAMER_NATIVE_AUDIO=ON, -DUSE_GSTREAMER_NATIVE_AUDIO=OFF,"
PACKAGECONFIG[native_video]      = "-DUSE_GSTREAMER_NATIVE_VIDEO=ON, -DUSE_GSTREAMER_NATIVE_VIDEO=OFF,"
PACKAGECONFIG[jpegxl]            = "-DUSE_JPEGXL=ON,-DUSE_JPEGXL=OFF,"
PACKAGECONFIG[documentation]     = "-DENABLE_DOCUMENTATION=ON,-DENABLE_DOCUMENTATION=OFF, gi-docgen-native gi-docgen"
PACKAGECONFIG[introspection]     = "-DENABLE_INTROSPECTION=ON,-DENABLE_INTROSPECTION=OFF, gobject-introspection-native"
PACKAGECONFIG[journaldlog]       = "-DENABLE_JOURNALD_LOG=ON,-DENABLE_JOURNALD_LOG=OFF,"
PACKAGECONFIG[lbse]              = "-DENABLE_LAYER_BASED_SVG_ENGINE=ON,-DENABLE_LAYER_BASED_SVG_ENGINE=OFF, "
PACKAGECONFIG[gstwebrtc]         = "-DUSE_GSTREAMER_WEBRTC=ON,-DUSE_GSTREAMER_WEBRTC=OFF, "
PACKAGECONFIG[lcms]              = "-DUSE_LCMS=ON,-DUSE_LCMS=OFF, "
PACKAGECONFIG[webassembly]       = "-DENABLE_WEBASSEMBLY=ON,-DENABLE_WEBASSEMBLY=OFF -DENABLE_WEBASSEMBLY_B3JIT=OFF, "
PACKAGECONFIG[malloc_heap_breakdown] = "-DENABLE_MALLOC_HEAP_BREAKDOWN=ON,-DENABLE_MALLOC_HEAP_BREAKDOWN=OFF,malloc-zone, malloc-zone"
PACKAGECONFIG[pdfjs]             = "-DENABLE_PDFJS=ON,-DENABLE_PDFJS=OFF,,"
PACKAGECONFIG[dolbyvision]       = "-DENABLE_DV=ON,-DENABLE_DV=OFF,,"
PACKAGECONFIG[vp9_hdr]           = "-DENABLE_HDR=ON,-DENABLE_HDR=OFF,,gstreamer1.0-plugins-good-matroska"
PACKAGECONFIG[instantratechange] = "-DENABLE_INSTANT_RATE_CHANGE=ON,-DENABLE_INSTANT_RATE_CHANGE=OFF,"
PACKAGECONFIG[logs]              = "-DENABLE_LOGS=ON,,"
PACKAGECONFIG[fhd]               = "-DVIDEO_DECODING_LIMIT=1920x1080@60,,"

PACKAGECONFIG:append = " vp9_hdr dolbyvision breakpad native_video woff2 serviceworker"
PACKAGECONFIG:append = " webcrypto webdriver remoteinspector releaselog accessibility speechsynthesis webaudio instantratechange"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'enable_libsoup3', 'usesoup3', 'usesoup2', d)}"
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

WPE_WEBKIT_LTO ??= "-flto=auto -fno-fat-lto-objects"
TARGET_CFLAGS += "${WPE_WEBKIT_LTO}"
TARGET_LDFLAGS += "${WPE_WEBKIT_LTO}"

def wk_use_ccache(bb,d):
    if d.getVar('CCACHE_DISABLED', True) == "1":
       return "NO"
    if bb.data.inherits_class("icecc", d) and d.getVar('ICECC_DISABLED', True) != "1":
       return "NO"
    return "YES"
export WK_USE_CCACHE="${@wk_use_ccache(bb, d)}"
