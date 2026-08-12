/*
 * iarm_otel_test_pub.c — IARM OTel POC: Publisher / RPC Caller
 *
 * This process:
 *   1. Calls rdk_otlp_init() to activate tracing (only needed here because
 *      this process CREATES the root span; the subscriber handler decides
 *      whether to create child spans).
 *   2. Starts a root distributed trace span.
 *   3. Broadcasts an IARM event — libIARMBus injects the current traceparent
 *      into the event suffix automatically.
 *   4. Makes an RPC call via IARM_Bus_CallWithTracing() — the traceparent is
 *      carried in an IARM_RPC_Envelope_t.
 *   5. Finishes the root span and shuts down.
 *
 * Expected outcome in the OTLP collector:
 *   Trace tree:
 *     [root: iarm-otel-poc-pub / "pub-root-span"]
 *       └─ [child: iarm-otel-poc-sub / "IARM.OTEL_TEST_OWNER.event0"]
 *       └─ [child: iarm-otel-poc-sub / "IARM.OTEL_TEST_OWNER.GetTestState"]
 *
 * All three spans should share the same trace_id.
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
#define TEST_OWNER      "OTEL_TEST_OWNER"
#define TEST_EVENT_ID   0
#define TEST_METHOD     "GetTestState"

typedef struct {
    int  request_token;
    char response_msg[64];
} TestRpcArg_t;

typedef struct {
    int  value;
    char label[32];
} TestEventData_t;

/* ── Helpers ─────────────────────────────────────────────────────────────── */
static void log_tp(const char *tag)
{
    const char *tp = rdk_otlp_get_current_traceparent();
    printf("[PUB] %s — traceparent: %s\n", tag,
           tp ? tp : "(none — not traced)");
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

    /* 3. Register ourselves as event owner so we can broadcast */
    IARM_Bus_RegisterEvent(1 /* max event id + 1 */);

    /* 4. Start the root span */
    rdk_otlp_start_distributed_trace(TEST_OWNER, "pub");
    log_tp("root span started");

    /* Give the subscriber a moment to be ready (in automated test the
     * orchestration script handles ordering; this sleep is a fallback) */
    sleep(1);

    /* ── Test 1: Event path ──────────────────────────────────────────────── */
    printf("\n[PUB] === TEST 1: BroadcastEvent ===\n");
    TestEventData_t evData = { .value = 42, .label = "hello-from-pub" };

    /* libIARMBus will call rdk_otlp_get_current_traceparent() internally and
     * append the suffix to the shared-memory event block. */
    IARM_Result_t rc = IARM_Bus_BroadcastEvent(TEST_OWNER,
                                               (IARM_EventId_t)TEST_EVENT_ID,
                                               &evData, sizeof(evData));
    printf("[PUB] BroadcastEvent returned %d (0=OK)\n", rc);
    log_tp("after broadcast");

    /* Give subscriber time to process and export the child span */
    sleep(2);

    /* ── Test 2: RPC path ────────────────────────────────────────────────── */
    printf("\n[PUB] === TEST 2: CallWithTracing ===\n");
    TestRpcArg_t rpcArg = { .request_token = 7, .response_msg = "" };

    /* IARM_Bus_CallWithTracing() wraps the arg in IARM_RPC_Envelope_t and
     * delivers it to the subscriber's _BusCall_FuncWrapper. */
    rc = IARM_Bus_CallWithTracing(TEST_OWNER, TEST_METHOD,
                                  &rpcArg, sizeof(rpcArg));
    printf("[PUB] CallWithTracing returned %d (0=OK)\n", rc);
    printf("[PUB] RPC response: \"%s\"\n", rpcArg.response_msg);
    log_tp("after rpc call");

    /* 5. Finish root span — will be exported to OTLP collector */
    rdk_otlp_finish_distributed_trace();
    printf("[PUB] root span finished\n");

    rdk_otlp_force_flush();
    sleep(1); /* allow exporter thread to drain */

    IARM_Bus_Disconnect();
    IARM_Bus_Term();
    rdk_otlp_shutdown();

    printf("[PUB] Done. Check OTLP collector for trace with 3 spans.\n");
    printf("[PUB] All spans must share the same trace_id.\n");
    return 0;
}
