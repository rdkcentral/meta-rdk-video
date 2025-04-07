#!/bin/bash

# Source device properties (if exists)
[ -f /etc/device.properties ] && . /etc/device.properties || echo "Warning: /etc/device.properties not found."

# Set RUST_LOG based on BUILD_TYPE
if [[ "$BUILD_TYPE" == "vbn" || "$BUILD_TYPE" == "dev" ]]; then
  systemctl set-environment RUST_LOG=Debug
else
  systemctl set-environment RUST_LOG=INFO
fi

# Execute /opt/ripple_pre.sh if applicable
[[ "$BUILD_TYPE" != "prod" && -f /opt/ripple_pre.sh ]] && source /opt/ripple_pre.sh

# Set/unset DEVICE_PLATFORM based on RDK_PROFILE
if [[ "$RDK_PROFILE" == "TV" ]]; then
  systemctl set-environment DEVICE_PLATFORM=tv
fi

