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

#include <mutex>
#include <plugins/plugins.h>
#include <memory>
#include "UtilsLogging.h"

using namespace WPEFramework;

namespace Utils {
    namespace Synchro {

        namespace {
            // set when inside of getFunctionToCall wrapper (or locked IARM handler - see UtilsSynchroIarm.hpp)
            thread_local bool isThreadUsingLockedApi = false;
        }

        // keeps API locks, one per specific class
        template<class C>
        struct ApiLocks {
            static std::recursive_mutex mtx;
        };

        template <class C> std::recursive_mutex ApiLocks<C>::mtx;

        template <typename METHOD, typename REALOBJECT>
        std::function<uint32_t(REALOBJECT*, const WPEFramework::Core::JSON::VariantContainer&, WPEFramework::Core::JSON::VariantContainer&)>
        getFunctionToCall(const std::string& debugname, const METHOD& method, REALOBJECT* objectPtr) {
            return [debugname, method](REALOBJECT *obj, const WPEFramework::Core::JSON::VariantContainer& in, WPEFramework::Core::JSON::VariantContainer& out) -> uint32_t {
                isThreadUsingLockedApi = true;
                // printf("METHOD CALL, GETTING LOCK: REALOBJECT '%s', method: '%s' MUTEX:%p\n",typeid(REALOBJECT).name(), debugname.c_str(), &ApiLocks<REALOBJECT>::mtx); fflush(stdout);
                std::lock_guard<std::recursive_mutex> lock(ApiLocks<REALOBJECT>::mtx);
                LOGINFO("calling %s with lock: %p\n", debugname.c_str(), &ApiLocks<REALOBJECT>::mtx);
                uint32_t ret;
                try {
                    ret = (obj->*method)(in, out);
                } catch (...) {
                    isThreadUsingLockedApi = false;
                    throw;
                }
                isThreadUsingLockedApi = false;
                return ret;
            };
        }

        template <typename METHOD, typename REALOBJECT>
        void RegisterLockedApi(const string& methodName, const METHOD& method, REALOBJECT* objectPtr)
        {
            using MethodType = decltype(getFunctionToCall(methodName, method, objectPtr));
            objectPtr->PluginHost::JSONRPC::Register<Core::JSON::VariantContainer, Core::JSON::VariantContainer, MethodType, REALOBJECT>(methodName, getFunctionToCall(methodName, method, objectPtr), objectPtr);
        }

        template <typename METHOD, typename REALOBJECT>
        void RegisterLockedApiForVersions(const string& methodName, const METHOD& method, REALOBJECT* objectPtr, const std::vector<uint8_t> versions)
        {
            objectPtr->PluginHost::JSONRPC::Register<METHOD,REALOBJECT>(methodName, getFunctionToCall(methodName, method, objectPtr), objectPtr, versions);
        }

        template <typename METHOD, typename REALOBJECT>
        void RegisterLockedApiForHandler(Core::JSONRPC::Handler* handler, const string& methodName, const METHOD& method, REALOBJECT* objectPtr)
        {
            handler->Register<JsonObject, JsonObject>(methodName, getFunctionToCall(methodName, method, objectPtr), objectPtr);
        }

        /*
            This guard can unlock & re-lock api mutex to prevent deadlock possible when calling other plugins via Invoke
            (could deadlock in case when that other plugin called Invoke on this plugin at the same time, or tried to call
            this plugin recursively, from the Invoke'd call).
        */
        template<class UsingClass>
        struct UnlockApiGuard {
            UnlockApiGuard() {
                if (isThreadUsingLockedApi) {
                    ApiLocks<UsingClass>::mtx.unlock();
                }
            }
            ~UnlockApiGuard() {
                if (isThreadUsingLockedApi) {
                    ApiLocks<UsingClass>::mtx.lock();
                }
            }
        };

        template<class UsingClass>
        struct LockApiGuard {
            std::unique_lock<std::recursive_mutex> _lock;
            LockApiGuard() : _lock(ApiLocks<UsingClass>::mtx) {}
            void unlock() {
                _lock.unlock();
            }
            void lock() {
                _lock.lock();
            }
        };


    } // Utils
} // Synchro