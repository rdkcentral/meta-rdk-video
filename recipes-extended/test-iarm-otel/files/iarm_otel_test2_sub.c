/*
 * iarm_otel_test2_sub.c - Receiver in either new or legacy mode.
 *
 * Modes:
 *   new    : starts child spans from incoming traceparent when present.
 *   legacy : never touches tracing APIs (payload-only handling).
 */

#include "libIBus.h"
#include "rdk_otlp_instrumentation.h"

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <stdlib.h>

#define TEST2_OWNER    "OTEL_TEST2_OWNER"
#define TEST2_EVENT_ID 0
#define TEST2_METHOD   "GetCompatState"

typedef struct {
    int value;
    char label[32];
} Test2EventData_t;

typedef struct {
    int request_token;
    char response_msg[64];
} Test2RpcArg_t;

static volatile int g_stop = 0;
static int g_new_mode = 0;

static void on_sig(int sig)
{
    (void)sig;
    g_stop = 1;
}

static int span_start_if_new(const char *span_name)
{
    const char *incoming_tp;

    if (!g_new_mode) {
        return 0;
    }

    incoming_tp = IARM_Bus_GetCurrentIncomingTraceparent();
    if (!incoming_tp) {
        printf("[SUB2][new] no incoming traceparent -> no child span\n");
        return 0;
    }

    printf("[SUB2][new] incoming traceparent: %s\n", incoming_tp);
    rdk_otlp_start_child_from_traceparent(incoming_tp, span_name);
    return 1;
}

static void on_event(const char *owner, IARM_EventId_t id, void *data, size_t len)
{
    Test2EventData_t *ev = (Test2EventData_t *)data;
    int started;

    (void)id;
    (void)len;

    printf("[SUB2] Event owner=%s value=%d label=\"%s\" mode=%s\n",
           owner, ev->value, ev->label, g_new_mode ? "new" : "legacy");

    started = span_start_if_new("IARM.OTEL_TEST2_OWNER.CompatEvent");

    usleep(50000);

    if (started) {
        rdk_otlp_finish_child_span();
    }
}

static IARM_Result_t on_rpc(void *arg)
{
    Test2RpcArg_t *rpc = (Test2RpcArg_t *)arg;
    int started;

    printf("[SUB2] RPC token=%d mode=%s\n",
           rpc->request_token, g_new_mode ? "new" : "legacy");

    started = span_start_if_new("IARM.OTEL_TEST2_OWNER.GetCompatState");

    usleep(70000);

    snprintf(rpc->response_msg, sizeof(rpc->response_msg),
             "compat-ok-token-%d-%s",
             rpc->request_token,
             g_new_mode ? "new" : "legacy");

    if (started) {
        rdk_otlp_finish_child_span();
    }

    return IARM_RESULT_SUCCESS;
}

static int parse_mode(int argc, char **argv)
{
    if (argc != 2) {
        return -1;
    }
    if (strcmp(argv[1], "new") == 0) {
        g_new_mode = 1;
        return 0;
    }
    if (strcmp(argv[1], "legacy") == 0) {
        g_new_mode = 0;
        return 0;
    }
    return -1;
}

int main(int argc, char **argv)
{
    int waited;

    if (parse_mode(argc, argv) != 0) {
        printf("Usage: %s <new|legacy>\n", argv[0]);
        return 1;
    }

    printf("[SUB2] Starting mode=%s\n", g_new_mode ? "new" : "legacy");

    signal(SIGTERM, on_sig);
    signal(SIGINT, on_sig);

    if (g_new_mode) {
        rdk_otlp_init("iarm-otel-poc-sub2-new", "1.0.0");
    }

    IARM_Bus_Init("iarm_otel_test2_sub");
    IARM_Bus_Connect();

    IARM_Bus_RegisterEventHandler(TEST2_OWNER, (IARM_EventId_t)TEST2_EVENT_ID,
                                  on_event);
    IARM_Bus_RegisterCall(TEST2_METHOD, on_rpc);

    printf("[SUB2] Ready. Waiting up to 45s...\n");

    waited = 0;
    while (!g_stop && waited < 45) {
        sleep(1);
        waited++;
    }

    if (g_new_mode) {
        rdk_otlp_force_flush();
        sleep(1);
    }

    IARM_Bus_Disconnect();
    IARM_Bus_Term();

    if (g_new_mode) {
        rdk_otlp_shutdown();
    }

    printf("[SUB2] Exiting\n");
    return 0;
}
