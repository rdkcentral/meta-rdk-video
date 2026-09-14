/*
 * iarm_tp_test_sub.c — IARM traceparent pass-through test: Subscriber / RPC Handler
 *
 * Reads the incoming traceparent purely via IARM_Bus_GetTraceparent() - a
 * plain string accessor with no tracing dependency inside IARM itself. This
 * process links librdk_otlp only to create the child span from that string.
 */

#include "libIBus.h"
#include "rdk_otlp_instrumentation.h"

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <stdlib.h>

#define TEST_OWNER       "TP_TEST_OWNER"
#define TEST_EVENT_SCAN  0
#define TEST_EVENT_PARSE 1
#define TEST_METHOD      "GetTpTestState"

typedef struct {
    int  value;
    char label[32];
} TestEventData_t;

typedef struct {
    int  request_token;
    char response_msg[64];
} TestRpcArg_t;

static volatile int g_stop = 0;
static void _sig_handler(int sig) { (void)sig; g_stop = 1; }

/* ── Helper: start child span from IARM's incoming traceparent, or skip ──── */
static int span_start(const char *span_name)
{
    const char *incoming_tp = IARM_Bus_GetTraceparent();
    if (!incoming_tp) {
        printf("[SUB]   no incoming traceparent - untraced sender. "
               "Skipping tracing, processing normally.\n");
        return 0;
    }
    printf("[SUB]   incoming traceparent: %s\n", incoming_tp);
    rdk_otlp_start_child_from_traceparent(incoming_tp, span_name);
    printf("[SUB]   child span started: %s\n", span_name);
    return 1;
}

static void _on_event_scan(const char *owner, IARM_EventId_t eventId,
                           void *data, size_t len)
{
    (void)eventId;
    (void)len;
    TestEventData_t *ev = (TestEventData_t *)data;
    printf("[SUB] SCAN event: owner=%s value=%d label=\"%s\"\n",
           owner, ev->value, ev->label);

    int started = span_start("IARM.TP_TEST_OWNER.ScanComplete");
    usleep(80000);
    if (started) rdk_otlp_finish_child_span();
}

static void _on_event_parse(const char *owner, IARM_EventId_t eventId,
                            void *data, size_t len)
{
    (void)eventId;
    (void)len;
    TestEventData_t *ev = (TestEventData_t *)data;
    printf("[SUB] PARSE event: owner=%s value=%d label=\"%s\"\n",
           owner, ev->value, ev->label);

    int started = span_start("IARM.TP_TEST_OWNER.ParseConfig");
    usleep(60000);
    if (started) rdk_otlp_finish_child_span();
}

static IARM_Result_t _on_get_tp_test_state(void *arg)
{
    TestRpcArg_t *rpc = (TestRpcArg_t *)arg;
    printf("[SUB] RPC GetTpTestState: request_token=%d\n", rpc->request_token);

    int started = span_start("IARM.TP_TEST_OWNER.GetTpTestState");
    usleep(70000);

    snprintf(rpc->response_msg, sizeof(rpc->response_msg),
             "tp-state-ok-token-%d", rpc->request_token);

    if (started) rdk_otlp_finish_child_span();
    return IARM_RESULT_SUCCESS;
}

int main(void)
{
    printf("[SUB] Starting iarm_tp_test_sub (pid %d)\n", (int)getpid());

    signal(SIGTERM, _sig_handler);
    signal(SIGINT,  _sig_handler);

    rdk_otlp_init("iarm-tp-poc-sub", "1.0.0");
    printf("[SUB] rdk_otlp_init done\n");

    IARM_Bus_Init("iarm_tp_test_sub");
    IARM_Bus_Connect();
    printf("[SUB] IARM connected\n");

    IARM_Bus_RegisterEventHandler(TEST_OWNER, (IARM_EventId_t)TEST_EVENT_SCAN,
                                  _on_event_scan);
    IARM_Bus_RegisterEventHandler(TEST_OWNER, (IARM_EventId_t)TEST_EVENT_PARSE,
                                  _on_event_parse);
    printf("[SUB] Registered SCAN and PARSE event handlers\n");

    IARM_Bus_RegisterCall(TEST_METHOD, _on_get_tp_test_state);
    printf("[SUB] Registered RPC handler (%s)\n", TEST_METHOD);

    printf("[SUB] Ready. Waiting for events/RPC calls (60 s or SIGTERM) ...\n");

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
