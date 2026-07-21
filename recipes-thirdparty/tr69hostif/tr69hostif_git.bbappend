# DS_COMRPC migration: complete removal of libds (devicesettings) from tr69hostif.
#
# --enable-thunder activates WITH_THUNDER_CLIENT in Makefile.am which:
#   1. Compiles *_Thunder.cpp files INSTEAD of the libds-based STBService files:
#        Components_AudioOutput_Thunder.cpp, Components_SPDIF_Thunder.cpp,
#        Components_HDMI_Thunder.cpp, Components_DisplayDevice_Thunder.cpp,
#        Components_VideoOutput_Thunder.cpp, Components_VideoDecoder_Thunder.cpp,
#        Capabilities_Thunder.cpp
#        — all zero device:: calls, no -lds link needed
#   2. Defines USE_THUNDER_CLIENT so hostIf_dsClient_ReqHandler.cpp and
#        Device_DeviceInfo.cpp skip their device:: blocks
#   3. Does NOT add -I/rdk/ds/ or -I/rdk/ds-hal/ include paths to CFLAGS
#
# No #if 0 source patching needed — upstream already migrated the code.
# At runtime, Thunder STBService parameters return errors gracefully if
# the Thunder DS service is unavailable (acceptable — not being validated).
#
# Rollback: delete this file.

EXTRA_OECONF:append = " --enable-thunder"
DEPENDS:remove = "devicesettings virtual/vendor-devicesettings-hal"
RDEPENDS:${PN}:remove = "devicesettings"

