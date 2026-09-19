#pragma once

#include "Module.h"
#include <interfaces/IMemory.h>
#include <interfaces/IResourceMonitor.h>
#include <interfaces/json/JResourceMonitor.h>

namespace WPEFramework {
namespace Plugin {
    class ResourceMonitor : public PluginHost::IPlugin, public PluginHost::JSONRPC {
    public:
        ResourceMonitor(const ResourceMonitor&) = delete;
        ResourceMonitor& operator=(const ResourceMonitor&) = delete;
        ResourceMonitor()
            : _service(nullptr)
            , _monitor(nullptr)
            , _connectionId(0)
            , _notification(*this)
        {
        }

        ~ResourceMonitor() override = default;

        const string Initialize(PluginHost::IShell* service) override;
        void Deinitialize(PluginHost::IShell* service) override;
        string Information() const override;

        BEGIN_INTERFACE_MAP(ResourceMonitor)
        INTERFACE_ENTRY(PluginHost::IPlugin)
        INTERFACE_ENTRY(PluginHost::IDispatcher)
        INTERFACE_AGGREGATE(Exchange::IResourceMonitor, _monitor)
        END_INTERFACE_MAP

    private:
        PluginHost::IShell* _service;
        Exchange::IResourceMonitor* _monitor;
        uint32_t _connectionId;

        class Notification : public Exchange::IResourceMonitor::INotification {
        public:
            explicit Notification(ResourceMonitor& parent)
                : _parent(parent)
            {
            }

            Notification() = delete;
            Notification(const Notification&) = delete;
            Notification& operator=(const Notification&) = delete;

            ~Notification() override = default;

            void OnResourceMonitorData(const Exchange::IResourceMonitor::EventData& data) override
            {
                Exchange::JResourceMonitor::Event::OnResourceMonitorData(_parent, data);
            }

            BEGIN_INTERFACE_MAP(Notification)
            INTERFACE_ENTRY(Exchange::IResourceMonitor::INotification)
            END_INTERFACE_MAP

        private:
            ResourceMonitor& _parent;
        };

        Core::Sink<Notification> _notification;
    };
} // namespace Plugin
} // namespace WPEFramework
