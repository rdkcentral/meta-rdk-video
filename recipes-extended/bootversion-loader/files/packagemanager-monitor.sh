#!/bin/sh

set -e

LOG_FILE="/opt/logs/packagemanager.log"

if ! command -v appsservicectl >/dev/null 2>&1; then
    echo "Error: appsservicectl is not available in PATH." >&2
    exit 1
fi

mkdir -p /opt/logs

while true; do
    {
        timestamp=$(date '+%Y-%m-%d %H:%M:%S %z')
        printf '\n=== %s ===\n' "$timestamp"
        if appsservicectl dumpsys packagemanger; then
            :
        else
            status=$?
            echo "Error: appsservicectl exited with status $status." >&2
        fi
    } >> "$LOG_FILE" 2>&1
    sleep 30
done
