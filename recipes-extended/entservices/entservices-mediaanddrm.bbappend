# Temporary TTS build troubleshooting.
PACKAGECONFIG:remove = "texttospeech"

# SystemAudioPlayer also needs the platform dependency previously supplied by TTS.
DEPENDS:append = " ${@bb.utils.contains('PACKAGECONFIG', 'systemaudioplayer', 'virtual/vendor-systemaudioplatform', '', d)}"
RDEPENDS:${PN}:append = " ${@bb.utils.contains('PACKAGECONFIG', 'systemaudioplayer', 'virtual/vendor-systemaudioplatform', '', d)}"
