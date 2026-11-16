SUMMARY = "Rialto reference player: C++ player library, CLI debugging front-end and JavaScriptCore binding"
DESCRIPTION = "Playback library over the public Rialto client API (IControl, IMediaPipeline) with an \
AAMP-compatible JavaScript binding for the WPE WebProcess. The CLI runs the same library outside a browser \
for debugging and for the negative API suite. The binding is built only with PACKAGECONFIG js-binding."
LICENSE = "CLOSED"
PACKAGECONFIG ??= "js-binding"
# Build the JavaScriptCore binding for the WebProcess. Adds WPE WebKit to the build dependencies.
# The WPE sysroot has no javascriptcoregtk pkg-config file; JavaScriptCore comes from wpe-webkit-1.1.pc.
# PACKAGECONFIG[js-binding] = "-DREFERENCE_PLAYER_BUILD_JS_BINDING=ON -DREFERENCE_PLAYER_JSC_PKG=wpe-webkit-1.1,-DREFERENCE_PLAYER_BUILD_JS_BINDING=OFF,wpe-webkit,pkgconfig-native,"
PACKAGECONFIG[js-binding] = "-DREFERENCE_PLAYER_BUILD_JS_BINDING=ON -DREFERENCE_PLAYER_JSC_PKG=wpe-webkit-1.1,-DREFERENCE_PLAYER_BUILD_JS_BINDING=OFF,wpe-webkit pkgconfig-native"
SRC_URI = "git://github.com/rdkcentral/rialto.git;protocol=https;branch=feature/RDKEMW-28666;subpath=tests/refplayer;destsuffix=refplayer"
SRCREV = "${AUTOREV}"
S = "${WORKDIR}/refplayer"
# Pulls in the Rialto client headers (${includedir}/rialto) and libRialtoClient.
DEPENDS = "rialto"
inherit cmake features_check
# Rialto is only built when this distro feature is enabled.
REQUIRED_DISTRO_FEATURES = "enable_rialto"
# Point the project at the Rialto client staged in the sysroot and force the real
# (non-mock) contract path so the tools actually link against libRialtoClient.
EXTRA_OECMAKE = " \
    -DREFERENCE_PLAYER_USE_MOCKS=OFF \
    -DREFERENCE_PLAYER_BUILD_TESTS=OFF \
    -DRIALTO_INCLUDE_DIR=${STAGING_INCDIR}/rialto \
    -DRIALTO_CLIENT_LIB=${STAGING_LIBDIR}/libRialtoClient.so \
"
# These unversioned .so files are runtime libraries, not development symlinks.
SOLIBS = ".so"
FILES_SOLIBSDEV = ""
FILES:${PN} = "${bindir}/rialto_reference_player ${libdir}/libRialtoRefPlayer.so"
# The JavaScript binding is a separate package so the CLI image does not carry it.
# The package is empty unless PACKAGECONFIG js-binding is set.
PACKAGES =+ "${PN}-jsbindings"
ALLOW_EMPTY:${PN}-jsbindings = "1"
FILES:${PN}-jsbindings = "${libdir}/libRialtoPlayerJSBindings.so"
RDEPENDS:${PN}-jsbindings = "${PN}"
