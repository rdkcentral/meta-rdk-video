# Temporary TTS build troubleshooting; accessibility pulls in TTS via rdkat.
PACKAGECONFIG:remove = "accessibility speechsynthesis"

# WPE 2.46 forces USE_ATSPI on; omit its TTS integration when rdkat is disabled.
SRC_URI:remove = "file://2.46/comcast-RDK-56287-rdkat-atspi2.patch"
