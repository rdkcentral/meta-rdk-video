#!/bin/sh
# run_iarm_otel_test2.sh - Runs pending compatibility scenarios for RDK-62026
#
# Scenarios executed:
#   1) new -> legacy
#   2) legacy -> new
#   3) SC3 post-root event (new sender emits after root ends -> new receiver)

set -e
set -o pipefail

OTLP_ENDPOINT="${OTLP_ENDPOINT:-http://localhost:4318}"
IARM_DAEMON_BIN="${IARM_DAEMON_BIN:-iarmbusd}"
SKIP_DAEMON="${SKIP_DAEMON:-0}"
BINDIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="/opt/logs"

PUB2="$BINDIR/iarm_otel_test2_pub"
SUB2="$BINDIR/iarm_otel_test2_sub"

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
    else
        echo "[TEST2] Starting iarmbusd ..."
        "$IARM_DAEMON_BIN" &
        DAEMON_PID=$!
        sleep 2
    fi
fi

run_case() {
    case_name="$1"
    sub_mode="$2"
    pub_scenario="$3"

    echo ""
    echo "[TEST2] === $case_name ==="

    "$SUB2" "$sub_mode" > "$LOG_DIR/iarm_otel_test2_sub_${case_name}.log" 2>&1 &
    SUB_PID=$!
    sleep 2

    PUB_RC=0
    "$PUB2" "$pub_scenario" 2>&1 | tee "$LOG_DIR/iarm_otel_test2_pub_${case_name}.log" || PUB_RC=$?

    sleep 2
    kill "$SUB_PID" 2>/dev/null || true
    wait "$SUB_PID" 2>/dev/null || true

    if [ "$PUB_RC" -ne 0 ]; then
        echo "[TEST2] FAIL: $case_name publisher rc=$PUB_RC"
        return 1
    fi

    echo "[TEST2] PASS: $case_name publisher rc=0"
    return 0
}

run_case "new_to_legacy" "legacy" "new_to_legacy"
run_case "legacy_to_new" "new" "legacy_to_new"
run_case "sc3" "new" "sc3"

if [ -n "${DAEMON_PID:-}" ]; then
    kill "$DAEMON_PID" 2>/dev/null || true
fi

echo ""
echo "[TEST2] Completed. Logs:"
echo "  $LOG_DIR/iarm_otel_test2_pub_new_to_legacy.log"
echo "  $LOG_DIR/iarm_otel_test2_sub_new_to_legacy.log"
echo "  $LOG_DIR/iarm_otel_test2_pub_legacy_to_new.log"
echo "  $LOG_DIR/iarm_otel_test2_sub_legacy_to_new.log"
echo "  $LOG_DIR/iarm_otel_test2_pub_sc3.log"
echo "  $LOG_DIR/iarm_otel_test2_sub_sc3.log"
echo ""
echo "[TEST2] Validation hints:"
echo "  new->legacy: expect root span in pub service, no child spans from sub legacy mode"
echo "  legacy->new: sub log should show 'no incoming traceparent -> no child span'"
echo "  sc3: sub log should show 'no incoming traceparent -> no child span' for post-root event"
