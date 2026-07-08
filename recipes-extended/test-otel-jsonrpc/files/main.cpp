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
 */

#include <rdk_otlp_instrumentation.h>

#include <core/core.h>
#include <websocket/websocket.h>

#include <iostream>
#include <string>
#include <thread>
#include <chrono>

using namespace WPEFramework;

static constexpr const char* COMPONENT_NAME    = "otel-jsonrpc-test";
static constexpr const char* COMPONENT_VERSION = "1.0.0";
static constexpr uint32_t    CALL_TIMEOUT_MS   = 5000;

// Target plugin callsign - change to match your test environment
static const std::string TARGET_CALLSIGN = "org.rdk.System";

/**
 * Test 1: 10 independent parent traces, each with one JSON-RPC call.
 * Rapid-fire to stress-test trace context propagation.
 */
static void test_independent_parent_traces(JSONRPC::LinkType<Core::JSON::IElement>& link)
{
    std::cout << "\n=== TEST 1: Independent parent traces (10 iterations) ===" << std::endl;

    for (int i = 0; i < 10; i++) {
        std::string key = "test.param." + std::to_string(i);

        // Start a new parent trace
        rdk_otlp_start_distributed_trace(key.c_str(), "get");

        // Get the traceparent that will be injected
        const char* tp = rdk_otlp_get_current_traceparent();
        std::cout << "[" << i << "] traceparent: " << (tp ? tp : "(null)") << std::endl;

        // Invoke JSON-RPC - the Thunder websocket layer auto-injects traceparent
        Core::ProxyType<Core::JSONRPC::Message> response;
        uint32_t result = link.Invoke(CALL_TIMEOUT_MS, "getSystemVersions", "{}", response);

        if (result == Core::ERROR_NONE && response.IsValid()) {
            std::cout << "[" << i << "] Response OK" << std::endl;
        } else {
            std::cout << "[" << i << "] Call failed: " << result << std::endl;
        }

        // Finish the parent trace
        rdk_otlp_finish_distributed_trace();

        // Clear shared memory entry for this key
        rdk_otlp_clear_trace_context(key.c_str());
    }

    // Flush to ensure all spans are exported before next test
    rdk_otlp_force_flush();
    std::cout << "=== TEST 1 COMPLETE ===" << std::endl;
}

/**
 * Test 2: Single parent trace with multiple different JSON-RPC calls.
 * All child spans should map to the same parent trace ID.
 */
static void test_single_parent_multiple_calls(JSONRPC::LinkType<Core::JSON::IElement>& link)
{
    std::cout << "\n=== TEST 2: Single parent, multiple children ===" << std::endl;

    const std::string key = "test.multi.call";

    // Start one parent trace
    rdk_otlp_start_distributed_trace(key.c_str(), "composite");

    const char* tp = rdk_otlp_get_current_traceparent();
    std::cout << "Parent traceparent: " << (tp ? tp : "(null)") << std::endl;

    // Call several different methods under the same parent
    const char* methods[] = {
        "getSystemVersions",
        "getDeviceInfo",
        "getMilestones",
        "getCoreTemperature",
        "getLastDeepSleepReason"
    };

    for (const char* method : methods) {
        Core::ProxyType<Core::JSONRPC::Message> response;
        uint32_t result = link.Invoke(CALL_TIMEOUT_MS, method, "{}", response);

        if (result == Core::ERROR_NONE && response.IsValid()) {
            std::cout << "  " << method << " -> OK" << std::endl;
        } else {
            std::cout << "  " << method << " -> failed (" << result << ")" << std::endl;
        }
    }

    // Finish the parent trace
    rdk_otlp_finish_distributed_trace();
    rdk_otlp_clear_trace_context(key.c_str());

    rdk_otlp_force_flush();
    std::cout << "=== TEST 2 COMPLETE ===" << std::endl;
}

int main(int argc, char* argv[])
{
    std::string callsign = TARGET_CALLSIGN;
    if (argc > 1) {
        callsign = argv[1];
    }

    std::cout << "OTEL JSON-RPC Trace Propagation Test" << std::endl;
    std::cout << "Target callsign: " << callsign << std::endl;

    // Initialize OTEL tracer
    rdk_otlp_init(COMPONENT_NAME, COMPONENT_VERSION);

    // Allow tracer startup
    std::this_thread::sleep_for(std::chrono::milliseconds(500));

    // Create Thunder JSON-RPC client
    // Requires THUNDER_ACCESS env var (e.g. "127.0.0.1:9998")
    const char* thunder_access = std::getenv("THUNDER_ACCESS");
    if (!thunder_access) {
        // Default to localhost Thunder
        Core::SystemInfo::SetEnvironment("THUNDER_ACCESS", "127.0.0.1:9998");
        std::cout << "THUNDER_ACCESS not set, defaulting to 127.0.0.1:9998" << std::endl;
    } else {
        std::cout << "THUNDER_ACCESS: " << thunder_access << std::endl;
    }

    JSONRPC::LinkType<Core::JSON::IElement> link(callsign, false);

    // Wait for connection
    std::this_thread::sleep_for(std::chrono::seconds(1));

    // Run tests
    test_independent_parent_traces(link);

    // Brief pause between tests
    std::this_thread::sleep_for(std::chrono::milliseconds(200));

    test_single_parent_multiple_calls(link);

    // Final flush and shutdown
    std::this_thread::sleep_for(std::chrono::seconds(2));
    rdk_otlp_shutdown();

    std::cout << "\nAll tests completed. Check Jaeger/collector for trace data." << std::endl;
    return 0;
}
