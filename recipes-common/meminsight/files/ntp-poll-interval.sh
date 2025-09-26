#!/usr/bin/env sh
#ead "poll interval" from ntp.log into a CSV (BusyBox-safe)

IN=${IN:-/opt/logs/ntp.log}
OUT=${OUT:-/tmp/ntp_poll_interval.csv}

# Header once
[ -s "$OUT" ] || echo "timestamp_utc,poll_interval" > "$OUT"

# Reusable AWK: find "poll interval" (case-insensitive), grab first number
parse='
{
  line=$0
  low=toupper(line)                     # case-insensitive test
  if (index(low,"POLL INTERVAL")) {
    # timestamp from line if present; else use current UTC
    ts=""
    if (match(line, /[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9:.]+Z/)) {
      ts=substr(line,RSTART,RLENGTH)
    } else {
      cmd="date -u +%Y-%m-%dT%H:%M:%SZ"; cmd|getline ts; close(cmd)
    }
    # take text after "poll interval", then first number (int/float)
    split(low, a, "POLL INTERVAL")
    rest=a[2]
    # map back to same slice of original (digits unaffected by case)
    split(line, a2, "poll interval")
    rest_orig=a2[2]

    # trim up to first digit or dot, then extract number
    gsub(/^[^0-9.]*/, "", rest_orig)
    if (match(rest_orig, /^[0-9]+(\.[0-9]+)?/)) {
      pi=substr(rest_orig, RSTART, RLENGTH)
      print ts "," pi
      fflush()
    }
  }
}
'

# 1) Capture whatever already exists
awk "$parse" "$IN" >> "$OUT" 2>/dev/null

# 2) Follow new lines forever (BusyBox tail -f)
tail -n0 -f "$IN" | awk "$parse" >> "$OUT"

