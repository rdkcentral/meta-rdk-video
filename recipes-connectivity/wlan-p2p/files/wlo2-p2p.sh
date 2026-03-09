#!/bin/sh
# Broadcom Miracast helper for wlan-p2p.service
# - Applies Broadcom-only overrides 
# - Prefers WIFI_P2P_CTRL_INTERFACE=p2p-dev-wl0.2 
#   falls back to wl0.2 if p2p-dev is not available yet
# - Sets wl0.2 unmanaged in NetworkManager (ExecStartPre)

set -eu

# --- Load device properties if available ---
if [ -r /etc/device.properties ]; then
    . /etc/device.properties || true
fi

is_brcm=""
case "${WIFI_VENDOR:-}${WIFI_CHIPSET:-}${WIFI_BRCM:-}${WIFI_DEVICE_VENDOR:-}" in
  *broadcom*|*brcm*|*BROADCOM*|*BRCM*) is_brcm="1" ;;
esac
if [ -e /sys/class/net/wl0.2 ]; then
    is_brcm="1"
fi
[ -n "$is_brcm" ] || exit 0

# --- Data and control interfaces ---
WIFI_P2P_DATA_INTERFACE="${WIFI_P2P_DATA_INTERFACE:-wl0.2}"

# 1) device.properties if set (e.g., WIFI_P2P_CTRL_INTERFACE=p2p-dev-wl0.2)
# 2) Else, detect p2p-dev-wl0.2 via `iw dev`
# 3) Else, fallback to wl0.2
CTRL_IF_CANDIDATE="${WIFI_P2P_CTRL_INTERFACE:-}"
if [ -z "$CTRL_IF_CANDIDATE" ] && command -v iw >/dev/null 2>&1; then
    if iw dev 2>/dev/null | awk '/Interface/ {print $2}' | grep -q '^p2p-dev-wl0\.2$'; then
        CTRL_IF_CANDIDATE="p2p-dev-wl0.2"
    fi
fi
WIFI_P2P_CTRL_INTERFACE="${CTRL_IF_CANDIDATE:-$WIFI_P2P_DATA_INTERFACE}"

WPA_P2P_SUPP_ARGS=""

WPA_SUPP_P2P_PID_FILE="/var/run/wpa_supplicant/p2p.pid"

/bin/systemctl set-environment WIFI_P2P_DATA_INTERFACE="$WIFI_P2P_DATA_INTERFACE"
if [ -z "${WPA_P2P_SUPP_CONF_FILE:-}" ]; then
    WPA_P2P_SUPP_CONF_FILE="/opt/secure/wifi/p2p/wpa_supplicant.conf"
fi
/bin/systemctl set-environment WPA_P2P_SUPP_CONF_FILE="$WPA_P2P_SUPP_CONF_FILE"

LOG_LEVEL_STR="${LOG_LEVEL_STR:-}"

# If we are using a p2p-dev- control interface, add use_p2p_device=1 to params
PARAMS_EXTRA=""
case "$WIFI_P2P_CTRL_INTERFACE" in
  p2p-dev-*) PARAMS_EXTRA=" -puse_p2p_device=1" ;;
esac

WPA_P2P_SUPP_ARGS=" -Dnl80211 -c $WPA_P2P_SUPP_CONF_FILE -i $WIFI_P2P_DATA_INTERFACE -t $LOG_LEVEL_STR -P $WPA_SUPP_P2P_PID_FILE$PARAMS_EXTRA"
/bin/systemctl set-environment WPA_P2P_SUPP_ARGS="$WPA_P2P_SUPP_ARGS"

echo "wl02-p2p.sh: CTRL_IF=$WIFI_P2P_CTRL_INTERFACE DATA_IF=$WIFI_P2P_DATA_INTERFACE LOG='$LOG_LEVEL_STR' EXTRA='$PARAMS_EXTRA'"

# --- Keep wl0.2 unmanaged in NetworkManager (Pre) ---
if command -v nmcli >/dev/null 2>&1; then
    if ip link show "$WIFI_P2P_DATA_INTERFACE" >/dev/null 2>&1; then
        nmcli device set "$WIFI_P2P_DATA_INTERFACE" managed no || true
    fi
fi

if ! grep -q '^ctrl_interface=' "$WPA_P2P_SUPP_CONF_FILE" 2>/dev/null; then
    echo "ctrl_interface=/var/run/wpa_supplicant" >> "$WPA_P2P_SUPP_CONF_FILE" || true
fi
if ! grep -q '^update_config=' "$WPA_P2P_SUPP_CONF_FILE" 2>/dev/null; then
    echo "update_config=1" >> "$WPA_P2P_SUPP_CONF_FILE" || true
fi

if ! grep -q '^p2p_disabled=' "$WPA_P2P_SUPP_CONF_FILE" 2>/dev/null; then
    echo "p2p_disabled=0" >> "$WPA_P2P_SUPP_CONF_FILE" || true
fi

exit 0

