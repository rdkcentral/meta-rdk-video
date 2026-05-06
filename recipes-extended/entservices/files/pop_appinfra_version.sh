#!/bin/bash
# Script to populate /opt/logs/version.txt with appgateway, appmanager, and cpc versions
# Preserves existing system version info and updates only the plugin versions
# Prevents duplication on service restarts

VERSION_FILE="/opt/logs/version.txt"
APPGATEWAY_VERSION="/etc/appgatewayversion.txt"
APPMANAGER_VERSION="/etc/appmanagersversion.txt"
CPC_VERSION="/etc/appgatewaycpcversion.txt"
DEVICE_PROPERTIES="/etc/device.properties"

# Create /opt/logs directory if it doesn't exist
mkdir -p "$(dirname "$VERSION_FILE")"

# Read existing content and remove plugin version lines (to avoid duplication)
# Keep only system version info
if [ -f "$VERSION_FILE" ]; then
    # Extract lines that are NOT plugin versions
    # Remove lines starting with APP_GATEWAY, APP_MANAGERS, APP_GATEWAY_CPC
    temp_file=$(mktemp)
    grep -v "^APP_GATEWAY\|^APP_MANAGERS\|^APP_GATEWAY_CPC" "$VERSION_FILE" > "$temp_file" 2>/dev/null || true
    
    # Overwrite the original file with cleaned content
    mv "$temp_file" "$VERSION_FILE"
fi

# Append appgateway version if it exists
if [ -f "$APPGATEWAY_VERSION" ]; then
    cat "$APPGATEWAY_VERSION" >> "$VERSION_FILE"
fi

# Append appmanager version if it exists
if [ -f "$APPMANAGER_VERSION" ]; then
    cat "$APPMANAGER_VERSION" >> "$VERSION_FILE"
fi

# Append CPC version if it exists
if [ -f "$CPC_VERSION" ]; then
    cat "$CPC_VERSION" >> "$VERSION_FILE"
fi

# Source device properties (if exists)
if [ -f "$DEVICE_PROPERTIES" ]; then
    . "$DEVICE_PROPERTIES"
else
    echo "Warning: $DEVICE_PROPERTIES not found."
fi

# Set RUST_LOG based on BUILD_TYPE
if [[ "$BUILD_TYPE" == "vbn" || "$BUILD_TYPE" == "dev" ]]; then
    systemctl set-environment RUST_LOG=Debug
fi

# Always exit with 0 to not block service start
exit 0
