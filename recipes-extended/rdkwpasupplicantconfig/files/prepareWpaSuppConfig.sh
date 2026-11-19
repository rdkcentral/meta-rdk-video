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
WPA_SUPP_ARGS=""
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
        # Terminate a last line that has no trailing newline so the new entry is not joined to it
        if [ -s "$WPA_SUPP_CONF_FILE" ] && [ -n "`tail -c 1 "$WPA_SUPP_CONF_FILE"`" ]; then
            echo >> "$WPA_SUPP_CONF_FILE"
        fi
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

    log "$WPA_SUPP_CONF_FILE file exists, removing network blocks and unsupported entries"
    sed -i -e '/^[[:space:]]*network={/,/^[[:space:]]*}[[:space:]]*$/d' \
           -e '/^[[:space:]]*update_config=/d' \
           -e '/^[[:space:]]*roaming_enable=/d' \
           -e '/^[[:space:]]*kvr_enable=/d' \
           -e '/^[[:space:]]*$/d' "$WPA_SUPP_CONF_FILE"
else
    log "$WPA_SUPP_CONF_FILE file is missing. Creating file and updating configurations..."
    : > "$WPA_SUPP_CONF_FILE"
fi
set_conf_param "country" "$COUNTRY_CODE"
set_conf_param "pmf" "$PMF_CONFIG"
set_conf_param "wowlan_triggers" "$WOWLAN_TRIGGERS"

# Log level is -d by default; on non-prod builds /opt/wpa_supplicant.logging with TRACE raises it to -dd
LOG_LEVEL_STR="-d"
if [ -f /opt/wpa_supplicant.logging ] && [ "$BUILD_TYPE" != "prod" ]; then
    DEBUGINIFILE=/opt/wpa_supplicant.logging
    log_line=`grep "LOG.RDK.WIFIWPA" "$DEBUGINIFILE"`
    case "$log_line" in
        *TRACE*) LOG_LEVEL_STR="-dd" ;;
    esac
fi
log "wpa_supplicant log level set to $LOG_LEVEL_STR"

# -u enables the D-Bus control interface (fi.w1.wpa_supplicant1) required by Type=dbus
/bin/systemctl set-environment WPA_SUPP_CONF_FILE=$WPA_SUPP_CONF_FILE
/bin/systemctl set-environment WPA_SUPP_ARGS="-Dnl80211 -c $WPA_SUPP_CONF_FILE -i $WIFI_INTERFACE -P $WPA_SUPP_PID_FILE -t -u $LOG_LEVEL_STR"
