#ifndef MODULE_NAME
#define MODULE_NAME OtelPluginTest
#endif

#include <core/core.h>
#include <plugins/plugins.h>

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
                Register(_T("testTrace"), &OtelPluginTest::TestTrace, this);
                Register(_T("testMultiTrace"), &OtelPluginTest::TestMultiTrace, this);
            }

            ~OtelPluginTest() override
            {
                Unregister(_T("testTrace"));
                Unregister(_T("testMultiTrace"));
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

            virtual string Information() const override
            {
                return string();
            }

            // JSON-RPC: testTrace
            // Params: {"callsign":"DisplayInfo.1","method":"tvcapabilities"}
            // Starts a distributed trace, invokes one method on a target plugin
            // with traceparent auto-injected, then finishes the trace.
            uint32_t TestTrace(const JsonObject& parameters, JsonObject& response)
            {
                string callsign = parameters.HasLabel("callsign") ? parameters["callsign"].String() : "DisplayInfo.1";
                string method   = parameters.HasLabel("method")   ? parameters["method"].String()   : "tvcapabilities";

                SYSLOG(Logging::Startup, (_T("OtelPluginTest::testTrace callsign=%s method=%s"),
                    callsign.c_str(), method.c_str()));

#if RDK_OTEL_ENABLED
                string opName = callsign + "." + method;
                rdk_otlp_start_distributed_trace(opName.c_str(), "invoke");
#endif

                auto link = std::make_shared<WPEFramework::JSONRPC::SmartLinkType<Core::JSON::IElement>>(
                    callsign, _T("OtelPluginTest"), _T(""));

                JsonObject params;
                JsonObject result;
                uint32_t errorCode = Core::ERROR_UNAVAILABLE;

                if (link != nullptr) {
                    // traceparent is auto-injected by JSONRPCLink.h when a span is active
                    errorCode = link->Invoke<JsonObject, JsonObject>(5000, method, params, result);
                }

#if RDK_OTEL_ENABLED
                rdk_otlp_finish_distributed_trace();
#endif

                if (errorCode == Core::ERROR_NONE) {
                    string resultStr;
                    result.ToString(resultStr);
                    response["success"] = true;
                    response["result"]  = resultStr;
                } else {
                    response["success"]   = false;
                    response["errorCode"] = static_cast<uint32_t>(errorCode);
                }

                // Report traceid for diagnostics
#if RDK_OTEL_ENABLED
                {
                    char traceId[33] = {};
                    char spanId[17]  = {};
                    // Log to file for easy verification
                    FILE* fp = fopen("/opt/logs/otel_plugin_test.log", "a");
                    if (fp) {
                        fprintf(fp, "[OtelPluginTest] testTrace: callsign=%s method=%s errorCode=%u\n",
                            callsign.c_str(), method.c_str(), errorCode);
                        fclose(fp);
                    }
                }
#endif

                return errorCode == Core::ERROR_NONE ? Core::ERROR_NONE : errorCode;
            }

            // JSON-RPC: testMultiTrace
            // Params: {"callsign":"DisplayInfo.1","methods":["tvcapabilities","framerate","totalgpuram","isaudiopassthrough","hdrsetting"]}
            // Starts ONE distributed trace and invokes all methods under the same trace ID.
            uint32_t TestMultiTrace(const JsonObject& parameters, JsonObject& response)
            {
                string callsign = parameters.HasLabel("callsign") ? parameters["callsign"].String() : "DisplayInfo.1";

                // Default methods - passed as comma-separated string: "methods":"m1,m2,m3"
                std::vector<string> methods;
                if (parameters.HasLabel("methods")) {
                    string methodsStr = parameters["methods"].String();
                    size_t pos = 0, found;
                    while ((found = methodsStr.find(',', pos)) != string::npos) {
                        methods.push_back(methodsStr.substr(pos, found - pos));
                        pos = found + 1;
                    }
                    methods.push_back(methodsStr.substr(pos));
                }
                if (methods.empty()) {
                    methods = { "tvcapabilities", "framerate", "totalgpuram", "isaudiopassthrough", "hdrsetting" };
                }

                SYSLOG(Logging::Startup, (_T("OtelPluginTest::testMultiTrace callsign=%s methods=%zu"),
                    callsign.c_str(), methods.size()));

#if RDK_OTEL_ENABLED
                string opName = callsign + ".multi";
                rdk_otlp_start_distributed_trace(opName.c_str(), "multi-invoke");
#endif

                auto link = std::make_shared<WPEFramework::JSONRPC::SmartLinkType<Core::JSON::IElement>>(
                    callsign, _T("OtelPluginTest"), _T(""));

                Core::JSON::ArrayType<Core::JSON::String> results;
                uint32_t lastError = Core::ERROR_NONE;

                for (const auto& method : methods) {
                    JsonObject params;
                    JsonObject result;
                    uint32_t errorCode = Core::ERROR_UNAVAILABLE;

                    if (link != nullptr) {
                        // Each Invoke() call auto-injects the SAME traceparent
                        // because the same distributed trace span is active
                        errorCode = link->Invoke<JsonObject, JsonObject>(5000, method, params, result);
                    }

                    if (errorCode != Core::ERROR_NONE) {
                        lastError = errorCode;
                        SYSLOG(Logging::Startup, (_T("OtelPluginTest::testMultiTrace: %s failed: %u"),
                            method.c_str(), errorCode));
                    }

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

#if RDK_OTEL_ENABLED
                {
                    FILE* fp = fopen("/opt/logs/otel_plugin_test.log", "a");
                    if (fp) {
                        fprintf(fp, "[OtelPluginTest] testMultiTrace: callsign=%s methods=%zu lastError=%u\n",
                            callsign.c_str(), methods.size(), lastError);
                        fclose(fp);
                    }
                }
#endif

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
            1, 0, 0,
            {},
            {},
            {}
        );

    } // namespace Plugin
} // namespace WPEFramework
