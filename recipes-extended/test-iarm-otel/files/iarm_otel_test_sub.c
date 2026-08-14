/*
 * iarm_otel_test_sub.c — IARM OTel POC: Subscriber / RPC Handler
 *
 * Handles the following scenarios exercised by iarm_otel_test_pub:
 *
 * SC1 — New sender → New receiver, EVENT path
 *   Three dedicated event handlers, each creating a child span and simulating
 *   realistic processing delays (different per event type) so that Jaeger shows
 *   a visually meaningful timeline:
 *     ScanComplete  (TEST_EVENT_SCAN  = 0)  ~200 ms
 *     ParseConfig   (TEST_EVENT_PARSE = 1)  ~150 ms
 *     ApplySettings (TEST_EVENT_APPLY = 2)  ~100 ms
 *
 * SC2 — New sender → New receiver, RPC path
 *   GetTestState handler creates a child span and simulates ~120 ms of
 *   state-query work.
 *
 * SC3 — Untraced / legacy sender → New receiver
 *   When IARM_Bus_GetCurrentIncomingTraceparent() returns NULL (sender had no
 *   active span), the handler skips tracing entirely and processes the event
 *   normally.  No crash, no orphan span.
 */

#include "libIBus.h"
#include "rdk_otlp_instrumentation.h"

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <stdlib.h>

/* ── Shared test constants (must match iarm_otel_test_pub.c) ─────────────── */
#define TEST_OWNER         "OTEL_TEST_OWNER"
#define TEST_EVENT_SCAN    0
#define TEST_EVENT_PARSE   1
#define TEST_EVENT_APPLY   2
#define TEST_METHOD        "GetTestState"

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

/* ── Helper: start child span from incoming traceparent, or skip if none ─── */
static int span_start(const char *incoming_tp, const char *span_name)
{
    if (!incoming_tp) {
        printf("[SUB]   SC3: no incoming traceparent - untraced/legacy sender. "
               "Skipping tracing, processing normally.\n");
        return 0;
    }
    printf("[SUB]   incoming traceparent: %s\n", incoming_tp);
    rdk_otlp_start_child_from_traceparent(incoming_tp, span_name);
    printf("[SUB]   child span started: %s\n", span_name);
    return 1;
}

/* ── SC1a handler: SCAN event — simulates 200 ms of processing ─────────── */
static void _on_event_scan(const char *owner, IARM_EventId_t eventId,
                           void *data, size_t len)
{
    (void)eventId;
    (void)len;
    TestEventData_t *ev = (TestEventData_t *)data;
    printf("[SUB] SCAN event: owner=%s value=%d label=\"%s\"\n",
           owner, ev->value, ev->label);

    int started = span_start(IARM_Bus_GetCurrentIncomingTraceparent(),
                             "IARM.OTEL_TEST_OWNER.ScanComplete");

    /* Simulate scan-result processing: validate, store, notify */
    printf("[SUB]   [scan] validating result ... "); fflush(stdout);
    usleep(80000);   /* 80 ms */
    printf("ok\n");
    printf("[SUB]   [scan] writing to state cache ... "); fflush(stdout);
    usleep(70000);   /* 70 ms */
    printf("ok\n");
    printf("[SUB]   [scan] notifying dependents ... "); fflush(stdout);
    usleep(50000);   /* 50 ms */
    printf("ok  (total ~200 ms)\n");

    if (started) rdk_otlp_finish_child_span();
}

/* ── SC1b handler: PARSE event — simulates 150 ms of processing ────────── */
static void _on_event_parse(const char *owner, IARM_EventId_t eventId,
                            void *data, size_t len)
{
    (void)eventId;
    (void)len;
    TestEventData_t *ev = (TestEventData_t *)data;
    printf("[SUB] PARSE event: owner=%s value=%d label=\"%s\"\n",
           owner, ev->value, ev->label);

    int started = span_start(IARM_Bus_GetCurrentIncomingTraceparent(),
                             "IARM.OTEL_TEST_OWNER.ParseConfig");

    /* Simulate config parse: read, validate schema, build internal model */
    printf("[SUB]   [parse] reading config blob ... "); fflush(stdout);
    usleep(60000);   /* 60 ms */
    printf("ok\n");
    printf("[SUB]   [parse] validating schema ... "); fflush(stdout);
    usleep(50000);   /* 50 ms */
    printf("ok\n");
    printf("[SUB]   [parse] building model ... "); fflush(stdout);
    usleep(40000);   /* 40 ms */
    printf("ok  (total ~150 ms)\n");

    if (started) rdk_otlp_finish_child_span();
}

