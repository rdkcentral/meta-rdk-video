/*
 * iarm_otel_test_pub.c — IARM OTel POC: Publisher / RPC Caller
 *
 * Covers the following compatibility scenarios (ref: Section 5 of
 * docs/iarm-otel-architecture.html):
 *
 * SC1 — New sender → New receiver, EVENT path (3 events, different durations)
 *   BroadcastEvent with active trace context → libIARMBus appends traceparent
 *   suffix.  Sub creates a child span per event with simulated processing work.
 *   Expected in Jaeger (all under one trace):
 *     [root]  iarm-otel-poc-pub  OTEL_TEST_OWNER                   ~5 s
 *       [SC1a] iarm-otel-poc-sub  IARM.OTEL_TEST_OWNER.ScanComplete  ~200 ms
 *       [SC1b] iarm-otel-poc-sub  IARM.OTEL_TEST_OWNER.ParseConfig   ~150 ms
 *       [SC1c] iarm-otel-poc-sub  IARM.OTEL_TEST_OWNER.ApplySettings ~100 ms
 *       [SC2]  iarm-otel-poc-sub  IARM.OTEL_TEST_OWNER.GetTestState  ~120 ms
 *
 * SC2 — New sender → New receiver, RPC path
 *   IARM_Bus_Call() transparently wraps the arg in an IARM_RPC_Envelope_t when a
 *   valid current traceparent is active. Sub creates a child span covering
 *   handler execution.
 *
 * SC3 — Untraced / legacy sender → New receiver, EVENT path
 *   Event broadcast AFTER root span is finished (no active traceparent).
 *   libIARMBus writes a zeroed suffix (no magic byte).  Sub detects no incoming
 *   traceparent, skips tracing entirely, and processes the event normally.
 *   Verifies backward compatibility: data payload is correct, no crash, no span.
 *
 * Usage: run AFTER iarm_otel_test_sub is started and IARM daemon is running.
 *        See run_iarm_otel_test.sh.
 */

#include "libIBus.h"
#include "rdk_otlp_instrumentation.h"

#include <stdio.h>
#include <string.h>
#include <unistd.h>

/* ── Shared test constants (must match iarm_otel_test_sub.c) ─────────────── */
#define TEST_OWNER         "OTEL_TEST_OWNER"
#define TEST_EVENT_SCAN    0   /* simulates device scan completion      */
#define TEST_EVENT_PARSE   1   /* simulates config parse completion     */
#define TEST_EVENT_APPLY   2   /* simulates settings apply completion   */
#define NUM_EVENTS         3
#define TEST_METHOD        "GetTestState"

typedef struct {
    int  value;
    char label[32];
} TestEventData_t;

typedef struct {
    int  request_token;
    char response_msg[64];
} TestRpcArg_t;

/* ── Helper ──────────────────────────────────────────────────────────────── */
static void log_tp(const char *tag)
{
    const char *tp = rdk_otlp_get_current_traceparent();
    printf("[PUB] %s — traceparent: %s\n", tag,
           tp ? tp : "(none)");
}

