#!/bin/bash
# Script to populate /opt/logs/version.txt with appgateway, appmanager, and cpc versions
# Preserves existing system version info and updates only the plugin versions
# Prevents duplication on service restarts

VERSION_FILE="/opt/logs/version.txt"
APPGATEWAY_VERSION="/etc/appgatewayversion.txt"
APPMANAGER_VERSION="/etc/appmanagersversion.txt"
APPGATEWAY_CPC_VERSION="/etc/appgatewaycpcversion.txt"

# Read BUILD_TYPE from device properties (if exists)
if [ -f /etc/device.properties ]; then
    BUILD_TYPE=$(grep -m 1 "^BUILD_TYPE=" /etc/device.properties | cut -d '=' -f 2- | sed 's/^[[:space:]]*//; s/[[:space:]]*$//; s/^"//; s/"$//')
else
    echo "Warning: /etc/device.properties not found."
fi

# Populate appinfra component versions into /opt/logs/version.txt only for vbn/dev builds
if [[ "$BUILD_TYPE" == "vbn" || "$BUILD_TYPE" == "dev" ]]; then
    mkdir -p "$(dirname "$VERSION_FILE")"

    # Read existing content and remove plugin version lines (to avoid duplication)
    # Keep only system version info
    if [ -f "$VERSION_FILE" ]; then
        # Extract lines that are NOT plugin versions
        # Remove lines starting with APP_GATEWAY, APP_GATEWAY_CPC, or APP_MANAGERS
        temp_file=$(mktemp)
        grep -Ev "^(APP_GATEWAY(_CPC)?|APP_MANAGERS)" "$VERSION_FILE" > "$temp_file" 2>/dev/null || true
        
        # Overwrite content in place to preserve existing file mode/ownership
        cat "$temp_file" > "$VERSION_FILE"
        rm -f "$temp_file"
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
    if [ -f "$APPGATEWAY_CPC_VERSION" ]; then
        cat "$APPGATEWAY_CPC_VERSION" >> "$VERSION_FILE"
    fi

fi

# Always exit with 0 to not block service start
exit 0
