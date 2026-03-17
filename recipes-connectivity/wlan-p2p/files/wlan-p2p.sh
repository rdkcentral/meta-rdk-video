#!/bin/sh
##########################################################################
# If not stated otherwise in this file or this component's LICENSE
# file the following copyright and licenses apply:
#
# Copyright 2016 RDK Management
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
##########################################################################
. /etc/device.properties

WPA_P2P_SUPP_CONF_DIR="/opt/secure/wifi/p2p"
WPA_P2P_SUPP_CONF_FILE=$WPA_P2P_SUPP_CONF_DIR/wpa_supplicant.conf
LOG_LEVEL_STR=""
DEBUGINIFILE=""
WPA_P2P_SUPP_ARGS=""

mkdir -p ${WPA_P2P_SUPP_CONF_DIR}

if [ ! -f ${WPA_P2P_SUPP_CONF_FILE} ]; then
   cp /etc/wifi_p2p/wpa_supplicant.conf $WPA_P2P_SUPP_CONF_FILE
fi
sync

# Select debug.ini location
if [ -f /opt/debug.ini ] && [ "$BUILD_TYPE" != "prod" ]; then
    DEBUGINIFILE=/opt/debug.ini
else
    DEBUGINIFILE=/etc/debug.ini
fi

# Map RDK log levels to wpa_supplicant
log_line=`grep "LOG.RDK.WIFIP2PWPA" $DEBUGINIFILE`

if echo "$log_line" | grep -q "TRACE9"; then
    LOG_LEVEL_STR="-ddd"
elif echo "$log_line" | grep -q "TRACE"; then
    LOG_LEVEL_STR="-dd"
elif echo "$log_line" | grep -q "DEBUG"; then
    LOG_LEVEL_STR="-d"
elif echo "$log_line" | grep -q "INFO"; then
    LOG_LEVEL_STR=""
elif echo "$log_line" | grep -q "WARNING"; then
    LOG_LEVEL_STR="-q"
elif echo "$log_line" | grep -q "ERROR"; then
    LOG_LEVEL_STR="-qq"
fi

# Broadcom specific p2p interface
if ip link show wl0.2 >/dev/null 2>&1; then
    echo "Broadcom platform detected"

    WIFI_P2P_INTERFACE="wl0.2"
echo "Using P2P interface: $WIFI_P2P_INTERFACE"

WPA_SUPP_P2P_PID_FILE="/var/run/wpa_supplicant/p2p.pid"

WPA_P2P_SUPP_ARGS=" -Dnl80211 -c $WPA_P2P_SUPP_CONF_FILE -i $WIFI_P2P_INTERFACE -t $LOG_LEVEL_STR -P $WPA_SUPP_P2P_PID_FILE"

else
# Default generic configuration for other platforms
    WPA_P2P_SUPP_ARGS=" -Dnl80211 -c $WPA_P2P_SUPP_CONF_FILE -i $WIFI_P2P_CTRL_INTERFACE -t $LOG_LEVEL_STR"

fi
# Export systemd environment 
/bin/systemctl set-environment WPA_P2P_SUPP_CONF_FILE=$WPA_P2P_SUPP_CONF_FILE
/bin/systemctl set-environment WPA_P2P_SUPP_ARGS="$WPA_P2P_SUPP_ARGS"

exit 0
