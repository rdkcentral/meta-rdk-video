##########################################################################
# If not stated otherwise in this file or this component's LICENSE
# file the following copyright and licenses apply:
#
# Copyright 2016 RDK Management
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
##########################################################################

#!/bin/bash
# Check if callsign argument is provided
if [ -z "$1" ]; then
  echo "Usage: $0 <callsign>"
  exit 1
fi

CALLSIGN=$1

# Deactivate the plugin
curl -X POST -H "Content-Type: application/json" 'http://127.0.0.1:9998/jsonrpc' \
  -d '{
    "jsonrpc": "2.0",
    "id": 4,
    "method": "Controller.1.deactivate",
    "params": { "callsign": "'"$CALLSIGN"'" }
  }'

echo

# Wait for 1 seconds before exiting
sleep 1


# Get the status of the plugin
curl -X POST -H "Content-Type: application/json" 'http://127.0.0.1:9998/jsonrpc' \
  -d '{
    "jsonrpc": "2.0",
    "id": 5,
    "method": "Controller.1.status@'"$CALLSIGN"'"
  }'

echo