int main(void)
{
    printf("[PUB] Starting iarm_otel_test_pub (pid %d)\n", (int)getpid());

    /* 1. Initialize OTel — this process is the root of the trace */
    rdk_otlp_init("iarm-otel-poc-pub", "1.0.0");
    printf("[PUB] rdk_otlp_init done\n");

    /* 2. Connect to IARM bus */
    IARM_Bus_Init("iarm_otel_test_pub");
    IARM_Bus_Connect();
    printf("[PUB] IARM connected\n");

    /* 3. Register as event owner for all event types */
    IARM_Bus_RegisterEvent(NUM_EVENTS);

    /* 4. Start root span — covers all SC1 + SC2 work */
    rdk_otlp_start_distributed_trace(TEST_OWNER, "pub");
    log_tp("root span started");

    /* Brief pause to ensure sub has completed all handler registrations */
    sleep(1);

    /* ═══════════════════════════════════════════════════════════════════════
     * SC1a — BroadcastEvent: SCAN
     * Sub handler simulates 200 ms of scan-result processing.
     * ════════════════════════════════════════════════════════════════════ */
    printf("\n[PUB] ── SC1a: BroadcastEvent SCAN (id=%d) ──\n", TEST_EVENT_SCAN);
    TestEventData_t evScan = { .value = 1, .label = "device_scan" };
    IARM_Result_t rc = IARM_Bus_BroadcastEvent(TEST_OWNER,
                           (IARM_EventId_t)TEST_EVENT_SCAN, &evScan, sizeof(evScan));
    printf("[PUB] BroadcastEvent(SCAN) returned %d (0=OK)\n", rc);
    log_tp("after SCAN broadcast");
    sleep(1);   /* wait for sub's ~200 ms handler + margin */

    /* ═══════════════════════════════════════════════════════════════════════
     * SC1b — BroadcastEvent: PARSE
     * Sub handler simulates 150 ms of config-parse processing.
     * ════════════════════════════════════════════════════════════════════ */
    printf("\n[PUB] ── SC1b: BroadcastEvent PARSE (id=%d) ──\n", TEST_EVENT_PARSE);
    TestEventData_t evParse = { .value = 2, .label = "parse_config" };
    rc = IARM_Bus_BroadcastEvent(TEST_OWNER,
             (IARM_EventId_t)TEST_EVENT_PARSE, &evParse, sizeof(evParse));
    printf("[PUB] BroadcastEvent(PARSE) returned %d (0=OK)\n", rc);
    log_tp("after PARSE broadcast");
    sleep(1);   /* wait for sub's ~150 ms handler + margin */

    /* ═══════════════════════════════════════════════════════════════════════
     * SC1c — BroadcastEvent: APPLY
     * Sub handler simulates 100 ms of settings-apply processing.
     * ════════════════════════════════════════════════════════════════════ */
    printf("\n[PUB] ── SC1c: BroadcastEvent APPLY (id=%d) ──\n", TEST_EVENT_APPLY);
    TestEventData_t evApply = { .value = 3, .label = "apply_settings" };
    rc = IARM_Bus_BroadcastEvent(TEST_OWNER,
             (IARM_EventId_t)TEST_EVENT_APPLY, &evApply, sizeof(evApply));
    printf("[PUB] BroadcastEvent(APPLY) returned %d (0=OK)\n", rc);
    log_tp("after APPLY broadcast");
    sleep(1);   /* wait for sub's ~100 ms handler + margin */

    /* ═══════════════════════════════════════════════════════════════════════
     * SC2 — RPC path via IARM_Bus_Call() with transparent trace propagation.
     * Sub handler simulates 120 ms of state-query work.
     * Note: owner name MUST match the IARM_Bus_Init member name of the target
     * process, not an arbitrary owner string.
     * ════════════════════════════════════════════════════════════════════ */
    printf("\n[PUB] ── SC2: IARM_Bus_Call() GetTestState (transparent tracing) ──\n");
    TestRpcArg_t rpcArg = { .request_token = 42, .response_msg = "" };
    rc = IARM_Bus_Call("iarm_otel_test_sub", TEST_METHOD,
                       &rpcArg, sizeof(rpcArg));
    printf("[PUB] IARM_Bus_Call() returned %d (0=OK)\n", rc);
    printf("[PUB] RPC response: \"%s\"\n", rpcArg.response_msg);
    log_tp("after RPC call");
    sleep(1);

    /* ── Finish and flush the root span before SC3 ───────────────────────── */
    rdk_otlp_finish_distributed_trace();
    printf("[PUB] root span finished\n");
    rdk_otlp_force_flush();
    sleep(2);   /* allow exporter thread to drain fully */

    /* ═══════════════════════════════════════════════════════════════════════
     * SC3 — Untraced / legacy sender → New receiver
     * Root span is finished: rdk_otlp_get_current_traceparent() returns NULL.
     * libIARMBus writes a zeroed suffix (no 0xAA magic byte).  Sub receives
     * the event, finds no incoming traceparent, skips tracing, and processes
     * the payload normally — no crash, no span created.
     * Validates backward compatibility with uninstrumented / legacy senders.
     * ════════════════════════════════════════════════════════════════════ */
    printf("\n[PUB] ── SC3: BroadcastEvent with no active span (untraced sender) ──\n");
    TestEventData_t evLegacy = { .value = 99, .label = "legacy_event" };
    rc = IARM_Bus_BroadcastEvent(TEST_OWNER,
             (IARM_EventId_t)TEST_EVENT_SCAN, &evLegacy, sizeof(evLegacy));
    printf("[PUB] BroadcastEvent(SC3) returned %d (0=OK)\n", rc);
    printf("[PUB] SC3: sub should receive event, find no traceparent, process normally.\n");
    sleep(1);

    IARM_Bus_Disconnect();
    IARM_Bus_Term();
    rdk_otlp_shutdown();

    printf("\n[PUB] Done.\n");
    printf("[PUB] Expected in Jaeger: 1 trace, 5 spans (root + SC1a/b/c + SC2).\n");
    printf("[PUB] SC3: event received by sub but NO child span — check sub log.\n");
    return 0;
}
