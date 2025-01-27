#!/bin/bash
#
#GENERIC VERSION
echo "launching miracastapp"

export LD_LIBRARY_PATH=/package/usr/lib:/usr/lib/plugins/platforms:$LD_LIBRARY_PATH
export THUNDER_ACCESS="100.64.11.1:9998"
mkdir -p /home/private/miracastapp
export WESTEROS_SINK_AMLOGIC_USE_DMABUF=1
export PLAYERSINKBIN_USE_WESTEROSSINK=1
export WESTEROS_GL_USE_AMLOGIC_AVSYNC=1
export WESTEROS_SINK_USE_FREERUN=1
export WESTEROS_GL_USE_REFRESH_LOCK=1
export GST_REGISTRY="/opt/.gstreamer/registry.bin"
exec /package/skyMiracastApplication 2>&1
