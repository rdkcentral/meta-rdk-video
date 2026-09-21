#!/bin/sh
#
# run_iarm_tp_test.sh — Orchestrates the IARM set/get traceparent test
#
# Prerequisites on target:
#   • iarmbusd running (or started by this script) - built from iarmbus2
#   • librdk_otlp.so present and OTLP_ENDPOINT reachable
#   • iarm_tp_test_pub and iarm_tp_test_sub in PATH (or same dir)
#
# What this script does:
#   1. (Optionally) starts iarmbusd if not already running.
#   2. Starts iarm_tp_test_sub in the background.
#   3. Waits 2 s for subscriber to register its handlers.
#   4. Runs iarm_tp_test_pub (foreground, completes quickly).
#   5. Waits for subscriber to finish exporting its spans.
#   6. Prints pass/fail guidance.
#
# Environment variables:
#   OTLP_ENDPOINT    Override collector endpoint (default: http://localhost:4318)
#   IARM_DAEMON_BIN  Path to iarmbusd binary (default: iarmbusd)
#   SKIP_DAEMON      Set to 1 to skip starting iarmbusd (if already running)
#
# Exit codes:
#   0 — both binaries exited cleanly
#   1 — publisher or subscriber returned non-zero

set -e
set -o pipefail

OTLP_ENDPOINT="${OTLP_ENDPOINT:-http://localhost:4318}"
IARM_DAEMON_BIN="${IARM_DAEMON_BIN:-iarmbusd}"
SKIP_DAEMON="${SKIP_DAEMON:-0}"
BINDIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="/opt/logs"

PUB="$BINDIR/iarm_tp_test_pub"
SUB="$BINDIR/iarm_tp_test_sub"

for bin in "$PUB" "$SUB"; do
    if [ ! -x "$bin" ]; then
        echo "[TEST] ERROR: $bin not found or not executable" >&2
        exit 1
    fi
done

export OTEL_EXPORTER_OTLP_ENDPOINT="$OTLP_ENDPOINT"
echo "[TEST] OTLP endpoint: $OTLP_ENDPOINT"

if [ "$SKIP_DAEMON" != "1" ]; then
    if pgrep -x iarmbusd > /dev/null 2>&1; then
        echo "[TEST] iarmbusd already running — skipping start"
    else
        echo "[TEST] Starting iarmbusd ..."
        "$IARM_DAEMON_BIN" &
        DAEMON_PID=$!
        sleep 2
        echo "[TEST] iarmbusd started (pid $DAEMON_PID)"
    fi
fi

echo "[TEST] Starting subscriber ..."
"$SUB" > "$LOG_DIR/iarm_tp_test_sub.log" 2>&1 &
SUB_PID=$!
echo "[TEST] Subscriber pid: $SUB_PID"

sleep 2

echo "[TEST] Starting publisher ..."
PUB_RC=0
"$PUB" 2>&1 | tee "$LOG_DIR/iarm_tp_test_pub.log" || PUB_RC=$?

echo "[TEST] Publisher exited (rc=$PUB_RC)"

sleep 3

if kill -0 "$SUB_PID" 2>/dev/null; then
    echo "[TEST] Stopping subscriber (pid $SUB_PID) ..."
    kill -TERM "$SUB_PID" 2>/dev/null || true
    sleep 1
fi

echo "[TEST] Logs: $LOG_DIR/iarm_tp_test_pub.log , $LOG_DIR/iarm_tp_test_sub.log"
echo "[TEST] Expected: 1 trace with root + SC1a/SC1b/SC2 child spans in Jaeger."
echo "[TEST] SC3 event should appear in sub log with no child span created."

exit "$PUB_RC"
