#include "ResourceMonitor.h"

namespace WPEFramework {

namespace Plugin {
    namespace {

        static Metadata<ResourceMonitor> metadata(
            // Version
            1, 0, 0,
            // Preconditions
            {},
            // Terminations
            {},
            // Controls
            {});
    }

    const string ResourceMonitor::Initialize(PluginHost::IShell* service)
    {
        string message;

        ASSERT(service != nullptr);
        ASSERT(_service == nullptr);
        ASSERT(_monitor == nullptr);
        ASSERT(_connectionId == 0);

        _service = service;
        _service->AddRef();
        _monitor = _service->Root<Exchange::IFCResourceMonitor>(_connectionId, 2000, _T("ResourceMonitorImplementation"));

        if (_monitor == nullptr) {
            message = _T("ResourceMonitor could not be instantiated.");
        } else {
            if (_monitor->Configure(service) == Core::ERROR_INCOMPLETE_CONFIG) {
                message = _T("ResourceMonitor could not be Configured.");
            }
            _monitor->Register(&_notification);
            Exchange::JFCResourceMonitor::Register(*this, _monitor);
        }

        return message;
    }

    void ResourceMonitor::Deinitialize(PluginHost::IShell* service VARIABLE_IS_NOT_USED)
    {
        if (_service != nullptr) {
            ASSERT(_service == _service);

            if (_monitor != nullptr) {
                Exchange::JFCResourceMonitor::Unregister(*this);
                _monitor->Unregister(&_notification);
                RPC::IRemoteConnection* connection(_service->RemoteConnection(_connectionId));
                VARIABLE_IS_NOT_USED uint32_t result = _monitor->Release();
                _monitor = nullptr;
                ASSERT(result == Core::ERROR_DESTRUCTION_SUCCEEDED);

                // The process can disappear in the meantime...
                if (connection != nullptr) {
                    // Connection is still there.
                    connection->Terminate();
                    connection->Release();
                }
            }

            _connectionId = 0;
            _service->Release();
            _service = nullptr;
        }
    }

    string ResourceMonitor::Information() const
    {
        return "";
    }
} // namespace Plugin
} // namespace Thunder
