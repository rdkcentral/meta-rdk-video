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
WPA_SUPP_ARGS=""
WPA_P2P_SUPP_CONF_DIR="/opt/secure/wifi/p2p"
WPA_P2P_SUPP_CONF_FILE=$WPA_P2P_SUPP_CONF_DIR/wpa_supplicant.conf
WPA_P2P_SUPP_ARGS=""
# Protected Management Frames - 0-Disabled, 1-Optional, 2-Required
PMF_CONFIG=1
THIS_SCRIPT=`basename "$0"`

log ()
{
    echo "`/bin/timestamp` [$THIS_SCRIPT]: $*" >> /opt/logs/wpa_supplicant.log
}

do_configure_p2p_wpa_args ()
{
	if [ ! -f /etc/wifi_p2p/wpa_supplicant.conf ];then
		log "ERROR: default p2p conf file /etc/wifi_p2p/wpa_supplicant.conf not present"
	else
        	mkdir -p ${WPA_P2P_SUPP_CONF_DIR}

		if [ ! -f ${WPA_P2P_SUPP_CONF_FILE} ];then
			cp /etc/wifi_p2p/wpa_supplicant.conf $WPA_P2P_SUPP_CONF_FILE
		fi
                WPA_P2P_SUPP_ARGS=" -Dnl80211 -c $WPA_P2P_SUPP_CONF_FILE -i $WIFI_P2P_CTRL_INTERFACE -N"
                sync
	fi
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
# Generate wpa_supplicant.conf
# 1. If file is not present create one and fill it with ctrl_interface & update_config values
# 2. If file is present and ctrl_interface or update_config values are not present recreate files with proper values
# 3. If file is present and values are proper do Nothing, Use the same
if [ -f $WPA_SUPP_CONF_FILE ]; then
    if ! systemctl is-active securemount.service; then
        log "TELEMETRY_WIFI_SECUREMOUNT_SERVICE_NOT_ACTIVE"
    elif [ ! -d /opt/secure ]; then
        log "TELEMETRY_WIFI_NO_SECURE_DIRECTORY /opt/secure"
    elif [ ! -f /opt/secure/wifi/wpa_supplicant.conf ]; then
        log "TELEMETRY_WIFI_WPA_SUPPLICANT_NO_SUCH_FILE /opt/secure/wifi/wpa_supplicant.conf"
    elif ! cmp "$WPA_SUPP_CONF_FILE" /opt/secure/wifi/wpa_supplicant.conf; then
        log "TELEMETRY_WIFI_WPA_SUPPLICANT_CONF_FILES_DIFFER $WPA_SUPP_CONF_FILE /opt/secure/wifi/wpa_supplicant.conf"
    fi
    if [[ `grep "ctrl_interface=/var/run/wpa_supplicant" $WPA_SUPP_CONF_FILE` ]] && [[ `grep "update_config=1" $WPA_SUPP_CONF_FILE` ]]; then
        log "$WPA_SUPP_CONF_FILE file exists and configurations are present"
        sed -i "/bssid=/d" $WPA_SUPP_CONF_FILE
        sed -i "s/key_mgmt=OPEN/key_mgmt=NONE/g" $WPA_SUPP_CONF_FILE
        if grep "country=" "$WPA_SUPP_CONF_FILE"; then
            log "Country code is present , No need to change"
        else
            sed -i "2acountry=$COUNTRY_CODE" $WPA_SUPP_CONF_FILE
        fi
    else
        log "$WPA_SUPP_CONF_FILE file exists, Updating missing configurations..."
        echo "ctrl_interface=/var/run/wpa_supplicant" > $WPA_SUPP_CONF_FILE
        echo "update_config=1" >> $WPA_SUPP_CONF_FILE
        echo "country=$COUNTRY_CODE" >> $WPA_SUPP_CONF_FILE
    fi
    # Enable 802.11w on wpa_supplicant
    if [[ `grep "pmf=" $WPA_SUPP_CONF_FILE` ]]; then
        echo "PMF is already set in config file"
    else
        echo "pmf=$PMF_CONFIG" >> $WPA_SUPP_CONF_FILE
        echo "PMF is enabled and set to mode "optional"."
    fi
    #Delete roaming_enable and kvr_enable in wpa_supplicant.conf as RDKE doesn't support
    sed -i '/roaming_enable/d' $WPA_SUPP_CONF_FILE
    sed -i '/kvr_enable/d' $WPA_SUPP_CONF_FILE
    #Update key_mgmt in wpa_supplicant.conf
    if grep -q 'psk="' $WPA_SUPP_CONF_FILE; then      # conf has passphrase
        sed -i 's/key_mgmt=[ \t]*\(WPA-PSK\|SAE\|WPA-PSK[ \t]*SAE\)[ \t]*$/key_mgmt=WPA-PSK WPA-PSK-SHA256 SAE/g' $WPA_SUPP_CONF_FILE
    elif grep -q 'psk=[^"]' $WPA_SUPP_CONF_FILE; then # conf has psk
        sed -i 's/key_mgmt=[ \t]*WPA-PSK[ \t]*$/key_mgmt=WPA-PSK WPA-PSK-SHA256/g' $WPA_SUPP_CONF_FILE
    fi
    grep -q "key_mgmt=NONE" $WPA_SUPP_CONF_FILE || sed -i "/auth_alg/d" $WPA_SUPP_CONF_FILE
    #Delete sae_password and ieee80211w
    sed -i "/ieee80211w/d" $WPA_SUPP_CONF_FILE
    sed -i "/sae_password/d" $WPA_SUPP_CONF_FILE
else
    log "$WPA_SUPP_CONF_FILE file is missing. Creating file and updating configurations..."
    echo "ctrl_interface=/var/run/wpa_supplicant" > $WPA_SUPP_CONF_FILE
    echo "update_config=1" >> $WPA_SUPP_CONF_FILE
    echo "country=$COUNTRY_CODE" >> $WPA_SUPP_CONF_FILE
    echo "pmf=$PMF_CONFIG" >> $WPA_SUPP_CONF_FILE
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
# Configuring wpa_supplicant args for P2P control interface
### do_configure_p2p_wpa_args
/bin/systemctl set-environment WPA_SUPP_CONF_FILE=$WPA_SUPP_CONF_FILE
/bin/systemctl set-environment WPA_SUPP_ARGS="${WPA_P2P_SUPP_ARGS} -Dnl80211 -c $WPA_SUPP_CONF_FILE -i $WIFI_INTERFACE -P $WPA_SUPP_PID_FILE -t -U -u $LOG_LEVEL_STR"

