/*
 * iarm_tp_test_mt_pub.c — IARM traceparent pass-through test: concurrency stress
 *
 * Spawns N worker threads, each repeatedly calling IARM_Bus_SetTraceparent()
 * immediately followed by IARM_Bus_Call() / IARM_Bus_BroadcastEvent() from its
 * own thread, all against the same connected IARM_Bus_Init() member.
 *
 * Each thread generates its own synthetic-but-valid W3C traceparent per call
 * (no dependency on any tracing library — plain deterministic hex strings),
 * embeds it in the payload it sends, and:
 *   - RPC path:   the subscriber echoes back whatever IARM_Bus_GetTraceparent()
 *                 returned inside its handler; this process compares the echo
 *                 to what it set for that specific call.
 *   - Event path: the subscriber itself compares its IARM_Bus_GetTraceparent()
 *                 against the traceparent embedded in the event payload, and
 *                 reports any mismatch in its own log.
 *
 * Because IARM_Bus_SetTraceparent()/IARM_Bus_GetTraceparent() are backed by
 * thread-local storage (see core/libIBus-dbus.c), a single-shot value set by
 * one thread must never be visible to, or clobbered by, a concurrent call on
 * another thread. Any mismatch here indicates a thread-safety regression in
 * that TLS handling or in the shared IARM dispatch path.
 *
 * Usage: iarm_tp_test_mt_pub [thread_count] [iterations_per_thread]
 */

#include "libIBus.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>

#define MT_TEST_OWNER        "TP_MT_TEST_OWNER"
#define MT_TEST_EVENT_ID     0
#define MT_TEST_METHOD       "GetTpMtTestState"
#define DEFAULT_THREAD_COUNT 8
#define DEFAULT_ITERATIONS   20
#define TP_BUF_SIZE          64

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

typedef struct {
    int thread_id;
    int iterations;
    int rpc_ok;
    int rpc_mismatch;
    int rpc_fail;
    int event_sent;
} ThreadCtx_t;

/* Deterministic, per-(thread,iteration,tag) valid W3C traceparent - no tracer involved. */
static void make_synthetic_traceparent(char *out, size_t outsz, int thread_id, int iteration, unsigned tag)
{
    unsigned long long span_val = ((unsigned long long)(unsigned)thread_id << 32) | (unsigned)iteration;
    snprintf(out, outsz, "00-%08x%08x%08x%08x-%016llx-01",
             (unsigned)thread_id, (unsigned)iteration, tag, 0xA5A5A5A5u, span_val);
}

static void *worker_thread(void *arg)
{
    ThreadCtx_t *ctx = (ThreadCtx_t *)arg;

    for (int i = 0; i < ctx->iterations; i++) {
        /* ── RPC round trip ─────────────────────────────────────────────── */
        char tp_rpc[TP_BUF_SIZE];
        make_synthetic_traceparent(tp_rpc, sizeof(tp_rpc), ctx->thread_id, i, 0x1111u);
        IARM_Bus_SetTraceparent(tp_rpc);

        TestMtRpcArg_t rpc;
        memset(&rpc, 0, sizeof(rpc));
        rpc.thread_id = ctx->thread_id;
        rpc.iteration = i;
        snprintf(rpc.embedded_tp, sizeof(rpc.embedded_tp), "%s", tp_rpc);

        IARM_Result_t rc = IARM_Bus_Call("iarm_tp_test_mt_sub", MT_TEST_METHOD,
                                          &rpc, sizeof(rpc));
        if (rc != IARM_RESULT_SUCCESS) {
            ctx->rpc_fail++;
            printf("[PUB][thread %d][iter %d] IARM_Bus_Call failed rc=%d\n",
                   ctx->thread_id, i, rc);
        } else if (strcmp(rpc.echoed_tp, tp_rpc) != 0) {
            ctx->rpc_mismatch++;
            printf("[PUB][thread %d][iter %d] MISMATCH sent=%s echoed=%s\n",
                   ctx->thread_id, i, tp_rpc, rpc.echoed_tp);
        } else {
            ctx->rpc_ok++;
        }

        /* ── Event broadcast, self-checked on the receiver side ───────────── */
        char tp_evt[TP_BUF_SIZE];
        make_synthetic_traceparent(tp_evt, sizeof(tp_evt), ctx->thread_id, i, 0x2222u);
        IARM_Bus_SetTraceparent(tp_evt);

        TestMtEventData_t ev;
        memset(&ev, 0, sizeof(ev));
        ev.thread_id = ctx->thread_id;
        ev.iteration = i;
        snprintf(ev.embedded_tp, sizeof(ev.embedded_tp), "%s", tp_evt);

        IARM_Bus_BroadcastEvent(MT_TEST_OWNER, (IARM_EventId_t)MT_TEST_EVENT_ID,
                                 &ev, sizeof(ev));
        ctx->event_sent++;
    }

    return NULL;
}

int main(int argc, char **argv)
{
    int thread_count = DEFAULT_THREAD_COUNT;
    int iterations = DEFAULT_ITERATIONS;

    if (argc > 1) thread_count = atoi(argv[1]);
    if (argc > 2) iterations = atoi(argv[2]);
    if (thread_count <= 0) thread_count = DEFAULT_THREAD_COUNT;
    if (iterations <= 0) iterations = DEFAULT_ITERATIONS;

    printf("[PUB] Starting iarm_tp_test_mt_pub (pid %d) threads=%d iterations=%d\n",
           (int)getpid(), thread_count, iterations);

    IARM_Bus_Init("iarm_tp_test_mt_pub");
    IARM_Bus_Connect();
    IARM_Bus_RegisterEvent(1);
    printf("[PUB] IARM connected\n");

    sleep(1); /* let the subscriber finish registering handlers */

    pthread_t *threads = calloc((size_t)thread_count, sizeof(pthread_t));
    ThreadCtx_t *ctxs = calloc((size_t)thread_count, sizeof(ThreadCtx_t));
    if (!threads || !ctxs) {
        printf("[PUB] OOM allocating thread contexts\n");
        free(threads);
        free(ctxs);
        return 1;
    }

    for (int i = 0; i < thread_count; i++) {
        ctxs[i].thread_id = i;
        ctxs[i].iterations = iterations;
        pthread_create(&threads[i], NULL, worker_thread, &ctxs[i]);
    }

    for (int i = 0; i < thread_count; i++) {
        pthread_join(threads[i], NULL);
    }

    int total_ok = 0, total_mismatch = 0, total_fail = 0, total_events = 0;
    for (int i = 0; i < thread_count; i++) {
        total_ok += ctxs[i].rpc_ok;
        total_mismatch += ctxs[i].rpc_mismatch;
        total_fail += ctxs[i].rpc_fail;
        total_events += ctxs[i].event_sent;
    }
    free(threads);
    free(ctxs);

    sleep(1); /* let the last events land before the subscriber prints its own summary */

    printf("\n[PUB] ── Summary ──\n");
    printf("[PUB] RPC calls: ok=%d mismatch=%d failed=%d (expected %d total)\n",
           total_ok, total_mismatch, total_fail, thread_count * iterations);
    printf("[PUB] Events broadcast: %d\n", total_events);
    printf("[PUB] Check the subscriber log for any event-path MISMATCH lines.\n");

    IARM_Bus_Disconnect();
    IARM_Bus_Term();

    int rc = (total_mismatch == 0 && total_fail == 0) ? 0 : 1;
    printf("[PUB] %s\n", rc == 0 ? "PASS (RPC path)" : "FAIL (RPC path)");
    return rc;
}
