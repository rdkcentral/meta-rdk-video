#!/bin/bash

FILE="/opt/persistent/wifi.interface.disable"

if [ -f "$FILE" ]; then
    echo "Disable file found. Stopping wpa_supplicant..."
    systemctl stop wpa_supplicant.service
else
    echo "Disable file removed. Starting wpa_supplicant..."
    systemctl start wpa_supplicant.service
fi
