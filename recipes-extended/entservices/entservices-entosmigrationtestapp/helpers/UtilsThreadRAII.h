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

#include "UtilsLogging.h"

namespace Utils {

class ThreadRAII {
public:
    ThreadRAII() {}
    ThreadRAII(std::thread&& t)
        : t(std::move(t))
    {
    }
    ~ThreadRAII()
    {
        try {
            if (t.joinable()) {
                t.join();
            }
        } catch (const std::system_error& e) {
            LOGERR("system_error exception in thread join %s", e.what());
        } catch (const std::exception& e) {
            LOGERR("exception in thread join %s", e.what());
        }
    }

    //support moving
    ThreadRAII(ThreadRAII&&) = default;
    ThreadRAII& operator=(ThreadRAII&&) = default;

    std::thread& get() { return t; }

private:
    std::thread t;
};
}
