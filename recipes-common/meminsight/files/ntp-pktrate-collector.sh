#!/bin/sh
# ntp_until_sync.sh — Capture NTP packets until "NTP synchronized: yes"

IFACE=${1:-any}         # Interface (default = any)
MAX_WAIT=${2:-120}      # Max seconds to wait
TAIL_AFTER_YES=${3:-2}  # Extra seconds to capture after sync

PCAP_FILE="/tmp/ntp_$(date +%Y%m%dT%H%M%S).pcap"

echo "Interface   : $IFACE"
echo "Max wait    : ${MAX_WAIT}s"
echo "PCAP output : $PCAP_FILE"
echo "Filter      : udp port 123"
echo

# Monotonic start time (fractional seconds)
start_up=$(awk '{print $1}' /proc/uptime)

# Start tcpdump (packet-buffered with -U)
tcpdump -i "$IFACE" udp port 123 -nn -U -w "$PCAP_FILE" 2>/dev/null &
TCPDUMP_PID=$!

if ! kill -0 "$TCPDUMP_PID" 2>/dev/null; then
  echo "Error: failed to start tcpdump." >&2
  exit 1
fi

echo "Capturing NTP packets... waiting for sync (timedatectl)..."

seen_no=0
yes_count=0
elapsed=0
SYNCED="no"

while [ $elapsed -lt "$MAX_WAIT" ]; do
  val=$(timedatectl status | awk -F': ' '/NTP synchronized:/ {print $2}' | tr -d ' ')
echo "value:$val"
  if [ "$val" = "yes" ]; then
      yes_count=1
      SYNCED="yes"
      break
fi
  sleep 1
  elapsed=$((elapsed+1))
done

# Extra grace period after sync
if [ "$SYNCED" = "yes" ] && [ "$TAIL_AFTER_YES" -gt 0 ]; then
  sleep "$TAIL_AFTER_YES"
fi

# Stop tcpdump
kill -TERM "$TCPDUMP_PID" 2>/dev/null || true
wait "$TCPDUMP_PID" 2>/dev/null || true

# Monotonic end time
end_up=$(awk '{print $1}' /proc/uptime)

# Duration
DUR=$(awk -v s="$start_up" -v e="$end_up" 'BEGIN{printf "%.2f", e-s}')

# If negative, make positive
DUR=$(awk -v d="$DUR" 'BEGIN { if (d<0) d=-d; print d }')
# If zero, make 1
[ "$(printf "%.0f" "$DUR")" -eq 0 ] && DUR=1

# Count packets from pcap
PACKETS=$(tcpdump -nn -q -r "$PCAP_FILE" 2>/dev/null | wc -l)

# Calculate packets/sec
RATE=$(awk -v p="$PACKETS" -v d="$DUR" 'BEGIN{ if(d>0) printf "%.2f", p/d; else print 0 }')

echo
echo "===== NTP Sync Summary ====="
echo "Synced        : $SYNCED"
echo "Interface     : $IFACE"
echo "PCAP file     : $PCAP_FILE"
echo "Duration (s)  : $DUR"
echo "Total packets : $PACKETS"
echo "Packets/sec   : $RATE"

# Exit non-zero if not synced
[ "$SYNCED" = "yes" ] || exit 2

