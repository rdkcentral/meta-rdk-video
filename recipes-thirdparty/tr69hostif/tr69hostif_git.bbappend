# DS_COMRPC migration: partial devicesettings removal for tr69hostif.
#
# dsMgr daemon is masked (dsmgr.service -> /dev/null) so device::Manager::Initialize()
# will fail at runtime; all STBService component functions have existing try/catch
# blocks that return NOK on failure — DS TR-069 parameters return errors gracefully.
#
# libds.so is intentionally kept linked: the 8 STBService component files
# (Components_AudioOutput, VideoOutput, HDMI, DisplayDevice, SPDIF, VideoDecoder,
# Capabilities) have 50+ embedded device:: calls that require full source patching
# before the -lds link can be removed. That work is tracked separately.
#
# virtual/vendor-devicesettings-hal (the HAL DSO) is removed since we have no
# vendor HAL for XiOne-UK and it is not needed when dsMgr is masked.
#
# Rollback: delete this file.

DEPENDS:remove = "virtual/vendor-devicesettings-hal"

