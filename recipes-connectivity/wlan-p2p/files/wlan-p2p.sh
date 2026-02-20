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

mkdir -p ${WPA_P2P_SUPP_CONF_DIR}

if [ ! -f ${WPA_P2P_SUPP_CONF_FILE} ];then
   cp /etc/wifi_p2p/wpa_supplicant.conf $WPA_P2P_SUPP_CONF_FILE
fi
sync

# Configuring wpa_supplicant log levels
# Get debug.ini file with opt-override support
if [ -f /opt/debug.ini ]  && [ "$BUILD_TYPE" != "prod" ]; then
	DEBUGINIFILE=/opt/debug.ini
else
	DEBUGINIFILE=/etc/debug.ini
fi

#Read debug.ini file and map to wpa-supplicant logging level
log_line=`grep "LOG.RDK.WIFIP2PWPA" $DEBUGINIFILE`

if [[ "$log_line" =~ "TRACE9" ]]; then
	LOG_LEVEL_STR="-ddd"
elif [[ "$log_line" =~ "TRACE" ]]; then
	LOG_LEVEL_STR="-dd"
elif [[ "$log_line" =~ "DEBUG" ]]; then
	LOG_LEVEL_STR="-d"
elif [[ "$log_line" =~ "INFO" ]]; then
	LOG_LEVEL_STR=""
elif [[ "$log_line" =~ "WARNING" ]]; then
	LOG_LEVEL_STR="-q"
elif [[ "$log_line" =~ "ERROR" ]]; then
	LOG_LEVEL_STR="-qq"
fi

/bin/systemctl set-environment WPA_P2P_SUPP_CONF_FILE=$WPA_P2P_SUPP_CONF_FILE
/bin/systemctl set-environment WPA_P2P_SUPP_ARGS=" -Dnl80211 -c $WPA_P2P_SUPP_CONF_FILE -i $WIFI_P2P_CTRL_INTERFACE -t $LOG_LEVEL_STR"

exit 0
