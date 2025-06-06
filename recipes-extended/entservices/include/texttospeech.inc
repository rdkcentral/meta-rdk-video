# Provides the TextToSpeech plugin settings for WPEFramwork

TEXTTOSPEECH_ENDPOINT ?= " "
TEXTTOSPEECH_SECURE_ENDPOINT ?= " "
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

# ----------------------------------------------------------------------------

PACKAGECONFIG[texttospeech] = "-DPLUGIN_TEXTTOSPEECH=ON \
                               -DPLUGIN_MONITOR_TEXTTOSPEECH=ON \
                               -DPLUGIN_TEXTTOSPEECH_ENDPOINT="${TEXTTOSPEECH_ENDPOINT}" \
                               -DPLUGIN_TEXTTOSPEECH_SECURE_ENDPOINT="${TEXTTOSPEECH_SECURE_ENDPOINT}" \
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
                               , -DPLUGIN_TEXTTOSPEECH=OFF, \
                               virtual/vendor-systemaudioplatform gstreamer1.0, \
                               virtual/vendor-systemaudioplatform gstreamer1.0"

# ----------------------------------------------------------------------------
