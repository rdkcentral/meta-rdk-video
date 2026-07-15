#ifndef MODULE_NAME
#define MODULE_NAME OtelPluginTest
#endif

#include <core/core.h>
#include <plugins/plugins.h>
#include <chrono>
#include <vector>

#if __has_include("rdk_otlp_instrumentation.h")
#include "rdk_otlp_instrumentation.h"
#define RDK_OTEL_ENABLED 1
#else
#define RDK_OTEL_ENABLED 0
#endif

MODULE_NAME_DECLARATION("OtelPluginTest")

namespace WPEFramework
{
    namespace Plugin
    {
        class OtelPluginTest : public PluginHost::IPlugin, public PluginHost::JSONRPC
        {
        public:
            OtelPluginTest()
                : PluginHost::JSONRPC()
                , _service(nullptr)
            {
                Register(_T("testTrace"),    &OtelPluginTest::TestTrace,    this);
                Register(_T("testMultiTrace"), &OtelPluginTest::TestMultiTrace, this);
                Register(_T("testBurst"),    &OtelPluginTest::TestBurst,    this);
                Register(_T("testLatency"),  &OtelPluginTest::TestLatency,  this);
            }

            ~OtelPluginTest() override
            {
                Unregister(_T("testTrace"));
                Unregister(_T("testMultiTrace"));
                Unregister(_T("testBurst"));
                Unregister(_T("testLatency"));
            }

            virtual const string Initialize(PluginHost::IShell* service) override
            {
                SYSLOG(Logging::Startup, (_T("OtelPluginTest: Initializing")));
                _service = service;
                Core::SystemInfo::SetEnvironment(_T("THUNDER_ACCESS"), (_T("127.0.0.1:9998")));
                return string();
            }

            virtual void Deinitialize(PluginHost::IShell* service) override
            {
                SYSLOG(Logging::Startup, (_T("OtelPluginTest: Deinitializing")));
                _service = nullptr;
            }

            virtual string Information() const override { return string(); }

        private:
            using Clock = std::chrono::high_resolution_clock;
            using Us    = std::chrono::microseconds;

            static int64_t nowUs()
            {
                return std::chrono::duration_cast<Us>(Clock::now().time_since_epoch()).count();
            }

            static std::shared_ptr<WPEFramework::JSONRPC::SmartLinkType<Core::JSON::IElement>>
            makeLink(const string& callsign)
            {
                return std::make_shared<WPEFramework::JSONRPC::SmartLinkType<Core::JSON::IElement>>(
                    callsign, _T("OtelPluginTest"), _T(""));
            }

        public:
            // ---------------------------------------------------------------
            // testTrace: single method, single call, one trace
            // {"callsign":"DisplayInfo.1","method":"tvcapabilities"}
            // ---------------------------------------------------------------
            uint32_t TestTrace(const JsonObject& parameters, JsonObject& response)
            {
                string callsign = parameters.HasLabel("callsign") ? parameters["callsign"].String() : "DisplayInfo.1";
                string method   = parameters.HasLabel("method")   ? parameters["method"].String()   : "tvcapabilities";

                auto link = makeLink(callsign);

#if RDK_OTEL_ENABLED
                string opName = callsign + "." + method;
                rdk_otlp_start_distributed_trace(opName.c_str(), "invoke");
#endif
                JsonObject params, result;
                uint32_t errorCode = Core::ERROR_UNAVAILABLE;
                if (link) errorCode = link->Invoke<JsonObject, JsonObject>(5000, method, params, result);
#if RDK_OTEL_ENABLED
                rdk_otlp_finish_distributed_trace();
#endif

                if (errorCode == Core::ERROR_NONE) {
                    response["success"] = true;
                    response["result"]  = result;
                } else {
                    response["success"]   = false;
                    response["errorCode"] = static_cast<uint32_t>(errorCode);
                }
                return errorCode == Core::ERROR_NONE ? Core::ERROR_NONE : errorCode;
            }

