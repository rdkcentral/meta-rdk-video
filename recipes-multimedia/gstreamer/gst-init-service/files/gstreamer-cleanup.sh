#!/bin/bash

# Copyright 2024 RDK Management
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
# This script checks if the GStreamer registry should be cleared based on
# firmware flash status and cleans it if necessary.

# Initialize variables safely
CDLFILE=""
PREV_CDLFILE=""
CUR_IMAGE=""

if [[ -f /opt/cdl_flashed_file_name ]]; then
    CDLFILE=$(cat /opt/cdl_flashed_file_name)
fi
if [[ -f /opt/previous_flashed_file_name ]]; then
    PREV_CDLFILE=$(cat /opt/previous_flashed_file_name)
fi
if [[ -f /version.txt ]]; then
    CUR_IMAGE=$(grep "^imagename:" /version.txt | cut -d":" -f2)
fi

# Print the variables for debugging - can be removed once the logic is stable
echo "DEBUG: CDLFILE=[${CDLFILE}]"
echo "DEBUG: PREV_CDLFILE=[${PREV_CDLFILE}]"
echo "DEBUG: CUR_IMAGE=[${CUR_IMAGE}]"

# Check all cleanup conditions in a single if statement using bash syntax
if [[ ! -f /opt/previous_flashed_file_name || \
      ( ! -f /opt/cdl_flashed_file_name && "${PREV_CDLFILE}" != *"${CUR_IMAGE}"* ) || \
      ( -f /opt/cdl_flashed_file_name && "${CDLFILE}" != *"${PREV_CDLFILE}"* ) ]]; then

    echo "Removing gstreamer registry on bootup after CDL"
    rm -rf /opt/.gstreamer
    mkdir -p /opt/.gstreamer
    GST_REGISTRY=/opt/.gstreamer/registry.bin GST_REGISTRY_UPDATE=yes gst-inspect-1.0 >/dev/null 2>&1

elif [[ ! -f /opt/.gstreamer/registry.bin ]]; then
    # Fallback: Clean if registry file is missing anyway
    echo "Gstreamer registry empty"
    rm -rf /opt/.gstreamer
    mkdir -p /opt/.gstreamer
    GST_REGISTRY=/opt/.gstreamer/registry.bin GST_REGISTRY_UPDATE=yes gst-inspect-1.0 >/dev/null 2>&1
else
    echo "gstreamer registry is not removed, previous reboot is not due to CDL"
fi
