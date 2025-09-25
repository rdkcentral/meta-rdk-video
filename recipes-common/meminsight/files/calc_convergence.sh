#!/usr/bin/env sh
# Usage: ./converge_simple.sh /path/to/ntp-metrics.csv [threshold_ms] [hold_seconds] [sample_interval_s]
# Defaults: threshold_ms=20  hold_seconds=60  sample_interval_s=5

CSV="$1"
TH="${2:-20}"
HOLD="${3:-60}"
SAMPLE="${4:-5}"

if [ -z "$CSV" ] || [ ! -f "$CSV" ]; then
  echo "Usage: $0 /path/to/ntp-metrics.csv [threshold_ms] [hold_seconds] [sample_interval_s]" >&2
  exit 1
fi

awk -F',' -v TH="$TH" -v HOLD="$HOLD" -v DT="$SAMPLE" '
  BEGIN {
    # We assume col1=timestamp, col2=offset_ms
    in_run=0; got=0; idx=0; first_idx=-1;
  }
  NR==1 { next }                  # skip header
  NF==0 { next }                  # skip blank lines
  {
    idx++;                        # data-row index (0-based within data)
    if (first_idx < 0) { first_idx = idx; first_ts = $1 }

    off = $2 + 0.0
    abs = (off < 0 ? -off : off)

    if (!got) {
      if (!in_run && abs <= TH) { in_run=1; run_start_idx = idx; run_start_ts = $1 }
      if (in_run && abs > TH)   { in_run=0 }
      if (in_run) {
        hold_sec = (idx - run_start_idx + 1) * DT
        if (hold_sec >= HOLD) {
          got=1
          conv_idx = run_start_idx
          conv_ts  = run_start_ts
        }
      }
    }
  }
  END {
    if (got) {
      since_start = (conv_idx - first_idx) * DT
      print "Converged at:", conv_ts
      print "Seconds since first sample:", since_start
      print "Policy: |offset| ≤ " TH " ms for ≥ " HOLD " s (sample=", DT, "s)"
    } else {
      print "No convergence under policy."
    }
  }
' "$CSV"
