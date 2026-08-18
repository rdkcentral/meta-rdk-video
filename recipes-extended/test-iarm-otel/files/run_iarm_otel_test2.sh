#!/bin/sh
# run_iarm_otel_test2.sh - Runs compatibility scenarios for RDK-62026:
#   1) new -> legacy : traced sender, legacy receiver
#   2) legacy -> new : untraced sender, new receiver
#   3) sc3           : traced sender finishes root, then sends untraced event to new receiver
#
# Pass/fail is determined by grepping sub logs for expected output strings.
# Exit code 0 = all cases passed, 1 = at least one failure.

set -e
set -o pipefail

OTLP_ENDPOINT="${OTLP_ENDPOINT:-http://localhost:4318}"
IARM_DAEMON_BIN="${IARM_DAEMON_BIN:-iarmbusd}"
SKIP_DAEMON="${SKIP_DAEMON:-0}"
BINDIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="/opt/logs"

PUB2="$BINDIR/iarm_otel_test2_pub"
SUB2="$BINDIR/iarm_otel_test2_sub"

OVERALL_RC=0

for bin in "$PUB2" "$SUB2"; do
    if [ ! -x "$bin" ]; then
        echo "[TEST2] ERROR: $bin not found or not executable" >&2
        exit 1
    fi
done

export OTEL_EXPORTER_OTLP_ENDPOINT="$OTLP_ENDPOINT"
echo "[TEST2] OTLP endpoint: $OTLP_ENDPOINT"

if [ "$SKIP_DAEMON" != "1" ]; then
    if pgrep -x iarmbusd > /dev/null 2>&1; then
        echo "[TEST2] iarmbusd already running - skipping start"
    elif command -v "$IARM_DAEMON_BIN" > /dev/null 2>&1; then
        echo "[TEST2] Starting iarmbusd ..."
        "$IARM_DAEMON_BIN" &
        DAEMON_PID=$!
        sleep 2
    else
        echo "[TEST2] WARNING: $IARM_DAEMON_BIN not in PATH - assuming daemon already running"
    fi
fi

# ── run_case <name> <sub_mode> <pub_scenario> <pass_grep> [<fail_grep>] ─────
# pass_grep: string that MUST appear in the sub log to pass.
# fail_grep: string that must NOT appear in the sub log (optional).
run_case() {
    case_name="$1"
    sub_mode="$2"
    pub_scenario="$3"
    pass_grep="$4"
    fail_grep="${5:-}"

    echo ""
    echo "[TEST2] ════════════════════════════════════════"
    echo "[TEST2] Scenario: $case_name"
    echo "[TEST2] ════════════════════════════════════════"

    SUB_LOG="$LOG_DIR/iarm_otel_test2_sub_${case_name}.log"
    PUB_LOG="$LOG_DIR/iarm_otel_test2_pub_${case_name}.log"

    "$SUB2" "$sub_mode" > "$SUB_LOG" 2>&1 &
    SUB_PID=$!
    sleep 2

    PUB_RC=0
    "$PUB2" "$pub_scenario" 2>&1 | tee "$PUB_LOG" || PUB_RC=$?

    sleep 2
    kill "$SUB_PID" 2>/dev/null || true
    wait "$SUB_PID" 2>/dev/null || true

    CASE_RC=0

    if [ "$PUB_RC" -ne 0 ]; then
        echo "[TEST2] FAIL [$case_name] publisher rc=$PUB_RC"
        CASE_RC=1
    fi

    if ! grep -q "$pass_grep" "$SUB_LOG" 2>/dev/null; then
        echo "[TEST2] FAIL [$case_name] expected string not found in sub log: $pass_grep"
        CASE_RC=1
    fi

    if [ -n "$fail_grep" ] && grep -q "$fail_grep" "$SUB_LOG" 2>/dev/null; then
        echo "[TEST2] FAIL [$case_name] forbidden string found in sub log: $fail_grep"
        CASE_RC=1
    fi

    if [ "$CASE_RC" -eq 0 ]; then
        echo "[TEST2] PASS [$case_name]"
    else
        OVERALL_RC=1
    fi
}

# ── Scenario 1: new sender -> legacy receiver ──────────────────────────────
# Event: patched libIARMBus appends traceparent suffix; legacy sub reads only
#        data[0..len-1] and ignores suffix. Payload must arrive correctly.
# RPC:   plain IARM_Bus_Call (not CallWithTracing). Both sides work unchanged.
# Expected in sub log: the event payload received and processed without crash.
# Forbidden: any indication a child span was started (legacy mode must never trace).
run_case "new_to_legacy" "legacy" "new_to_legacy" \
    "new_to_legacy_event" \
    "child span started"

# ── Scenario 2: legacy sender -> new receiver ──────────────────────────────
# Sender has no active span and never calls rdk_otlp_init.
# New receiver runs IARM_Bus_GetCurrentIncomingTraceparent() which returns NULL.
# Expected in sub log: "no incoming traceparent" for both event and RPC.
run_case "legacy_to_new" "new" "legacy_to_new" \
    "no incoming traceparent -> no child span" \
    "child span started"

# ── Scenario 3: SC3 post-root untraced event -> new receiver ───────────────
# Sender finishes its root span before broadcasting. No active context.
# New receiver gets event with no magic suffix, sees no incoming traceparent.
# Expected in sub log: "no incoming traceparent" for post-root event.
run_case "sc3" "new" "sc3" \
    "no incoming traceparent -> no child span" \
    "child span started"

if [ -n "${DAEMON_PID:-}" ]; then
    kill "$DAEMON_PID" 2>/dev/null || true
fi

echo ""
echo "[TEST2] ════════════════════════════════════════════════════════"
echo "[TEST2] Compatibility matrix results"
echo "[TEST2] ════════════════════════════════════════════════════════"
echo ""
echo " Logs (check for details):"
echo "   $LOG_DIR/iarm_otel_test2_sub_new_to_legacy.log"
echo "   $LOG_DIR/iarm_otel_test2_sub_legacy_to_new.log"
echo "   $LOG_DIR/iarm_otel_test2_sub_sc3.log"
echo ""
echo " OTel lib: /opt/logs/rdk_otel_tracer.log"
echo ""
echo " Expected Jaeger state after new_to_legacy run:"
echo "   One root span in service iarm-otel-poc-pub2-new."
echo "   No child spans from sub (legacy mode never creates them)."
echo ""

if [ "$OVERALL_RC" -eq 0 ]; then
    echo " RESULT: ALL PASSED"
else
    echo " RESULT: ONE OR MORE FAILURES — see log details above"
fi

exit "$OVERALL_RC"
