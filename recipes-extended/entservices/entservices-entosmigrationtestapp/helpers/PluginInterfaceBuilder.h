/**
 * If not stated otherwise in this file or this component's LICENSE
 * file the following copyright and licenses apply:
 *
 * Copyright 2024 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
#pragma once

#include <com/Communicator.h>
#include <core/Proxy.h>
#include <plugins/IShell.h>

#include "UtilsLogging.h"

namespace WPEFramework {
namespace PluginHost {
    class IShell;
}

namespace Plugin {

    template <typename INTERFACE>
    class PluginInterfaceRef {
        INTERFACE* _interface;
        PluginHost::IShell* _service;

    public:
        PluginInterfaceRef()
            : _interface(nullptr)
        {
        }

        PluginInterfaceRef(INTERFACE* interface, PluginHost::IShell* controller)
            : _interface(interface)
        {
        }

        ~PluginInterfaceRef()
        {
            Reset();
        }

        // avoid copies
        PluginInterfaceRef(const PluginInterfaceRef&) = delete;
        PluginInterfaceRef& operator=(const PluginInterfaceRef&) = delete;

        // use move
        PluginInterfaceRef(PluginInterfaceRef&& other)
            : _interface(other._interface)
        {
            other._interface = nullptr;
        }

        PluginInterfaceRef& operator=(PluginInterfaceRef&& other)
        {
            if (this != &other) {
                _interface = other._interface;
                other._interface = nullptr;
            }
            return *this;
        }

        operator bool() const
        {
            return _interface != nullptr;
        }

        INTERFACE* operator->() const
        {
            return _interface;
        }

        void Reset()
        {
            if (_interface) {
                _interface->Release();
                _interface = nullptr;
            }
        }
    };

    template <typename INTERFACE>
    class PluginInterfaceBuilder;

    // default impl
    template <typename INTERFACE>
    INTERFACE* createInterface(PluginInterfaceBuilder<INTERFACE>& builder)
    {
        WPEFramework::PluginHost::IShell* controller = builder.controller();
        const std::string& callsign = builder.callSign();
        const int retryCount = builder.retryCount();
        const uint32_t retryInterval = builder.retryInterval();
        int count = 0;

        if (!controller) {
            LOGERR("Invalid controller");
            return nullptr;
        }

        do {
            auto pluginInterface = controller->QueryInterfaceByCallsign<INTERFACE>(callsign.c_str());

            if (pluginInterface) {
                pluginInterface->AddRef();
                LOGINFO("plugin interface succeed and retry count: %d", count);
                return pluginInterface;
            } else {
                count++;
                LOGERR("plugin interface failed and retry: %d", count);
                usleep(retryInterval * 1000);
            }
        } while (count < retryCount);

        return nullptr;
    }

    template <typename T, typename... Args>
    std::unique_ptr<T> make_unique(Args&&... args)
    {
        return std::unique_ptr<T>(new T(std::forward<Args>(args)...));
    }

    template <typename INTERFACE>
    class PluginInterfaceBuilder {

        const std::string _callsign;
        PluginHost::IShell* _service;
        uint32_t _version;
        uint32_t _timeout;
        int _retryCount;
        uint32_t _retryInterval;

    public:
        PluginInterfaceBuilder(const char* callsign)
            : _callsign(callsign)
            , _service(nullptr)
            , _version(static_cast<uint32_t>(~0))
            , _timeout(3000)
            , _retryCount(0)
            , _retryInterval(0)
        {
        }

        // won't take ownership of ref members
        ~PluginInterfaceBuilder() = default;

        inline PluginInterfaceBuilder& withVersion(uint32_t version)
        {
            _version = version;
            return *this;
        }

        inline PluginInterfaceBuilder& withTimeout(uint32_t timeoutMs)
        {
            _timeout = timeoutMs;
            return *this;
        }

        inline PluginInterfaceBuilder& withIShell(PluginHost::IShell* service)
        {
            _service = service;
            return *this;
        }

        inline PluginInterfaceBuilder& withRetryIntervalMS(int retryInterval)
        {
            _retryInterval = retryInterval;
            return *this;
        }

        inline PluginInterfaceBuilder& withRetryCount(int retryCount)
        {
            _retryCount = retryCount;
            return *this;
        }

        PluginInterfaceRef<INTERFACE> createInterface()
        {
            auto* interface = ::WPEFramework::Plugin::createInterface<INTERFACE>(*this);

            if (!interface) {
                LOGERR("Failed to create plugin interface for %s", _callsign.c_str());
            }

            // pass on the ownership of controller to interfaceRef
            return std::move(PluginInterfaceRef<INTERFACE>(interface, _service));
        }

        const uint32_t retryInterval() const
        {
            return _retryInterval;
        }

        const int retryCount() const
        {
            return _retryCount;
        }

        const std::string& callSign() const
        {
            return _callsign;
        }

        WPEFramework::PluginHost::IShell* controller()
        {
            return _service;
        }
    };

} // Plugin
} // WPEFramework
