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

# Wait for 2 seconds before exiting
sleep 1


# Get the status of the plugin
curl -X POST -H "Content-Type: application/json" 'http://127.0.0.1:9998/jsonrpc' \
  -d '{
    "jsonrpc": "2.0",
    "id": 5,
    "method": "Controller.1.status@'"$CALLSIGN"'"
  }'

echo
