#!/bin/bash

LOGFILE="/opt/logs/ntp.log"
CSVFILE="/opt/poll_interval.csv"

sh /lib/rdk/ntp-pktrate-collector.sh

# Ensure CSV file has a header
if [ ! -f "$CSVFILE" ]; then
    echo "timestamp,poll_interval" > "$CSVFILE"
fi

sleep 480
echo "Starting to collect Poll interval after 8 mins >> /tmp/pktrate.log
# Track whether we found any poll interval
FOUND=0

echo "reading the file" >> /tmp/pktrate.log
# Read file line by line
while IFS= read -r line; do
    if echo "$line" | grep -iq "poll interval"; then
        # Extract timestamp (ISO format at start of line)
        LOG_TIMESTAMP=$(echo "$line" | grep -oE '^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9:.]+Z')
        # Extract poll interval value
        VALUE=$(echo "$line" | grep -oE 'poll interval: [0-9]+' | awk '{print $3}')
        echo "$LOG_TIMESTAMP,$VALUE" >> "$CSVFILE"
        FOUND=1
    fi
done < "$LOGFILE"
echo "done reading file" >> /tmp/pktrate.log
# If no poll interval was found
if [ "$FOUND" -eq 0 ]; then
    echo ",,poll_interval_not_found" >> "$CSVFILE"
fi
