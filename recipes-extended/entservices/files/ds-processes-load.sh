#!/bin/sh

cleanup() {
    for pid in "$PIDSTAT_PID" "$NETHOGS_PID"; do
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            kill "$pid" 2>/dev/null
        fi
    done
}

trap cleanup EXIT
trap 'trap - EXIT; cleanup; exit 130' INT TERM HUP QUIT

if [ -f /opt/pidstat-enable ]; then
    PIDSTAT_DELAY=$(tr -d '[:space:]' < /opt/pidstat-enable 2>/dev/null)

    case "$PIDSTAT_DELAY" in
        ''|0|*[!0-9]*)
            PIDSTAT_DELAY=1
            ;;
    esac

    pidstat -h -u -r -d -p ALL "$PIDSTAT_DELAY" > /opt/logs/ds-processes-load.log &
    PIDSTAT_PID=$!
fi

if [ -f /opt/nethogs-enable ]; then
    NETHOGS_DELAY=$(tr -d '[:space:]' < /opt/nethogs-enable 2>/dev/null)

    case "$NETHOGS_DELAY" in
        ''|0|.*|*[!0-9.]*|*.*.*)
            NETHOGS_DELAY=0.5
            ;;
    esac

    nethogs -t -d "$NETHOGS_DELAY" >> /opt/logs/ds-processes-nethogs.log &
    NETHOGS_PID=$!
fi

wait