# Provides the TextToSpeech plugin settings for WPEFramwork

TEXTTOSPEECH_ENDPOINT ?= " "
TEXTTOSPEECH_SECURE_ENDPOINT ?= " "
TEXTTOSPEECH_LOCAL_ENDPOINT ?= " "
TEXTTOSPEECH_LANGUAGE ?= "en-US"
TEXTTOSPEECH_VOLUME ?= "100"
TEXTTOSPEECH_RATE ?= "50"
TEXTTOSPEECH_ENDPOINT_TYPE ?= " "
TEXTTOSPEECH_SPEECHRATE ?= " "
TEXTTOSPEECH_SATPLUGINCALLSIGN ?= " "
TEXTTOSPEECH_VOICE_FOR_EN ?= "carol"
TEXTTOSPEECH_VOICE_FOR_ES ?= "Angelica"
TEXTTOSPEECH_VOICE_FOR_FR ?= "amelie"
TEXTTOSPEECH_VOICE_FOR_GB ?= "en-GB-Standard-N"
TEXTTOSPEECH_VOICE_FOR_DE ?= "de-DE-Standard-G"
TEXTTOSPEECH_VOICE_FOR_IT ?= "it-IT-Standard-E"
TEXTTOSPEECH_LOCALVOICE_FOR_EN ?= " "
TEXTTOSPEECH_LOCALVOICE_FOR_ES ?= " "
TEXTTOSPEECH_LOCALVOICE_FOR_FR ?= " "
TEXTTOSPEECH_LOCALVOICE_FOR_GB ?= " "
TEXTTOSPEECH_LOCALVOICE_FOR_DE ?= " "
TEXTTOSPEECH_LOCALVOICE_FOR_IT ?= " "

#Below changes needs to be migrated to rdke-region-us-config 
TEXTTOSPEECH_LOCAL_ENDPOINT = "${@bb.utils.contains('DISTRO_FEATURES', 'flex2_rdk', 'http://localhost:50050/nuanceEve/tts?', '',d)}"
TEXTTOSPEECH_ENDPOINT_TYPE = "${@bb.utils.contains('DISTRO_FEATURES', 'flex2_rdk', 'TTS2', '',d)}"
TEXTTOSPEECH_SPEECHRATE =  "${@bb.utils.contains('DISTRO_FEATURES', 'flex2_rdk', 'medium', '',d)}"
TEXTTOSPEECH_SATPLUGINCALLSIGN =  "${@bb.utils.contains('DISTRO_FEATURES', 'flex2_rdk', 'org.rdk.AuthService', '',d)}"
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------

PACKAGECONFIG[texttospeech] = "-DPLUGIN_TEXTTOSPEECH=ON \
                               -DPLUGIN_MONITOR_TEXTTOSPEECH=ON \
                               -DPLUGIN_TEXTTOSPEECH_ENDPOINT="${TEXTTOSPEECH_ENDPOINT}" \
                               -DPLUGIN_TEXTTOSPEECH_SECURE_ENDPOINT="${TEXTTOSPEECH_SECURE_ENDPOINT}" \
                               -DPLUGIN_TEXTTOSPEECH_LOCAL_ENDPOINT="${TEXTTOSPEECH_LOCAL_ENDPOINT}" \
                               -DPLUGIN_TEXTTOSPEECH_LANGUAGE="${TEXTTOSPEECH_LANGUAGE}" \
                               -DPLUGIN_TEXTTOSPEECH_VOLUME="${TEXTTOSPEECH_VOLUME}" \
                               -DPLUGIN_TEXTTOSPEECH_RATE="${TEXTTOSPEECH_RATE}" \
                               -DPLUGIN_TEXTTOSPEECH_ENDPOINT_TYPE="${TEXTTOSPEECH_ENDPOINT_TYPE}" \
                               -DPLUGIN_TEXTTOSPEECH_SPEECHRATE="${TEXTTOSPEECH_SPEECHRATE}" \
                               -DPLUGIN_TEXTTOSPEECH_SATPLUGINCALLSIGN="${TEXTTOSPEECH_SATPLUGINCALLSIGN}" \
                               -DPLUGIN_TEXTTOSPEECH_VOICE_FOR_EN="${TEXTTOSPEECH_VOICE_FOR_EN}" \
                               -DPLUGIN_TEXTTOSPEECH_VOICE_FOR_ES="${TEXTTOSPEECH_VOICE_FOR_ES}" \
                               -DPLUGIN_TEXTTOSPEECH_VOICE_FOR_FR="${TEXTTOSPEECH_VOICE_FOR_FR}" \
                               -DPLUGIN_TEXTTOSPEECH_VOICE_FOR_GB="${TEXTTOSPEECH_VOICE_FOR_GB}" \
                               -DPLUGIN_TEXTTOSPEECH_VOICE_FOR_DE="${TEXTTOSPEECH_VOICE_FOR_DE}" \
                               -DPLUGIN_TEXTTOSPEECH_VOICE_FOR_IT="${TEXTTOSPEECH_VOICE_FOR_IT}" \
                               -DPLUGIN_TEXTTOSPEECH_LOCALVOICE_FOR_EN="${TEXTTOSPEECH_LOCALVOICE_FOR_EN}" \
                               -DPLUGIN_TEXTTOSPEECH_LOCALVOICE_FOR_ES="${TEXTTOSPEECH_LOCALVOICE_FOR_ES}" \
                               -DPLUGIN_TEXTTOSPEECH_LOCALVOICE_FOR_FR="${TEXTTOSPEECH_LOCALVOICE_FOR_FR}" \
                               -DPLUGIN_TEXTTOSPEECH_LOCALVOICE_FOR_GB="${TEXTTOSPEECH_LOCALVOICE_FOR_GB}" \
                               -DPLUGIN_TEXTTOSPEECH_LOCALVOICE_FOR_DE="${TEXTTOSPEECH_LOCALVOICE_FOR_DE}" \
                               -DPLUGIN_TEXTTOSPEECH_LOCALVOICE_FOR_IT="${TEXTTOSPEECH_LOCALVOICE_FOR_IT}" \
                               , -DPLUGIN_TEXTTOSPEECH=OFF, \
                               virtual/vendor-systemaudioplatform gstreamer1.0, \
                               virtual/vendor-systemaudioplatform gstreamer1.0"

# ----------------------------------------------------------------------------
