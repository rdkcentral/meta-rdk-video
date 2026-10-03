/*
 * iarm_tp_test_mt_sub.c — subscriber for the concurrency stress test.
 *
 * RPC handler: echoes back whatever IARM_Bus_GetTraceparent() returns inside
 * the handler, so the (multithreaded) caller can verify the round trip.
 * Also starts a real child span from that traceparent via rdk_otlp, exactly
 * like the single-threaded test, to exercise the tracer under load too.
 *
 * Event handler: self-checks by comparing IARM_Bus_GetTraceparent() against
 * the traceparent the sender embedded in the event payload itself, since
 * there is no synchronous reply on the event path. Mismatches are logged and
 * counted; the running totals are printed periodically and at exit.
 */

#include "libIBus.h"
#include "rdk_otlp_instrumentation.h"

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <stdlib.h>
#include <pthread.h>

#define MT_TEST_OWNER    "TP_MT_TEST_OWNER"
#define MT_TEST_EVENT_ID 0
#define MT_TEST_METHOD   "GetTpMtTestState"
#define TP_BUF_SIZE      64

typedef struct {
    int  thread_id;
    int  iteration;
    char embedded_tp[TP_BUF_SIZE];
} TestMtEventData_t;

typedef struct {
    int  thread_id;
    int  iteration;
    char embedded_tp[TP_BUF_SIZE];
    char echoed_tp[TP_BUF_SIZE];
} TestMtRpcArg_t;

static volatile int g_stop = 0;
static void _sig_handler(int sig) { (void)sig; g_stop = 1; }

static pthread_mutex_t g_stats_lock = PTHREAD_MUTEX_INITIALIZER;
static long g_events_ok = 0;
static long g_events_mismatch = 0;
static long g_rpc_calls = 0;

static void _on_mt_event(const char *owner, IARM_EventId_t eventId, void *data, size_t len)
{
    (void)owner;
    (void)eventId;
    (void)len;
    TestMtEventData_t *ev = (TestMtEventData_t *)data;

    const char *incoming_tp = IARM_Bus_GetTraceparent();
    int match = incoming_tp && (strcmp(incoming_tp, ev->embedded_tp) == 0);
    if (incoming_tp) {
        rdk_otlp_start_child_from_traceparent(incoming_tp, "IARM.TP_MT_TEST_OWNER.Event");
        rdk_otlp_finish_child_span();
    }

    pthread_mutex_lock(&g_stats_lock);
    if (match) {
        g_events_ok++;
    } else {
        g_events_mismatch++;
        printf("[SUB][thread %d][iter %d] EVENT MISMATCH embedded=%s incoming=%s\n",
               ev->thread_id, ev->iteration, ev->embedded_tp,
               incoming_tp ? incoming_tp : "(none)");
    }
    pthread_mutex_unlock(&g_stats_lock);
}

static IARM_Result_t _on_mt_rpc(void *arg)
{
    TestMtRpcArg_t *rpc = (TestMtRpcArg_t *)arg;

    const char *incoming_tp = IARM_Bus_GetTraceparent();
    snprintf(rpc->echoed_tp, sizeof(rpc->echoed_tp), "%s", incoming_tp ? incoming_tp : "");
    if (incoming_tp) {
        rdk_otlp_start_child_from_traceparent(incoming_tp, "IARM.TP_MT_TEST_OWNER.GetTpMtTestState");
        rdk_otlp_finish_child_span();
    }

    pthread_mutex_lock(&g_stats_lock);
    g_rpc_calls++;
    pthread_mutex_unlock(&g_stats_lock);

    return IARM_RESULT_SUCCESS;
}

int main(void)
{
    printf("[SUB] Starting iarm_tp_test_mt_sub (pid %d)\n", (int)getpid());

    signal(SIGTERM, _sig_handler);
    signal(SIGINT,  _sig_handler);

    rdk_otlp_init("iarm-tp-mt-poc-sub", "1.0.0");
    printf("[SUB] rdk_otlp_init done\n");

    IARM_Bus_Init("iarm_tp_test_mt_sub");
    IARM_Bus_Connect();
    printf("[SUB] IARM connected\n");

    IARM_Bus_RegisterEventHandler(MT_TEST_OWNER, (IARM_EventId_t)MT_TEST_EVENT_ID,
                                  _on_mt_event);
    IARM_Bus_RegisterCall(MT_TEST_METHOD, _on_mt_rpc);
    printf("[SUB] Registered MT event handler and RPC handler (%s)\n", MT_TEST_METHOD);

    printf("[SUB] Ready. Waiting for concurrent calls/events (60 s or SIGTERM) ...\n");

    int waited = 0;
    while (!g_stop && waited < 60) {
        sleep(1);
        waited++;
    }

    pthread_mutex_lock(&g_stats_lock);
    printf("\n[SUB] ── Summary ──\n");
    printf("[SUB] RPC calls handled: %ld\n", g_rpc_calls);
    printf("[SUB] Events matched: %ld, mismatched: %ld\n", g_events_ok, g_events_mismatch);
    printf("[SUB] %s\n", g_events_mismatch == 0 ? "PASS (event path)" : "FAIL (event path)");
    pthread_mutex_unlock(&g_stats_lock);

    IARM_Bus_Disconnect();
    IARM_Bus_Term();
    rdk_otlp_force_flush();
    rdk_otlp_shutdown();

    printf("[SUB] Exiting.\n");
    return (g_events_mismatch == 0) ? 0 : 1;
}
