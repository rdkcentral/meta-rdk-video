#!/bin/sh
# perf.sh - Perf tracing script for WPEFramework
# This script is executed after wpeframework.service starts.

LOGFILE="/opt/logs/perf.sh.log"
exec >> "$LOGFILE" 2>&1

ts=$(date '+%Y%m%d_%H%M%S')

echo "$(date): perf.sh: Starting perf tracing for WPEFramework..."

tid=""
for p in $(pgrep -x WPEFramework); do
  for t in /proc/$p/task/*; do
    if grep -q 'Monitor::IResou' "$t/comm" 2>/dev/null; then
      tid=$(basename "$t")
      echo "$(date): Found target thread: pid=$p tid=$tid $(ps -L -p $p -o tid,cls,rtprio,ni,pri,comm 2>/dev/null | awk -v id=$tid '$1==id')"
      break 2
    fi
  done
done

if [ -z "$tid" ]; then
  echo "$(date): perf.sh: ERROR - No matching WPEFramework thread found. Exiting."
  exit 1
fi

export LD_LIBRARY_PATH=/opt/perf/packages-split/lib32-perf/usr/lib

echo "$(date): perf.sh: Running perf record on tid=$tid -> perf${ts}.data ..."
/opt/perf/packages-split/lib32-perf/usr/bin/perf record -o "/opt/logs/perf${ts}.data" -F 99 --tid "$tid" --call-graph fp -- sleep 200

echo "$(date): perf.sh: Done. (exit=$?)"
