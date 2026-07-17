# OtelPluginTest — Test Commands Reference

## Prerequisites

### Activate plugin (once per boot)
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":1,"method":"Controller.1.activate","params":{"callsign":"OtelPluginTest"}}'
```

### Check logs
```bash
cat /opt/logs/otel_plugin_test.log   # plugin method calls + timing
cat /opt/logs/traceid.log            # traceparents received by PluginServer
cat /opt/logs/traces.log             # OTEL spans exported to collector
cat /opt/logs/shlib.log              # librdk_otlp init / span lifecycle
```

---

## testTrace — Single method, single call, one trace

Single JSON-RPC call to a target plugin. Starts one distributed trace span,
injects traceparent, invokes the method, finishes the span.

### In-process plugin (DisplayInfo)
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":2,"method":"OtelPluginTest.1.testTrace","params":{"callsign":"DisplayInfo.1","method":"tvcapabilities"}}'
```

### Out-of-process plugin (NetworkManager)
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":3,"method":"OtelPluginTest.1.testTrace","params":{"callsign":"org.rdk.NetworkManager.1","method":"GetAvailableInterfaces"}}'
```

### Default (DisplayInfo.1 / tvcapabilities)
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":4,"method":"OtelPluginTest.1.testTrace","params":{}}'
```

**Expected response:**
```json
{"jsonrpc":"2.0","id":2,"result":{"success":true,"result":{...plugin response...}}}
```

---

## testMultiTrace — N different methods, one trace

Multiple different methods called on the same plugin, all under one shared
trace ID. Each method appears as a separate child span with the same parentSpanId.

### In-process plugin (DisplayInfo) — 5 methods
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":5,"method":"OtelPluginTest.1.testMultiTrace","params":{"callsign":"DisplayInfo.1","methods":"tvcapabilities,framerate,totalgpuram,isaudiopassthrough,hdrsetting"}}'
```

### Out-of-process plugin (NetworkManager)
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":6,"method":"OtelPluginTest.1.testMultiTrace","params":{"callsign":"org.rdk.NetworkManager.1","methods":"GetAvailableInterfaces,GetPrimaryInterface,IsConnectedToInternet"}}'
```

### Default (DisplayInfo.1, 5 methods)
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":7,"method":"OtelPluginTest.1.testMultiTrace","params":{}}'
```

**Expected response:**
```json
{"jsonrpc":"2.0","id":5,"result":{"success":true,"results":"[\"tvcapabilities:ok\",\"framerate:ok\",...]"}}
```

**Expected in Jaeger:** One root span + N child spans all sharing the same traceId.

---

## testBurst — Same method called N times, one trace

Stress test: same method called `count` times sequentially under one trace.
Returns timing statistics. All calls are child spans of a single root span.

### In-process, 50 calls
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":8,"method":"OtelPluginTest.1.testBurst","params":{"callsign":"DisplayInfo.1","method":"tvcapabilities","count":50}}'
```

### Out-of-process, 20 calls
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":9,"method":"OtelPluginTest.1.testBurst","params":{"callsign":"org.rdk.NetworkManager.1","method":"GetAvailableInterfaces","count":20}}'
```

### Default (DisplayInfo.1, 10 calls)
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":10,"method":"OtelPluginTest.1.testBurst","params":{}}'
```

**Expected response:**
```json
{
  "success": true,
  "count": 50,
  "success_count": 50,
  "fail_count": 0,
  "total_ms": 245.3,
  "avg_ms": 4.906,
  "min_ms": 3.1,
  "max_ms": 12.4
}
```

---

## testLatency — With vs Without OTEL overhead comparison

Runs `iterations` rounds WITHOUT OTEL (no span, no traceparent injection),
then `iterations` rounds WITH OTEL active. Reports the timing difference.

### Single method, in-process, 30 iterations
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":11,"method":"OtelPluginTest.1.testLatency","params":{"callsign":"DisplayInfo.1","method":"tvcapabilities","iterations":30}}'
```

### Single method, out-of-process, 30 iterations
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":12,"method":"OtelPluginTest.1.testLatency","params":{"callsign":"org.rdk.NetworkManager.1","method":"GetAvailableInterfaces","iterations":30}}'
```

### Multi-method per iteration, in-process
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":13,"method":"OtelPluginTest.1.testLatency","params":{"callsign":"DisplayInfo.1","methods":"tvcapabilities,framerate,totalgpuram","iterations":20}}'
```

### Multi-method per iteration, out-of-process
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":14,"method":"OtelPluginTest.1.testLatency","params":{"callsign":"org.rdk.NetworkManager.1","methods":"GetAvailableInterfaces,GetPrimaryInterface","iterations":20}}'
```

