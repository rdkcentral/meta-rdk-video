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
#SRC_URI = " \
    file://CMakeLists.txt \
    file://README.md \
    file://include/rialto_refplayer/IRefPlayer.h \
    file://include/rialto_refplayer/RefPlayerEvent.h \
    file://include/rialto_refplayer/RefPlayerTypes.h \
    file://src/core/CommandWorker.cpp \
    file://src/core/CommandWorker.h \
    file://src/core/EventBus.cpp \
    file://src/core/EventBus.h \
    file://src/core/Logger.cpp \
    file://src/core/Logger.h \
    file://src/core/MediaFeeder.cpp \
    file://src/core/MediaFeeder.h \
    file://src/core/PipelineCallbacks.cpp \
    file://src/core/PipelineCallbacks.h \
    file://src/core/PlayerStateMachine.cpp \
    file://src/core/PlayerStateMachine.h \
    file://src/core/PositionTracker.cpp \
    file://src/core/PositionTracker.h \
    file://src/core/RefPlayer.cpp \
    file://src/core/RefPlayer.h \
    file://src/core/RialtoNames.h \
    file://src/core/RialtoSession.cpp \
    file://src/core/RialtoSession.h \
    file://src/core/SourceManager.cpp \
    file://src/core/SourceManager.h \
    file://src/demux/DemuxerRegistry.cpp \
    file://src/demux/IDemuxer.h \
    file://src/demux/Mp4Demuxer.cpp \
    file://src/demux/Mp4Demuxer.h \
    file://src/demux/TrackModel.h \
    file://src/frontend/cli/CliLog.h \
    file://src/frontend/cli/ContractValidator.cpp \
    file://src/frontend/cli/ContractValidator.h \
    file://src/frontend/cli/NegativeChecks.cpp \
    file://src/frontend/cli/NegativeChecks.h \
    file://src/frontend/cli/main.cpp \
    file://src/frontend/jsbinding/JSEventListener.cpp \
    file://src/frontend/jsbinding/JSEventListener.h \
    file://src/frontend/jsbinding/JSUtils.cpp \
    file://src/frontend/jsbinding/JSUtils.h \
    file://src/frontend/jsbinding/RialtoPlayerJSBinding.cpp \
    file://src/frontend/jsbinding/RialtoPlayerJSBinding.h \
    file://src/frontend/jsbinding/RialtoPlayerJSBindings.map \
    file://test/mocks/rialto/IClientLogControl.h \
    file://test/mocks/rialto/IClientLogHandler.h \
    file://test/mocks/rialto/IControl.h \
    file://test/mocks/rialto/IMediaKeysCapabilities.h \
    file://test/mocks/rialto/IMediaPipeline.h \
    file://test/mocks/rialto/IMediaPipelineCapabilities.h \
    file://test/mocks/rialto/IWebAudioPlayer.h \
    file://test/unit/RefPlayerTest.cpp \
"
#S = "${WORKDIR}"
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