            // ---------------------------------------------------------------
            // testMultiTrace: N different methods, one trace
            // {"callsign":"DisplayInfo.1","methods":"tvcapabilities,framerate,..."}
            // ---------------------------------------------------------------
            uint32_t TestMultiTrace(const JsonObject& parameters, JsonObject& response)
            {
                string callsign = parameters.HasLabel("callsign") ? parameters["callsign"].String() : "DisplayInfo.1";

                std::vector<string> methods;
                if (parameters.HasLabel("methods")) {
                    string s = parameters["methods"].String();
                    size_t pos = 0, found;
                    while ((found = s.find(',', pos)) != string::npos) {
                        methods.push_back(s.substr(pos, found - pos));
                        pos = found + 1;
                    }
                    methods.push_back(s.substr(pos));
                }
                if (methods.empty())
                    methods = { "tvcapabilities", "framerate", "totalgpuram", "isaudiopassthrough", "hdrsetting" };

                auto link = makeLink(callsign);

#if RDK_OTEL_ENABLED
                string opName = callsign + ".multi";
                rdk_otlp_start_distributed_trace(opName.c_str(), "multi-invoke");
#endif
                Core::JSON::ArrayType<Core::JSON::String> results;
                uint32_t lastError = Core::ERROR_NONE;
                for (const auto& method : methods) {
                    JsonObject params, result;
                    uint32_t errorCode = Core::ERROR_UNAVAILABLE;
                    if (link) errorCode = link->Invoke<JsonObject, JsonObject>(5000, method, params, result);
                    if (errorCode != Core::ERROR_NONE) lastError = errorCode;
                    Core::JSON::String entry;
                    entry = method + ":" + (errorCode == Core::ERROR_NONE ? "ok" : "err");
                    results.Add(entry);
                }
#if RDK_OTEL_ENABLED
                rdk_otlp_finish_distributed_trace();
#endif
                string resultsStr;
                results.ToString(resultsStr);
                response["success"] = (lastError == Core::ERROR_NONE);
                response["results"] = resultsStr;
                return Core::ERROR_NONE;
            }

            // ---------------------------------------------------------------
            // testBurst: same method called N times, all under one trace
            // {"callsign":"DisplayInfo.1","method":"tvcapabilities","count":20}
            // Returns: total_ms, avg_ms, min_ms, max_ms, success_count, fail_count
            // ---------------------------------------------------------------
            uint32_t TestBurst(const JsonObject& parameters, JsonObject& response)
            {
                string callsign = parameters.HasLabel("callsign") ? parameters["callsign"].String() : "DisplayInfo.1";
                string method   = parameters.HasLabel("method")   ? parameters["method"].String()   : "tvcapabilities";
                uint32_t count  = parameters.HasLabel("count")    ? static_cast<uint32_t>(parameters["count"].Number()) : 10;
                if (count == 0 || count > 500) count = 10;

                auto link = makeLink(callsign);

#if RDK_OTEL_ENABLED
                string opName = callsign + "." + method + ".burst";
                rdk_otlp_start_distributed_trace(opName.c_str(), "burst");
#endif
                uint32_t successCount = 0, failCount = 0;
                int64_t totalUs = 0, minUs = INT64_MAX, maxUs = 0;

                for (uint32_t i = 0; i < count; i++) {
                    JsonObject params, result;
                    int64_t t0 = nowUs();
                    uint32_t errorCode = Core::ERROR_UNAVAILABLE;
                    if (link) errorCode = link->Invoke<JsonObject, JsonObject>(5000, method, params, result);
                    int64_t elapsed = nowUs() - t0;

                    if (errorCode == Core::ERROR_NONE) successCount++;
                    else failCount++;

                    totalUs += elapsed;
                    if (elapsed < minUs) minUs = elapsed;
                    if (elapsed > maxUs) maxUs = elapsed;
                }
#if RDK_OTEL_ENABLED
                rdk_otlp_finish_distributed_trace();
#endif
                int64_t avgUs = count > 0 ? totalUs / count : 0;

                response["success"]       = (failCount == 0);
                response["count"]         = static_cast<uint32_t>(count);
                response["success_count"] = static_cast<uint32_t>(successCount);
                response["fail_count"]    = static_cast<uint32_t>(failCount);
                response["total_ms"]      = static_cast<double>(totalUs) / 1000.0;
                response["avg_ms"]        = static_cast<double>(avgUs)   / 1000.0;
                response["min_ms"]        = static_cast<double>(minUs)   / 1000.0;
                response["max_ms"]        = static_cast<double>(maxUs)   / 1000.0;

                FILE* fp = fopen("/opt/logs/otel_plugin_test.log", "a");
                if (fp) {
                    fprintf(fp, "[OtelPluginTest] testBurst: callsign=%s method=%s count=%u avg_ms=%.3f min_ms=%.3f max_ms=%.3f ok=%u fail=%u\n",
                        callsign.c_str(), method.c_str(), count,
                        static_cast<double>(avgUs)/1000.0, static_cast<double>(minUs)/1000.0, static_cast<double>(maxUs)/1000.0,
                        successCount, failCount);
                    fclose(fp);
                }
                return Core::ERROR_NONE;
            }

