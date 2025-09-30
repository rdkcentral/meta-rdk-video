#!/bin/sh
#h — Capture NTP packets until /tmp/systiemmgr/ntp is created

IFACE=${1:-any}         # Interface (default = any)
#MAX_WAIT=${2:-120}      # Max seconds to wait
TAIL_AFTER_FILE=${3:-2} # Extra seconds to capture after file appears
SUMMARY_CSV="/tmp/ntp_sync_summary.csv"

PCAP_FILE="/tmp/ntp_$(date +%Y%m%dT%H%M%S).pcap"
MARKER_FILE="/tmp/systimemgr/ntp"
OUT="/tmp/ntp_poll_interval.csv"
TOP_OUT="/tmp/ntp_top.csv"
IN="/opt/logs/ntp.log"


echo "Interface   : $IFACE"
#echo "Max wait    : ${MAX_WAIT}s"
echo "PCAP output : $PCAP_FILE"
echo "Filter      : udp port 123"
echo "Marker file : $MARKER_FILE"
echo

# Monotonic start time (fractional seconds)
start_up=$(awk '{print $1}' /proc/uptime)
START_TS_UTC=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
# Start tcpdump (packet-buffered with -U)
tcpdump -i "$IFACE" udp port 123 -nn -U -w "$PCAP_FILE" 2>/dev/null &
TCPDUMP_PID=$!


if ! kill -0 "$TCPDUMP_PID" 2>/dev/null; then
  echo "Error: failed to start tcpdump." >&2
  exit 1
fi

TOP_PID=""
[ -s "$TOP_OUT" ] || echo "timestamp_utc,cpu_percent,mem_percent" > "$TOP_OUT"
if systemctl list-unit-files | grep -q '^systemd-timesyncd.service'; then
  SVC=systemd-timesyncd.service
  KIND=timesyncd
else
  SVC=chronyd.service
  KIND=chrony
fi
echo "service:$SVC"

# 2) wait until service is active
until systemctl is-active --quiet "$SVC"; do sleep 1; done

# 3) wait for a non-zero MainPID
PID=0
while :; do
  PID="$(systemctl show -p MainPID --value "$SVC" || echo 0)"
  [[ "$PID" != "0" && -n "$PID" ]] && break
  sleep 1
done

ntp_client_pid=$PID

echo "pid:$ntp_client_pid"
if [ -n $ntp_client_pid ]; then
  top -b -d 1 -p "$ntp_client_pid" | awk -v pid="$ntp_client_pid" '
    $1==pid {
      cmd = "date -u +%Y-%m-%dT%H:%M:%SZ"; cmd | getline ts; close(cmd);
      printf "%s,%s,%s\n", ts, $9, $10
      fflush(stdout)
    }
  ' >> "$TOP_OUT" &

 TOP_PID=$!
fi

echo "Capturing NTP Metrics,poll interval,Pkts... waiting for $MARKER_FILE to appear..."
[ -s "$OUT" ] || echo "timestamp_utc,poll_interval" > "$OUT"

awk -F 'poll interval:' '/poll interval:/ {
  # grab ISO timestamp
  if (match($0, /[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9:.]+Z/)) ts=substr($0,RSTART,RLENGTH);
  # right side after "poll interval:" -> trim left spaces, coerce to number
  s=$2; sub(/^[[:space:]]+/, "", s); pi=s+0;
  if (ts!="" && pi!="") print ts "," pi;
}' "$IN" >> "$OUT"



WATCH_PID=$!


elapsed=0
SYNCED="no"


  if [ -f "$MARKER_FILE" ]; then
    SYNCED="yes"
    break
  fi
  sleep 1
  elapsed=$((elapsed+1))


# Extra grace period after marker detected
if [ "$SYNCED" = "yes" ] && [ "$TAIL_AFTER_FILE" -gt 0 ]; then
  sleep "$TAIL_AFTER_FILE"
fi

# Stop tcpdump
kill -TERM "$TCPDUMP_PID" 2>/dev/null || true
wait "$TCPDUMP_PID" 2>/dev/null || true

[ -n "$TOP_PID" ] && kill -TERM "$TOP_PID" 2>/dev/null || true
[ -n "$TOP_PID" ] && wait "$TOP_PID" 2>/dev/null || true

kill -TERM "$WATCH_PID" 2>/dev/null || true
wait "$WATCH_PID" 2>/dev/null || true

# Monotonic end time
end_up=$(awk '{print $1}' /proc/uptime)


END_TS_UTC=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

DUR=$(awk -v s="$start_up" -v e="$end_up" 'BEGIN{d=e-s; if(d<0)d=-d; if(d==0)d=1; printf "%.2f", d}')

# Count packets from pcap
PACKETS=$(tcpdump -nn -q -r "$PCAP_FILE" 2>/dev/null | wc -l)

# Calculate packets/sec
RATE=$(awk -v p="$PACKETS" -v d="$DUR" 'BEGIN{ if(d>0) printf "%.2f", p/d; else print 0 }')


[ -f "$SUMMARY_CSV" ] || echo "start_utc,end_utc,synced,iface,pcap_file,duration_s,total_packets,packets_per_sec" > "$SUMMARY_CSV"

# Append one CSV row
# (quotes around strings; numeric fields as-is)
printf '"%s","%s","%s","%s","%s",%s,%s,%s\n' \
  "$START_TS_UTC" \
  "$END_TS_UTC" \
  "$SYNCED" \
  "$IFACE" \
  "$PCAP_FILE" \
  "$DUR" \
  "$PACKETS" \
  "$RATE" >> "$SUMMARY_CSV"

echo "Summary appended to: $SUMMARY_CSV"

echo
echo "===== NTP Sync Summary ====="
echo "Start (UTC)   : $START_TS_UTC"
echo "End   (UTC)   : $END_TS_UTC"
echo "Synced        : $SYNCED"
echo "Interface     : $IFACE"
echo "PCAP file     : $PCAP_FILE"
echo "Duration (s)  : $DUR"
echo "Total packets : $PACKETS"
echo "Packets/sec   : $RATE"

# Exit non-zero if marker not seen
[ "$SYNCED" = "yes" ] || exit 2