/* ── SC1c handler: APPLY event — simulates 100 ms of processing ────────── */
static void _on_event_apply(const char *owner, IARM_EventId_t eventId,
                            void *data, size_t len)
{
    (void)eventId;
    (void)len;
    TestEventData_t *ev = (TestEventData_t *)data;
    printf("[SUB] APPLY event: owner=%s value=%d label=\"%s\"\n",
           owner, ev->value, ev->label);

    int started = span_start(IARM_Bus_GetCurrentIncomingTraceparent(),
                             "IARM.OTEL_TEST_OWNER.ApplySettings");

    /* Simulate settings apply: backup, write, verify */
    printf("[SUB]   [apply] backing up current settings ... "); fflush(stdout);
    usleep(40000);   /* 40 ms */
    printf("ok\n");
    printf("[SUB]   [apply] writing new settings ... "); fflush(stdout);
    usleep(35000);   /* 35 ms */
    printf("ok\n");
    printf("[SUB]   [apply] verifying write ... "); fflush(stdout);
    usleep(25000);   /* 25 ms */
    printf("ok  (total ~100 ms)\n");

    if (started) rdk_otlp_finish_child_span();
}

/* ── SC2 handler: GetTestState RPC — simulates 120 ms of processing ────── */
static IARM_Result_t _on_get_test_state(void *arg)
{
    TestRpcArg_t *rpc = (TestRpcArg_t *)arg;
    printf("[SUB] RPC GetTestState: request_token=%d\n", rpc->request_token);

    int started = span_start(IARM_Bus_GetCurrentIncomingTraceparent(),
                             "IARM.OTEL_TEST_OWNER.GetTestState");

    /* Simulate state query: lookup, format, return */
    printf("[SUB]   [rpc] querying device state ... "); fflush(stdout);
    usleep(70000);   /* 70 ms */
    printf("ok\n");
    printf("[SUB]   [rpc] formatting response ... "); fflush(stdout);
    usleep(50000);   /* 50 ms */
    printf("ok  (total ~120 ms)\n");

    snprintf(rpc->response_msg, sizeof(rpc->response_msg),
             "state-ok-token-%d", rpc->request_token);

    if (started) rdk_otlp_finish_child_span();
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

    /* Register handlers for all 3 event types */
    IARM_Bus_RegisterEventHandler(TEST_OWNER, (IARM_EventId_t)TEST_EVENT_SCAN,
                                  _on_event_scan);
    printf("[SUB] Registered SCAN  handler (event %d) — simulates ~200 ms\n",
           TEST_EVENT_SCAN);
    IARM_Bus_RegisterEventHandler(TEST_OWNER, (IARM_EventId_t)TEST_EVENT_PARSE,
                                  _on_event_parse);
    printf("[SUB] Registered PARSE handler (event %d) — simulates ~150 ms\n",
           TEST_EVENT_PARSE);
    IARM_Bus_RegisterEventHandler(TEST_OWNER, (IARM_EventId_t)TEST_EVENT_APPLY,
                                  _on_event_apply);
    printf("[SUB] Registered APPLY handler (event %d) — simulates ~100 ms\n",
           TEST_EVENT_APPLY);

    /* Register RPC handler */
    IARM_Bus_RegisterCall(TEST_METHOD, _on_get_test_state);
    printf("[SUB] Registered RPC   handler (%s) — simulates ~120 ms\n",
           TEST_METHOD);

    printf("[SUB] Ready. Waiting for events/RPC calls (60 s or SIGTERM) ...\n");

    /* Wait loop — 60 s hard limit */
    int waited = 0;
    while (!g_stop && waited < 60) {
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
