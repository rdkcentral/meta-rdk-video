#!/bin/sh
#
# run_iarm_tp_mt_test.sh — Orchestrates the IARM traceparent concurrency stress test
#
# Prerequisites on target:
#   • iarmbusd running (or started by this script)
#   • librdk_otlp.so present and OTLP_ENDPOINT reachable (real spans are used)
#   • iarm_tp_test_mt_pub and iarm_tp_test_mt_sub in PATH (or same dir)
#
# What this script does:
#   1. (Optionally) starts iarmbusd if not already running.
#   2. Starts iarm_tp_test_mt_sub in the background.
#   3. Waits 2 s for the subscriber to register its handlers.
#   4. Runs iarm_tp_test_mt_pub (foreground) with the requested thread/iteration counts.
#   5. Stops the subscriber and checks both logs for PASS/FAIL and MISMATCH lines.
#
# Usage:
#   run_iarm_tp_mt_test.sh [thread_count] [iterations_per_thread]
#
# Environment variables:
#   OTLP_ENDPOINT    Override collector endpoint (default: http://localhost:4318)
#   IARM_DAEMON_BIN  Path to iarmbusd binary (default: iarmbusd)
#   SKIP_DAEMON      Set to 1 to skip starting iarmbusd (if already running)
#
# Exit codes:
#   0 — publisher and subscriber both reported PASS, no mismatches found
#   1 — publisher or subscriber reported FAIL, or a MISMATCH line was found

set -e
set -o pipefail

THREAD_COUNT="${1:-8}"
ITERATIONS="${2:-20}"
OTLP_ENDPOINT="${OTLP_ENDPOINT:-http://localhost:4318}"
IARM_DAEMON_BIN="${IARM_DAEMON_BIN:-iarmbusd}"
SKIP_DAEMON="${SKIP_DAEMON:-0}"
BINDIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="/opt/logs"

PUB="$BINDIR/iarm_tp_test_mt_pub"
SUB="$BINDIR/iarm_tp_test_mt_sub"

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
"$SUB" > "$LOG_DIR/iarm_tp_test_mt_sub.log" 2>&1 &
SUB_PID=$!
echo "[TEST] Subscriber pid: $SUB_PID"

sleep 2

echo "[TEST] Starting publisher (threads=$THREAD_COUNT iterations=$ITERATIONS) ..."
PUB_RC=0
"$PUB" "$THREAD_COUNT" "$ITERATIONS" 2>&1 | tee "$LOG_DIR/iarm_tp_test_mt_pub.log" || PUB_RC=$?

echo "[TEST] Publisher exited (rc=$PUB_RC)"

sleep 2

if kill -0 "$SUB_PID" 2>/dev/null; then
    echo "[TEST] Stopping subscriber (pid $SUB_PID) ..."
    kill -TERM "$SUB_PID" 2>/dev/null || true
    sleep 1
fi
wait "$SUB_PID" 2>/dev/null || true

SUB_LOG="$LOG_DIR/iarm_tp_test_mt_sub.log"
PUB_LOG="$LOG_DIR/iarm_tp_test_mt_pub.log"
RESULT_RC=0

if [ "$PUB_RC" != "0" ]; then
    echo "[TEST] Publisher reported FAIL (rc=$PUB_RC)"
    RESULT_RC=1
fi

if grep -q "MISMATCH" "$SUB_LOG" 2>/dev/null; then
    echo "[TEST] Subscriber log contains MISMATCH lines — see $SUB_LOG"
    RESULT_RC=1
fi

if ! grep -q "PASS (event path)" "$SUB_LOG" 2>/dev/null; then
    echo "[TEST] Subscriber did not report PASS — see $SUB_LOG"
    RESULT_RC=1
fi

echo "[TEST] Logs: $PUB_LOG , $SUB_LOG"
if [ "$RESULT_RC" = "0" ]; then
    echo "[TEST] Overall: PASS"
else
    echo "[TEST] Overall: FAIL"
fi

exit "$RESULT_RC"
