#!/bin/sh

# If not stated otherwise in this file or this component's Licenses.txt file the
# following copyright and licenses apply:
#
# Copyright 2018 RDK Management
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

. /etc/device.properties
DEBUGINIFILE=""
LOG_LEVEL_STR="-d"
WPA_SUPP_PID_FILE="/var/run/wpa_supplicant/wlan0.pid"
WPA_SUPP_CONF_DIR="/opt/secure/wifi"
WPA_SUPP_CONF_FILE=$WPA_SUPP_CONF_DIR/wpa_supplicant.conf
WPA_SUPP_CTRL_INTERFACE="/run/wpa_supplicant"
WPA_SUPP_ARGS=""
# Protected Management Frames - 0-Disabled, 1-Optional, 2-Required
PMF_CONFIG=1
THIS_SCRIPT=`basename "$0"`

log ()
{
    echo "`/bin/timestamp` [$THIS_SCRIPT]: $*" >> /opt/logs/wpa_supplicant.log
}

if [ ! -d $WPA_SUPP_CONF_DIR ]; then
    log "WIFI DIRECTORY NOT THERE!"
fi

while ! mkdir -p "$WPA_SUPP_CONF_DIR" &> /dev/null;do
    log "Unable to create dir $WPA_SUPP_CONF_DIR"
    sleep 3
done

# Fetch alpha-2 country code from driver using iw
COUNTRY_CODE=$(sed -rn 's/.*country ([A-Z]{2}):.*/\1/p' <<< `iw reg get`)
if [ -z "$COUNTRY_CODE" ]; then
    log "Failed to get country code from iw, trying to fetch using wl"
    COUNTRY_CODE=`wl country | cut -f 1 -d " "`
    if [ -z "$COUNTRY_CODE" ]
    then
        log "Country code is still empty from wl, Lets set it to default US"
        COUNTRY_CODE="US"
    fi
fi
log "Setting WiFi Regulatory domain to $COUNTRY_CODE."
# Generate wpa_supplicant.conf, retaining the saved network blocks
touch $WPA_SUPP_CONF_FILE
sed -i -e "/^ctrl_interface=/d" -e "/^pmf=/d" -e "/^wowlan_triggers=/d" $WPA_SUPP_CONF_FILE
echo "pmf=$PMF_CONFIG" >> $WPA_SUPP_CONF_FILE
echo "wowlan_triggers=any" >> $WPA_SUPP_CONF_FILE
if grep "country=" "$WPA_SUPP_CONF_FILE"; then
    log "Country code is present , No need to change"
else
    echo "country=$COUNTRY_CODE" >> $WPA_SUPP_CONF_FILE
fi
# Configuring wpa_supplicant log levels
# Get wpa_supplicant.logging file with opt-override support
if [ -f /opt/wpa_supplicant.logging ]  && [ "$BUILD_TYPE" != "prod" ]; then
   DEBUGINIFILE=/opt/wpa_supplicant.logging
else
   DEBUGINIFILE=/etc/wpa_supplicant.logging
fi
#Read debug.ini file and map to wpa-supplicant logging level
log_line=`grep "LOG.RDK.WIFIWPA" $DEBUGINIFILE`
if [[ "$log_line" =~ "TRACE" ]]; then
    LOG_LEVEL_STR="-dd"
fi
/bin/systemctl set-environment WPA_SUPP_CONF_FILE=$WPA_SUPP_CONF_FILE
/bin/systemctl set-environment WPA_SUPP_ARGS="-Dnl80211 -c $WPA_SUPP_CONF_FILE -i $WIFI_INTERFACE -P $WPA_SUPP_PID_FILE -O $WPA_SUPP_CTRL_INTERFACE -t -u $LOG_LEVEL_STR"

