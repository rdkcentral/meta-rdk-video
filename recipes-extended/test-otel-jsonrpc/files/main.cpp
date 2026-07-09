/**
 * OTEL Thunder JSON-RPC Trace Propagation Test App
 *
 * Test 1: Invokes the same JSON-RPC method 10 times, each with a unique
 *          parent trace. Validates that each call gets its own distinct
 *          parent-child mapping in the collector.
 *
 * Test 2: Creates a single parent trace and invokes multiple different
 *          JSON-RPC methods under it. Validates all child spans share
 *          the same parent trace ID.
 *
 * Uses curl to send JSON-RPC requests to Thunder's HTTP endpoint,
 * injecting traceparent into params. This tests the full E2E path:
 *   test app (parent span) -> Thunder PluginServer (child span)
 */

#include <rdk_otlp_instrumentation.h>

#include <iostream>
#include <string>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <thread>
#include <chrono>
#include <sstream>
#include <array>

static constexpr const char* COMPONENT_NAME    = "otel-jsonrpc-test";
static constexpr const char* COMPONENT_VERSION = "1.0.0";

static std::string g_thunder_url = "http://127.0.0.1:9998/jsonrpc";

/**
 * Execute a curl command and return the response body.
 */
static std::string exec_curl(const std::string& json_body, const std::string& callsign)
{
    std::string url = g_thunder_url + "/" + callsign;

    std::string cmd = "curl -s -X POST "
                      "-H 'Content-Type: application/json' "
                      "-d '" + json_body + "' "
                      "'" + url + "' 2>&1";

    std::array<char, 4096> buffer;
    std::string result;
    FILE* pipe = popen(cmd.c_str(), "r");
    if (!pipe) return "(popen failed)";
    while (fgets(buffer.data(), buffer.size(), pipe) != nullptr) {
        result += buffer.data();
    }
    pclose(pipe);
    return result;
}

/**
 * Build a JSON-RPC request body with traceparent injected into params.
 */
static std::string build_jsonrpc_request(const std::string& method,
                                          const char* traceparent,
                                          int id)
{
    std::ostringstream oss;
    oss << "{\"jsonrpc\":\"2.0\",\"id\":" << id
        << ",\"method\":\"" << method << "\"";

    if (traceparent) {
        oss << ",\"params\":{\"traceparent\":\"" << traceparent << "\"}";
    }

    oss << "}";
    return oss.str();
}

/**
 * Test 1: 10 independent parent traces, each with one JSON-RPC call.
 */
static void test_independent_parent_traces(const std::string& callsign)
{
    std::cout << "\n=== TEST 1: Independent parent traces (10 iterations) ===" << std::endl;

    for (int i = 0; i < 10; i++) {
        std::string key = "test.param." + std::to_string(i);

        // Start a new parent trace
        rdk_otlp_start_distributed_trace(key.c_str(), "get");

        // Get the traceparent for this parent span
        const char* tp = rdk_otlp_get_current_traceparent();
        std::cout << "[" << i << "] traceparent: " << (tp ? tp : "(null)") << std::endl;

        // Send JSON-RPC request with traceparent
        std::string body = build_jsonrpc_request("tvcapabilities", tp, i + 1);
        std::string resp = exec_curl(body, callsign);
        std::cout << "[" << i << "] response: " << resp.substr(0, 120) << std::endl;

        // Finish the parent trace
        rdk_otlp_finish_distributed_trace();
        rdk_otlp_clear_trace_context(key.c_str());
    }

    rdk_otlp_force_flush();
    std::cout << "=== TEST 1 COMPLETE ===" << std::endl;
}

/**
 * Test 2: Single parent trace with multiple different JSON-RPC calls.
 * All child spans should map to the same parent trace ID.
 */
static void test_single_parent_multiple_calls(const std::string& callsign)
{
    std::cout << "\n=== TEST 2: Single parent, multiple children ===" << std::endl;

    const std::string key = "test.multi.call";

    // Start one parent trace
    rdk_otlp_start_distributed_trace(key.c_str(), "composite");

    const char* tp = rdk_otlp_get_current_traceparent();
    std::cout << "Parent traceparent: " << (tp ? tp : "(null)") << std::endl;

    const char* methods[] = {
        "tvcapabilities",
        "framerate",
        "totalgpuram",
        "isaudiopassthrough",
        "hdrsetting"
    };

    int id = 100;
    for (const char* method : methods) {
        std::string body = build_jsonrpc_request(method, tp, id++);
        std::string resp = exec_curl(body, callsign);
        std::cout << "  " << method << " -> " << resp.substr(0, 100) << std::endl;
    }

    // Finish the parent trace
    rdk_otlp_finish_distributed_trace();
    rdk_otlp_clear_trace_context(key.c_str());

    rdk_otlp_force_flush();
    std::cout << "=== TEST 2 COMPLETE ===" << std::endl;
}

int main(int argc, char* argv[])
{
    std::string callsign = "DisplayInfo.1";
    if (argc > 1) {
        callsign = argv[1];
    }

    const char* thunder_url_env = std::getenv("THUNDER_URL");
    if (thunder_url_env) {
        g_thunder_url = thunder_url_env;
    }

    std::cout << "OTEL JSON-RPC Trace Propagation Test" << std::endl;
    std::cout << "Target callsign: " << callsign << std::endl;
    std::cout << "Thunder URL: " << g_thunder_url << std::endl;

    // Initialize OTEL tracer
    rdk_otlp_init(COMPONENT_NAME, COMPONENT_VERSION);
    std::this_thread::sleep_for(std::chrono::milliseconds(500));

    // Run tests
    test_independent_parent_traces(callsign);

    std::this_thread::sleep_for(std::chrono::milliseconds(200));

    test_single_parent_multiple_calls(callsign);

    // Final flush and shutdown
    std::this_thread::sleep_for(std::chrono::seconds(2));
    rdk_otlp_shutdown();

    std::cout << "\nAll tests completed. Check Jaeger/collector for trace data." << std::endl;
    return 0;
}