            // ---------------------------------------------------------------
            // testLatency: compare Invoke() timing WITH vs WITHOUT OTEL
            // {"callsign":"DisplayInfo.1","method":"tvcapabilities","iterations":20}
            // OR for multi-method: add "methods":"m1,m2,m3" (overrides method)
            // Runs `iterations` rounds each without and with OTEL active.
            // Returns avg_without_ms, avg_with_ms, overhead_ms, overhead_pct
            // ---------------------------------------------------------------
            uint32_t TestLatency(const JsonObject& parameters, JsonObject& response)
            {
                string callsign   = parameters.HasLabel("callsign")   ? parameters["callsign"].String()   : "DisplayInfo.1";
                string method     = parameters.HasLabel("method")     ? parameters["method"].String()     : "tvcapabilities";
                uint32_t iters    = parameters.HasLabel("iterations") ? static_cast<uint32_t>(parameters["iterations"].Number()) : 20;
                if (iters == 0 || iters > 200) iters = 20;

                std::vector<string> methods;
                if (parameters.HasLabel("methods")) {
                    string s = parameters["methods"].String();
                    size_t pos = 0, found;
                    while ((found = s.find(',', pos)) != string::npos) {
                        methods.push_back(s.substr(pos, found - pos));
                        pos = found + 1;
                    }
                    methods.push_back(s.substr(pos));
                }
                bool isMulti = !methods.empty();
                if (!isMulti) methods = { method };

                // Create link once — reused for both without/with runs
                auto link = makeLink(callsign);

                // --- WITHOUT OTEL: no span started, no traceparent injected ---
                int64_t totalWithoutUs = 0;
                for (uint32_t i = 0; i < iters; i++) {
                    int64_t t0 = nowUs();
                    for (const auto& m : methods) {
                        JsonObject params, result;
                        if (link) link->Invoke<JsonObject, JsonObject>(5000, m, params, result);
                    }
                    totalWithoutUs += nowUs() - t0;
                }
                double avgWithoutUs = static_cast<double>(totalWithoutUs) / iters;

                // --- WITH OTEL: span active, traceparent injected each call ---
                int64_t totalWithUs = 0;
                for (uint32_t i = 0; i < iters; i++) {
#if RDK_OTEL_ENABLED
                    string opName = isMulti ? (callsign + ".latency.multi") : (callsign + "." + method + ".latency");
                    rdk_otlp_start_distributed_trace(opName.c_str(), "latency");
#endif
                    int64_t t0 = nowUs();
                    for (const auto& m : methods) {
                        JsonObject params, result;
                        if (link) link->Invoke<JsonObject, JsonObject>(5000, m, params, result);
                    }
                    totalWithUs += nowUs() - t0;
#if RDK_OTEL_ENABLED
                    rdk_otlp_finish_distributed_trace();
#endif
                }
                double avgWithUs = static_cast<double>(totalWithUs) / iters;

                double overheadUs  = avgWithUs - avgWithoutUs;
                double overheadPct = avgWithoutUs > 0 ? (overheadUs / avgWithoutUs) * 100.0 : 0.0;

                response["success"]         = true;
                response["iterations"]      = static_cast<uint32_t>(iters);
                response["methods_per_iter"] = static_cast<uint32_t>(methods.size());
                response["avg_without_ms"]  = avgWithoutUs / 1000.0;
                response["avg_with_ms"]     = avgWithUs    / 1000.0;
                response["overhead_ms"]     = overheadUs   / 1000.0;
                response["overhead_pct"]    = overheadPct;

                FILE* fp = fopen("/opt/logs/otel_plugin_test.log", "a");
                if (fp) {
                    fprintf(fp, "[OtelPluginTest] testLatency: callsign=%s iters=%u methods=%zu "
                        "avg_without_ms=%.3f avg_with_ms=%.3f overhead_ms=%.3f overhead_pct=%.1f%%\n",
                        callsign.c_str(), iters, methods.size(),
                        avgWithoutUs/1000.0, avgWithUs/1000.0, overheadUs/1000.0, overheadPct);
                    fclose(fp);
                }
                return Core::ERROR_NONE;
            }

            BEGIN_INTERFACE_MAP(OtelPluginTest)
            INTERFACE_ENTRY(PluginHost::IPlugin)
            INTERFACE_ENTRY(PluginHost::IDispatcher)
            END_INTERFACE_MAP

        private:
            PluginHost::IShell* _service;
        };

        static Plugin::Metadata<Plugin::OtelPluginTest> metadata(
            1, 0, 0, {}, {}, {}
        );

    } // namespace Plugin
} // namespace WPEFramework