### Default (DisplayInfo.1, tvcapabilities, 20 iterations)
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":15,"method":"OtelPluginTest.1.testLatency","params":{}}'
```

**Expected response:**
```json
{
  "success": true,
  "iterations": 30,
  "methods_per_iter": 1,
  "avg_without_ms": 4.82,
  "avg_with_ms": 4.91,
  "overhead_ms": 0.09,
  "overhead_pct": 1.87
}
```

---

## Concurrent / Worker Pool Stress Tests

These fire multiple requests simultaneously to hit different Thunder worker threads.

### 5 concurrent testTrace requests
```bash
for i in 1 2 3 4 5; do
  curl -s -X POST http://127.0.0.1:9998/jsonrpc \
    -d '{"jsonrpc":"2.0","id":'$i',"method":"OtelPluginTest.1.testTrace","params":{"callsign":"DisplayInfo.1","method":"tvcapabilities"}}' &
done
wait
```

### Mixed in-process + out-of-process concurrent burst
```bash
curl -s -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":1,"method":"OtelPluginTest.1.testBurst","params":{"callsign":"DisplayInfo.1","method":"tvcapabilities","count":10}}' &
curl -s -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":2,"method":"OtelPluginTest.1.testBurst","params":{"callsign":"org.rdk.NetworkManager.1","method":"GetAvailableInterfaces","count":10}}' &
wait
```

### Concurrent latency comparison (in-process vs OOP simultaneously)
```bash
curl -s -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":1,"method":"OtelPluginTest.1.testLatency","params":{"callsign":"DisplayInfo.1","method":"tvcapabilities","iterations":20}}' &
curl -s -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":2,"method":"OtelPluginTest.1.testLatency","params":{"callsign":"org.rdk.NetworkManager.1","method":"GetAvailableInterfaces","iterations":20}}' &
wait
```

---

## testComRpc — COM-RPC path via QueryInterfaceByCallsign

Acquires `IDispatcher` via COM-RPC (`QueryInterfaceByCallsign`) instead of JSON-RPC websocket.
For **OOP plugins** (NetworkManager in WPEProcess) each `Invoke()` call goes through
`UnknownProxy::Invoke()` in `libWPEFrameworkCOM.so` and generates a child span automatically.
For **in-process plugins** (DisplayInfo) a direct vtable pointer is returned — no IPC, no span (expected).

### OOP plugin — NetworkManager (triggers COM-RPC child spans)
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":20,"method":"OtelPluginTest.1.testComRpc","params":{"callsign":"org.rdk.NetworkManager.1","method":"GetAvailableInterfaces","count":5}}'
```

### OOP plugin — multiple calls to see separate child spans per call
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":21,"method":"OtelPluginTest.1.testComRpc","params":{"callsign":"org.rdk.NetworkManager.1","method":"GetPrimaryInterface","count":10}}'
```

### In-process plugin — DisplayInfo (no COM-RPC, no span — verifies guard works)
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":22,"method":"OtelPluginTest.1.testComRpc","params":{"callsign":"DisplayInfo.1","method":"tvcapabilities","count":5}}'
```

### Default (NetworkManager, GetAvailableInterfaces, 5 calls)
```bash
curl -X POST http://127.0.0.1:9998/jsonrpc \
  -d '{"jsonrpc":"2.0","id":23,"method":"OtelPluginTest.1.testComRpc","params":{}}'
```

**Expected response (OOP):**
```json
{"success":true,"count":5,"success_count":5,"fail_count":0,"avg_ms":243.5,"transport":"comrpc"}
```

**Expected in Jaeger (OOP):** One root span `comrpc.org.rdk.NetworkManager.1.GetAvailableInterfaces`
with N children each named `COMRPC.if0x<InterfaceId>.method0` — one per `Invoke()` call.

**Note:** The `COMRPC.if0x...` span name encodes the interface ID (hex) and method index.
`IDispatcher::Invoke` is method 0 of the IDispatcher interface, so span name will be
`COMRPC.if0x<IDispatcher::ID>.method0`.

---



| Parameter | Methods | Type | Default | Notes |
|---|---|---|---|---|
| `callsign` | all | string | `DisplayInfo.1` | Thunder plugin callsign with version |
| `method` | testTrace, testBurst, testLatency | string | `tvcapabilities` | Single method name |
| `methods` | testMultiTrace, testLatency | string | 5 DisplayInfo methods | Comma-separated method names |
| `count` | testBurst | number | `10` | Number of repeat calls (max 500) |
| `iterations` | testLatency | number | `20` | Rounds per without/with run (max 200) |

## Known In-Process Plugins (DisplayInfo)
- `tvcapabilities`
- `framerate`
- `totalgpuram`
- `freegpuram`
- `isaudiopassthrough`
- `hdrsetting`

## Known Out-Of-Process Plugins (NetworkManager)
- `GetAvailableInterfaces`
- `GetPrimaryInterface`
- `IsConnectedToInternet`
- `GetIPSettings`
