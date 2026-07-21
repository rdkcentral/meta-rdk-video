#!/bin/sh
# perf.sh - Perf tracing script for WPEFramework
# This script is executed after wpeframework.service starts.

echo "perf.sh: Starting perf tracing for WPEFramework..."

for p in $(pgrep -x WPEFramework); do
  for t in /proc/$p/task/*; do
    if grep -q 'Monitor::IResou' "$t/comm" 2>/dev/null; then
      tid=$(basename "$t")
      echo "pid=$p tid=$tid $(ps -L -p $p -o tid,cls,rtprio,ni,pri,comm | awk -v id=$tid '$1==id')"
    fi
  done
done

export LD_LIBRARY_PATH=/opt/perf/packages-split/lib32-perf/usr/lib

/opt/perf/packages-split/lib32-perf/usr/bin/perf record -g -t $tid -F 999 -o /opt/logs/perf.data -- sleep 200

echo "perf.sh: Done."
