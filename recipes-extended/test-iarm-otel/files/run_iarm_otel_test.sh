#!/bin/sh
#
# run_iarm_otel_test.sh — Orchestrates the IARM OTel POC test
#
# Prerequisites on target:
#   • iarmbusd running (or started by this script)
#   • librdk_otlp.so present and OTLP_ENDPOINT reachable
#   • iarm_otel_test_pub and iarm_otel_test_sub in PATH (or same dir)
#
# What this script does:
#   1. (Optionally) starts iarmbusd if not already running.
#   2. Starts iarm_otel_test_sub in the background.
#   3. Waits 2 s for subscriber to register its handlers.
#   4. Runs iarm_otel_test_pub (foreground, completes quickly).
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
set -o pipefail   # make pipe fail if any stage fails (catches pub crash via tee)

OTLP_ENDPOINT="${OTLP_ENDPOINT:-http://localhost:4318}"
IARM_DAEMON_BIN="${IARM_DAEMON_BIN:-iarmbusd}"
SKIP_DAEMON="${SKIP_DAEMON:-0}"
BINDIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="/opt/logs"

PUB="$BINDIR/iarm_otel_test_pub"
SUB="$BINDIR/iarm_otel_test_sub"

# ── Sanity checks ─────────────────────────────────────────────────────────
for bin in "$PUB" "$SUB"; do
    if [ ! -x "$bin" ]; then
        echo "[TEST] ERROR: $bin not found or not executable" >&2
        exit 1
    fi
done

# Export endpoint so rdk_otlp picks it up (if it reads OTEL_EXPORTER_OTLP_ENDPOINT)
export OTEL_EXPORTER_OTLP_ENDPOINT="$OTLP_ENDPOINT"
echo "[TEST] OTLP endpoint: $OTLP_ENDPOINT"

# ── Start IARM daemon if needed ───────────────────────────────────────────
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

# ── Start subscriber in background ────────────────────────────────────────
echo "[TEST] Starting subscriber ..."
"$SUB" > "$LOG_DIR/iarm_otel_test_sub.log" 2>&1 &
SUB_PID=$!
echo "[TEST] Subscriber pid: $SUB_PID"

# Give subscriber time to register handlers before publisher fires
sleep 2

# ── Run publisher (foreground) ────────────────────────────────────────────
echo "[TEST] Starting publisher ..."
PUB_RC=0
"$PUB" 2>&1 | tee "$LOG_DIR/iarm_otel_test_pub.log" || PUB_RC=$?

echo "[TEST] Publisher exited (rc=$PUB_RC)"

# Give subscriber time to finish exporting its child spans
sleep 3
kill "$SUB_PID" 2>/dev/null || true
wait "$SUB_PID" 2>/dev/null || true
echo "[TEST] Subscriber stopped"

# ── Clean up daemon if we started it ─────────────────────────────────────
if [ -n "${DAEMON_PID:-}" ]; then
    kill "$DAEMON_PID" 2>/dev/null || true
    echo "[TEST] iarmbusd stopped"
fi

# ── Results guidance ──────────────────────────────────────────────────────
echo ""
echo "═══════════════════════════════════════════════════════════"
echo " IARM OTel POC Test Results"
echo "═══════════════════════════════════════════════════════════"
echo ""
echo " Logs:"
echo "   Publisher : $LOG_DIR/iarm_otel_test_pub.log"
echo "   Subscriber: $LOG_DIR/iarm_otel_test_sub.log"
echo "   OTel lib  : /opt/logs/rdk_otel_tracer.log"
echo ""
echo " Expected OTLP trace (verify in collector UI or Jaeger):"
echo ""
 echo "   [root]  iarm-otel-poc-pub  OTEL_TEST_OWNER                       ~5 s"
 echo "     SC1a  iarm-otel-poc-sub  IARM.OTEL_TEST_OWNER.ScanComplete     ~200 ms"
 echo "     SC1b  iarm-otel-poc-sub  IARM.OTEL_TEST_OWNER.ParseConfig      ~150 ms"
 echo "     SC1c  iarm-otel-poc-sub  IARM.OTEL_TEST_OWNER.ApplySettings    ~100 ms"
 echo "     SC2   iarm-otel-poc-sub  IARM.OTEL_TEST_OWNER.GetTestState     ~120 ms"
 echo ""
 echo " All 5 spans MUST share the same trace_id."
 echo ""
 echo " SC3 (untraced sender) is verified in sub log only — no span expected:"
 echo "   grep 'SC3: no incoming traceparent' /opt/logs/iarm_otel_test_sub.log"
 echo ""
 echo " Failure indicators:"
 echo "   child spans have different trace_id -> traceparent injection broken"
 echo "   'incoming traceparent: (none)' for SC1 events -> patched libIARMBus not loaded"
 echo "   SC3 produces an unexpected child span -> magic byte check failed"
echo ""

if [ "$PUB_RC" -ne 0 ]; then
    echo " FAIL: publisher exited with rc=$PUB_RC"
    exit 1
fi

echo " Publisher exited cleanly.  Check OTLP collector for trace correlation."
exit 0
