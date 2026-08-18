/*
 * iarm_otel_test2_pub.c - Compatibility runner for pending scenarios.
 *
 * Scenarios:
 *   new_to_legacy : traced sender -> legacy receiver (event + RPC)
 *   legacy_to_new : untraced sender -> new receiver (event + RPC)
 *   sc3           : traced sender finishes root, then sends untraced event
 */

#include "libIBus.h"
#include "rdk_otlp_instrumentation.h"

#include <stdio.h>
#include <string.h>
#include <unistd.h>

#define TEST2_OWNER      "OTEL_TEST2_OWNER"
#define TEST2_EVENT_ID   0
#define TEST2_METHOD     "GetCompatState"
#define TEST2_SUB_MEMBER "iarm_otel_test2_sub"

typedef struct {
    int value;
    char label[32];
} Test2EventData_t;

typedef struct {
    int request_token;
    char response_msg[64];
} Test2RpcArg_t;

static void usage(const char *prog)
{
    printf("Usage: %s <new_to_legacy|legacy_to_new|sc3>\n", prog);
}

static void send_event(const char *label, int value)
{
    Test2EventData_t ev;
    IARM_Result_t rc;

    ev.value = value;
    snprintf(ev.label, sizeof(ev.label), "%s", label);

    rc = IARM_Bus_BroadcastEvent(TEST2_OWNER, (IARM_EventId_t)TEST2_EVENT_ID,
                                 &ev, sizeof(ev));
    printf("[PUB2] BroadcastEvent label=%s rc=%d\n", ev.label, rc);
}

static void send_rpc_legacy(int token)
{
    Test2RpcArg_t arg;
    IARM_Result_t rc;

    memset(&arg, 0, sizeof(arg));
    arg.request_token = token;

    rc = IARM_Bus_Call(TEST2_SUB_MEMBER, TEST2_METHOD, &arg, sizeof(arg));
    printf("[PUB2] Call(legacy) rc=%d response=\"%s\"\n", rc, arg.response_msg);
}

static void send_rpc_traced(int token)
{
    Test2RpcArg_t arg;
    IARM_Result_t rc;

    memset(&arg, 0, sizeof(arg));
    arg.request_token = token;

    rc = IARM_Bus_CallWithTracing(TEST2_SUB_MEMBER, TEST2_METHOD,
                                  &arg, sizeof(arg));
    printf("[PUB2] CallWithTracing rc=%d response=\"%s\"\n", rc, arg.response_msg);
}

int main(int argc, char **argv)
{
    const char *scenario;

    if (argc != 2) {
        usage(argv[0]);
        return 1;
    }
    scenario = argv[1];

    printf("[PUB2] Starting scenario: %s\n", scenario);

    IARM_Bus_Init("iarm_otel_test2_pub");
    IARM_Bus_Connect();
    IARM_Bus_RegisterEvent(1);

    if (strcmp(scenario, "new_to_legacy") == 0) {
        /*
         * New sender -> Legacy receiver.
         *
         * EVENT path: patched libIARMBus appends a traceparent suffix when a
         * root span is active.  The legacy receiver reads only data[0..len-1]
         * and ignores the suffix bytes silently — no crash, correct payload.
         *
         * RPC path: we deliberately use IARM_Bus_Call (NOT CallWithTracing)
         * because IARM_Bus_CallWithTracing wraps the arg in an RPCA envelope.
         * A legacy handler that doesn't know the envelope would see corrupted
         * fields and may crash.  The correct contract is: CallWithTracing is
         * used only when both sender AND receiver have the patched library.
         * This test proves plain RPC still works across the boundary.
         */
        rdk_otlp_init("iarm-otel-poc-pub2-new", "1.0.0");
        rdk_otlp_start_distributed_trace("OTEL_TEST2_NEW_TO_LEGACY", "pub2");

        send_event("new_to_legacy_event", 101);
        sleep(1);
        /* Use legacy call — not CallWithTracing — against legacy receiver. */
        send_rpc_legacy(101);

        rdk_otlp_finish_distributed_trace();
        rdk_otlp_force_flush();
        sleep(1);
        rdk_otlp_shutdown();
    } else if (strcmp(scenario, "legacy_to_new") == 0) {
        /* No otel init, no active span -> emulates legacy sender behavior. */
        send_event("legacy_to_new_event", 202);
        sleep(1);
        send_rpc_legacy(202);
    } else if (strcmp(scenario, "sc3") == 0) {
        rdk_otlp_init("iarm-otel-poc-pub2-sc3", "1.0.0");
        rdk_otlp_start_distributed_trace("OTEL_TEST2_SC3", "pub2");
        rdk_otlp_finish_distributed_trace();
        rdk_otlp_force_flush();

        /* Event emitted after root ends -> receiver should see no incoming tp. */
        send_event("sc3_post_root_event", 303);
        sleep(1);

        rdk_otlp_shutdown();
    } else {
        usage(argv[0]);
        IARM_Bus_Disconnect();
        IARM_Bus_Term();
        return 1;
    }

    IARM_Bus_Disconnect();
    IARM_Bus_Term();

    printf("[PUB2] Done scenario: %s\n", scenario);
    return 0;
}
