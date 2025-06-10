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
#include <libIBus.h>
#include <UtilsSynchro.hpp>
#include <map>
#include <string>
#include "UtilsLogging.h"

using namespace WPEFramework;

namespace Utils {

    namespace Synchro {

        // owner -> map( eventId -> real handler)
        using HandlerMapType = std::map<std::string, std::map<IARM_EventId_t, IARM_EventHandler_t>>;

        // maps evnt types to handlers, one per specific class
        template<class UsingClass>
        struct IarmHandlers {
            static HandlerMapType _registered_iarm_handlers;
        };

        template<class UsingClass>
        HandlerMapType IarmHandlers<UsingClass>::_registered_iarm_handlers;

        // we need separate handler per class, so that when we call IARM_Bus_RemoveEventHandler, we will not
        // remove _generic_iarm_handler registered by other classes/in-process plugins
        template<class UsingClass>
        static void _generic_iarm_handler(const char *owner, IARM_EventId_t eventId, void *data, size_t len) {
            auto& handlers_map = IarmHandlers<UsingClass>::_registered_iarm_handlers;
            isThreadUsingLockedApi = true;
            std::lock_guard<std::recursive_mutex> lock(ApiLocks<UsingClass>::mtx);
            LOGINFO("calling handler %s/%d with lock: %p\n", owner, eventId, &ApiLocks<UsingClass>::mtx);
            try {
                handlers_map[owner][eventId](owner, eventId, data, len);
            } catch (...) {
                isThreadUsingLockedApi = false;
                throw;
            }
            isThreadUsingLockedApi = false;
        }

        template<class UsingClass>
        static IARM_Result_t RegisterLockedIarmEventHandler(const char *ownerName, IARM_EventId_t eventId, IARM_EventHandler_t handler) {
            auto generic_handler = _generic_iarm_handler<UsingClass>;
            auto& handlers_map = IarmHandlers<UsingClass>::_registered_iarm_handlers;

            std::lock_guard<std::recursive_mutex> lock(ApiLocks<UsingClass>::mtx);
            handlers_map[ownerName][eventId] = handler;
            return ::IARM_Bus_RegisterEventHandler(ownerName, eventId, generic_handler);
        }

        template<class UsingClass>
        static IARM_Result_t RemoveLockedEventHandler(const char *ownerName, IARM_EventId_t eventId, IARM_EventHandler_t handler) {
            auto& handlers_map = IarmHandlers<UsingClass>::_registered_iarm_handlers;

            std::lock_guard<std::recursive_mutex> lock(ApiLocks<UsingClass>::mtx);
            if (handler != handlers_map[ownerName][eventId]) {
                LOGERR("class %s RemoveLockedEventHandler for ownerName: %s, event: %d passed handler: %p different than registered: %p\n", typeid(UsingClass).name(), ownerName, eventId, handler, handlers_map[ownerName][eventId]); fflush(stdout);
            }
            // still erase the event in any case
            handlers_map[ownerName].erase(eventId);
            return ::IARM_Bus_RemoveEventHandler(ownerName, eventId, _generic_iarm_handler<UsingClass>);
        }
    } // Synchro
} // Utils
