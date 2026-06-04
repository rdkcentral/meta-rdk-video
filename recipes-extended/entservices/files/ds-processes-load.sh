#!/bin/sh

cleanup() {
    for pid in "$PIDSTAT_PID" "$NETHOGS_PID"; do
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            kill "$pid" 2>/dev/null
        fi
    done
}

trap cleanup EXIT
trap 'trap - EXIT; cleanup; exit 130' INT TERM

pidstat -h -u -r -d -p ALL 1 > /opt/logs/ds-processes-load.log &
PIDSTAT_PID=$!

nethogs -t -d 0.5 >> /opt/logs/ds-processes-nethogs.log &
NETHOGS_PID=$!

wait