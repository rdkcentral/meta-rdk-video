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
LOG_LEVEL_STR=""
WPA_SUPP_PID_FILE="/var/run/wpa_supplicant/wlan0.pid"
WPA_SUPP_CONF_DIR="/opt/secure/wifi"
WPA_SUPP_CONF_FILE=$WPA_SUPP_CONF_DIR/wpa_supplicant.conf
WPA_SUPP_CTRL_INTERFACE="/var/run/wpa_supplicant"
# Protected Management Frames - 0-Disabled, 1-Optional, 2-Required
PMF_CONFIG=1
WOWLAN_TRIGGERS="any"
THIS_SCRIPT=`basename "$0"`
# Alpha-2 country code, substituted at build time from the regional config
COUNTRY_CODE="@WIFI_COUNTRY_CODE@"

log ()
{
    echo "`/bin/timestamp` [$THIS_SCRIPT]: $*" >> /opt/logs/wpa_supplicant.log
}

# Set KEY=VALUE in the conf file, replacing an existing entry or appending a new one
set_conf_param ()
{
    if grep -q "^$1=" "$WPA_SUPP_CONF_FILE"; then
        sed -i "s/^$1=.*/$1=$2/" "$WPA_SUPP_CONF_FILE"
    else
        echo "$1=$2" >> "$WPA_SUPP_CONF_FILE"
    fi
    log "Set $1=$2 in $WPA_SUPP_CONF_FILE"
}

while ! mkdir -p "$WPA_SUPP_CONF_DIR" > /dev/null 2>&1; do
    log "Unable to create dir $WPA_SUPP_CONF_DIR"
    sleep 3
done

if [ -f "$WPA_SUPP_CONF_FILE" ]; then
    if ! systemctl is-active securemount.service; then
        log "TELEMETRY_WIFI_SECUREMOUNT_SERVICE_NOT_ACTIVE"
    fi

    log "$WPA_SUPP_CONF_FILE file exists and configurations are present"
    # Strip entries that conflict with the command line args or are invalid for this wpa_supplicant
    sed -i "/ctrl_interface/d" $WPA_SUPP_CONF_FILE
    sed -i "/bssid=/d" $WPA_SUPP_CONF_FILE
    sed -i "s/key_mgmt=OPEN/key_mgmt=NONE/g" $WPA_SUPP_CONF_FILE
    sed -i "/update_config/d" $WPA_SUPP_CONF_FILE
    sed -i '/roaming_enable/d' $WPA_SUPP_CONF_FILE
    sed -i '/kvr_enable/d' $WPA_SUPP_CONF_FILE
    if grep -q 'psk="' $WPA_SUPP_CONF_FILE; then      # conf has passphrase
        sed -i 's/key_mgmt=[ \t]*\(WPA-PSK\|SAE\|WPA-PSK[ \t]*SAE\)[ \t]*$/key_mgmt=WPA-PSK WPA-PSK-SHA256 SAE/g' $WPA_SUPP_CONF_FILE
    elif grep -q 'psk=[^"]' $WPA_SUPP_CONF_FILE; then # conf has psk
        sed -i 's/key_mgmt=[ \t]*WPA-PSK[ \t]*$/key_mgmt=WPA-PSK WPA-PSK-SHA256/g' $WPA_SUPP_CONF_FILE
    fi
    grep -q "key_mgmt=NONE" $WPA_SUPP_CONF_FILE || sed -i "/auth_alg/d" $WPA_SUPP_CONF_FILE
    sed -i "/ieee80211w/d" $WPA_SUPP_CONF_FILE
    sed -i "/sae_password/d" $WPA_SUPP_CONF_FILE

    if grep -q "^country=$COUNTRY_CODE$" $WPA_SUPP_CONF_FILE; then
        log "Country code $COUNTRY_CODE is present, No need to change"
    elif grep -q "^country=" $WPA_SUPP_CONF_FILE; then
        log "Updating country code to $COUNTRY_CODE"
        sed -i "s/^country=.*/country=$COUNTRY_CODE/" $WPA_SUPP_CONF_FILE
    elif [ ! -s "$WPA_SUPP_CONF_FILE" ]; then
        echo "country=$COUNTRY_CODE" > "$WPA_SUPP_CONF_FILE"
    else
        sed -i "1icountry=$COUNTRY_CODE" $WPA_SUPP_CONF_FILE
    fi
else
    log "$WPA_SUPP_CONF_FILE file is missing. Creating file and updating configurations..."
    echo "country=$COUNTRY_CODE" > $WPA_SUPP_CONF_FILE
fi
set_conf_param "pmf" "$PMF_CONFIG"
set_conf_param "wowlan_triggers" "$WOWLAN_TRIGGERS"

# Get wpa_supplicant.logging file with opt-override support
if [ -f /opt/wpa_supplicant.logging ] && [ "$BUILD_TYPE" != "prod" ]; then
    DEBUGINIFILE=/opt/wpa_supplicant.logging
else
    DEBUGINIFILE=/etc/wpa_supplicant.logging
fi
# Only -d (default) and -dd (TRACE/TRACE9 override) are supported
log_line=`grep "LOG.RDK.WIFIWPA" $DEBUGINIFILE`
case "$log_line" in
    *TRACE*) LOG_LEVEL_STR="-dd" ;;
    *)       LOG_LEVEL_STR="-d" ;;
esac
log "wpa_supplicant log level set to $LOG_LEVEL_STR"

# -u enables the D-Bus control interface (fi.w1.wpa_supplicant1) required by Type=dbus
/bin/systemctl set-environment WPA_SUPP_CONF_FILE=$WPA_SUPP_CONF_FILE
/bin/systemctl set-environment WPA_SUPP_ARGS="-Dnl80211 -O $WPA_SUPP_CTRL_INTERFACE -c $WPA_SUPP_CONF_FILE -i $WIFI_INTERFACE -P $WPA_SUPP_PID_FILE -t -u $LOG_LEVEL_STR"