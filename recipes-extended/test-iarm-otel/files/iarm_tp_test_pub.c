/*
 * iarm_tp_test_pub.c — IARM traceparent pass-through test: Publisher / RPC Caller
 *
 * Demonstrates the set_traceparent()/get_traceparent() model: IARM itself
 * never links or calls into any tracing library. This process links
 * librdk_otlp only to obtain its own current traceparent, then hands that
 * opaque string to IARM_Bus_SetTraceparent() immediately before each call.
 *
 *   SC1 — BroadcastEvent with an active span: traceparent is set before each
 *         of two events. Sub creates a child span per event.
 *   SC2 — IARM_Bus_Call with an active span: traceparent is set before the
 *         RPC. Sub creates a child span covering handler execution.
 *   SC3 — BroadcastEvent after the root span is finished: no traceparent is
 *         set, sub must not create a span and must process the event normally.
 */

#include "libIBus.h"
#include "rdk_otlp_instrumentation.h"

#include <stdio.h>
#include <string.h>
#include <unistd.h>

#define TEST_OWNER       "TP_TEST_OWNER"
#define TEST_EVENT_SCAN  0
#define TEST_EVENT_PARSE 1
#define NUM_EVENTS       2
#define TEST_METHOD      "GetTpTestState"

typedef struct {
    int  value;
    char label[32];
} TestEventData_t;

typedef struct {
    int  request_token;
    char response_msg[64];
} TestRpcArg_t;

static void set_outgoing_tp_if_active(const char *tag)
{
    const char *tp = rdk_otlp_get_current_traceparent();
    printf("[PUB] %s — current traceparent: %s\n", tag, tp ? tp : "(none)");
    /* IARM_Bus_SetTraceparent() only ever sees this opaque string; NULL clears it. */
    IARM_Bus_SetTraceparent(tp);
}

int main(void)
{
    printf("[PUB] Starting iarm_tp_test_pub (pid %d)\n", (int)getpid());

    rdk_otlp_init("iarm-tp-poc-pub", "1.0.0");
    printf("[PUB] rdk_otlp_init done\n");

    IARM_Bus_Init("iarm_tp_test_pub");
    IARM_Bus_Connect();
    printf("[PUB] IARM connected\n");

    IARM_Bus_RegisterEvent(NUM_EVENTS);

    rdk_otlp_start_distributed_trace(TEST_OWNER, "pub");
    sleep(1);   /* let sub finish registering handlers */

    /* ── SC1a — BroadcastEvent SCAN with an active span ────────────────── */
    printf("\n[PUB] ── SC1a: BroadcastEvent SCAN (id=%d) ──\n", TEST_EVENT_SCAN);
    TestEventData_t evScan = { .value = 1, .label = "device_scan" };
    set_outgoing_tp_if_active("before SCAN broadcast");
    IARM_Result_t rc = IARM_Bus_BroadcastEvent(TEST_OWNER,
                           (IARM_EventId_t)TEST_EVENT_SCAN, &evScan, sizeof(evScan));
    printf("[PUB] BroadcastEvent(SCAN) returned %d (0=OK)\n", rc);
    sleep(1);

    /* ── SC1b — BroadcastEvent PARSE with an active span ───────────────── */
    printf("\n[PUB] ── SC1b: BroadcastEvent PARSE (id=%d) ──\n", TEST_EVENT_PARSE);
    TestEventData_t evParse = { .value = 2, .label = "parse_config" };
    set_outgoing_tp_if_active("before PARSE broadcast");
    rc = IARM_Bus_BroadcastEvent(TEST_OWNER,
             (IARM_EventId_t)TEST_EVENT_PARSE, &evParse, sizeof(evParse));
    printf("[PUB] BroadcastEvent(PARSE) returned %d (0=OK)\n", rc);
    sleep(1);

    /* ── SC2 — RPC call with an active span ────────────────────────────── */
    printf("\n[PUB] ── SC2: IARM_Bus_Call GetTpTestState ──\n");
    TestRpcArg_t rpcArg = { .request_token = 42, .response_msg = "" };
    set_outgoing_tp_if_active("before RPC call");
    rc = IARM_Bus_Call("iarm_tp_test_sub", TEST_METHOD, &rpcArg, sizeof(rpcArg));
    printf("[PUB] IARM_Bus_Call returned %d (0=OK)\n", rc);
    printf("[PUB] RPC response: \"%s\"\n", rpcArg.response_msg);
    sleep(1);

    rdk_otlp_finish_distributed_trace();
    printf("[PUB] root span finished\n");
    rdk_otlp_force_flush();
    sleep(2);

    /* ── SC3 — BroadcastEvent with no active span, no traceparent set ─── */
    printf("\n[PUB] ── SC3: BroadcastEvent with no active span (untraced) ──\n");
    TestEventData_t evLegacy = { .value = 99, .label = "no_span_event" };
    rc = IARM_Bus_BroadcastEvent(TEST_OWNER,
             (IARM_EventId_t)TEST_EVENT_SCAN, &evLegacy, sizeof(evLegacy));
    printf("[PUB] BroadcastEvent(SC3) returned %d (0=OK)\n", rc);
    printf("[PUB] SC3: sub should receive event, find no traceparent, process normally.\n");
    sleep(1);

    IARM_Bus_Disconnect();
    IARM_Bus_Term();
    rdk_otlp_shutdown();

    printf("\n[PUB] Done.\n");
    printf("[PUB] Expected in Jaeger: 1 trace, 4 spans (root + SC1a/b + SC2). SC3 produces no span.\n");
    return 0;
}
