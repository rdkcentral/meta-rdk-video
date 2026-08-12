/*
 * iarm_otel_test_sub.c — IARM OTel POC: Subscriber / RPC Handler
 *
 * This process:
 *   1. Optionally calls rdk_otlp_init() so that child spans are exported from
 *      this process to the same OTLP collector.
 *   2. Registers an IARM event handler for OTEL_TEST_OWNER / event 0.
 *   3. Registers an IARM RPC handler for OTEL_TEST_OWNER / GetTestState.
 *   4. Waits for 30 s (or until signalled) then exits cleanly.
 *
 * Transport-only model:
 *   • libIARMBus propagates traceparent and exposes it via
 *     IARM_Bus_GetCurrentIncomingTraceparent().
 *   • Handler code decides whether to create child spans.
 *
 * Verification:
 *   After both processes exit, open the OTLP collector UI (or scrape
 *   /opt/logs/rdk_otel_tracer.log on both processes) and confirm:
 *     • 3 spans share one trace_id
 *     • span "IARM.OTEL_TEST_OWNER.event0" is a child of "pub-root-span"
 *     • span "IARM.OTEL_TEST_OWNER.GetTestState" is a child of "pub-root-span"
 *
 * Direct-linking failure indicator:
 *   If isTracingEnabled() causes a crash or deadlock in processes that never
 *   called rdk_otlp_init(), switch to the dlsym approach.
 */

#include "libIBus.h"
#include "rdk_otlp_instrumentation.h"

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <stdlib.h>

/* ── Shared test constants (must match iarm_otel_test_pub.c) ─────────────── */
#define TEST_OWNER      "OTEL_TEST_OWNER"
#define TEST_EVENT_ID   0
#define TEST_METHOD     "GetTestState"

typedef struct {
    int  value;
    char label[32];
} TestEventData_t;

typedef struct {
    int  request_token;
    char response_msg[64];
} TestRpcArg_t;

/* ── Flag: set by SIGTERM / SIGINT to break the wait loop ────────────────── */
static volatile int g_stop = 0;
static void _sig_handler(int sig) { (void)sig; g_stop = 1; }

/* ── Event handler — handler-controlled span lifecycle ───────────────────── */
static void _on_test_event(const char *owner, IARM_EventId_t eventId,
                           void *data, size_t len)
{
    int span_started = 0;
    const char *incoming_tp = IARM_Bus_GetCurrentIncomingTraceparent();
    if (incoming_tp) {
        char span_name[96];
        snprintf(span_name, sizeof(span_name), "IARM.%s.event%d",
                 owner, (int)eventId);
        rdk_otlp_start_child_from_traceparent(incoming_tp, span_name);
        span_started = 1;
    }

    TestEventData_t *ev = (TestEventData_t *)data;
    printf("[SUB] Event received: owner=%s id=%d value=%d label=\"%s\"\n",
           owner, (int)eventId, ev->value, ev->label);

    /* Demonstrate that child span is active inside handler. */
    const char *tp = rdk_otlp_get_current_traceparent();
    printf("[SUB]   current traceparent inside handler: %s\n",
           tp ? tp : "(none)");
    printf("[SUB]   incoming parent traceparent: %s\n",
           incoming_tp ? incoming_tp : "(none)");

    if (span_started) {
        rdk_otlp_finish_child_span();
    }
}

/* ── RPC handler — handler-controlled span lifecycle ─────────────────────── */
static IARM_Result_t _on_get_test_state(void *arg)
{
    int span_started = 0;
    const char *incoming_tp = IARM_Bus_GetCurrentIncomingTraceparent();
    if (incoming_tp) {
        rdk_otlp_start_child_from_traceparent(incoming_tp,
                                              "IARM.OTEL_TEST_OWNER.GetTestState");
        span_started = 1;
    }

    TestRpcArg_t *rpc = (TestRpcArg_t *)arg;
    printf("[SUB] RPC called: request_token=%d\n", rpc->request_token);

    /* Same chain-check as above */
    const char *tp = rdk_otlp_get_current_traceparent();
    printf("[SUB]   current traceparent inside RPC handler: %s\n",
           tp ? tp : "(none)");
    printf("[SUB]   incoming parent traceparent: %s\n",
           incoming_tp ? incoming_tp : "(none)");

    /* Fill response — caller receives this via IARM_Bus_Call's result copy */
    snprintf(rpc->response_msg, sizeof(rpc->response_msg),
             "ok-token-%d", rpc->request_token);

    if (span_started) {
        rdk_otlp_finish_child_span();
    }
    return IARM_RESULT_SUCCESS;
}

int main(void)
{
    printf("[SUB] Starting iarm_otel_test_sub (pid %d)\n", (int)getpid());

    signal(SIGTERM, _sig_handler);
    signal(SIGINT,  _sig_handler);

    /*
     * Initialize OTel so child spans are actually exported from this process.
     *
     * DIRECT-LINKING RISK NOTE:
     *   Even without this call, rdk_otlp_start_child_from_traceparent and
     *   rdk_otlp_finish_child_span are no-ops (g_tracer == NULL guard).
     *   However rdk_otlp_finish_child_span() calls isTracingEnabled() before
     *   the g_tracer guard.  That runs one-time inotify init + RFC flag file
     *   read.  This is harmless but measurable.  Comment this block out to
     *   test the "uninstrumented subscriber" scenario.
     */
    rdk_otlp_init("iarm-otel-poc-sub", "1.0.0");
    printf("[SUB] rdk_otlp_init done\n");

    /* Connect to IARM */
    IARM_Bus_Init("iarm_otel_test_sub");
    IARM_Bus_Connect();
    printf("[SUB] IARM connected\n");

    /* Register event handler */
    IARM_Bus_RegisterEventHandler(TEST_OWNER,
                                  (IARM_EventId_t)TEST_EVENT_ID,
                                  _on_test_event);
    printf("[SUB] Registered event handler for %s / event %d\n",
           TEST_OWNER, TEST_EVENT_ID);

    /* Register RPC handler */
    IARM_Bus_RegisterCall(TEST_METHOD, _on_get_test_state);
    printf("[SUB] Registered RPC handler for %s\n", TEST_METHOD);

    printf("[SUB] Waiting for events/RPC calls (30 s or SIGTERM) ...\n");

    /* Wait loop — 30 s hard limit so the process doesn't hang in CI */
    int waited = 0;
    while (!g_stop && waited < 30) {
        sleep(1);
        waited++;
    }

    rdk_otlp_force_flush();
    sleep(1);

    IARM_Bus_Disconnect();
    IARM_Bus_Term();
    rdk_otlp_shutdown();

    printf("[SUB] Exiting.\n");
    return 0;
}
