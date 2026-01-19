#!/bin/sh

#!/bin/bash

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

NTP_FILE="ntp_capture_$TIMESTAMP.pcap"
DNS_FILE="dns_capture_$TIMESTAMP.pcap"

echo "Starting packet capture..."
echo "NTP  -> $NTP_FILE"
echo "DNS  -> $DNS_FILE"
echo "Press Ctrl+C to stop."

# Start NTP capture in background
tcpdump -i any -w "$NTP_FILE" udp port 123 &
NTP_PID=$!

# Start DNS capture in background
tcpdump -i any -w "$DNS_FILE" '(udp port 53) or (tcp port 53)' &
DNS_PID=$!

# Wait for Ctrl+C
trap "echo 'Stopping captures...'; kill $NTP_PID $DNS_PID; exit" INT

wait
