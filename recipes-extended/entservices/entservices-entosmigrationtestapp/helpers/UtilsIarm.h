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

#include "libIBus.h"
#include <unistd.h>

#define IARM_CHECK(FUNC) { \
    if ((res = FUNC) != IARM_RESULT_SUCCESS) { \
        LOGINFO("IARM %s: %s", #FUNC, \
            res == IARM_RESULT_INVALID_PARAM ? "invalid param" : ( \
            res == IARM_RESULT_INVALID_STATE ? "invalid state" : ( \
            res == IARM_RESULT_IPCCORE_FAIL ? "ipcore fail" : ( \
            res == IARM_RESULT_OOM ? "oom" : "unknown")))); \
    } \
    else \
    { \
        LOGINFO("IARM %s: success", #FUNC); \
    } \
}

namespace Utils {
struct IARM {
    static bool init()
    {
        IARM_Result_t res;
        bool result = false;

        if (isConnected()) {
            LOGINFO("IARM already connected");
            result = true;
        } else {
            unsigned int retryCount = 0;
            do
            {
                res = IARM_Bus_Init(NAME);
                LOGINFO("IARM_Bus_Init: %d", res);
                if (res == IARM_RESULT_SUCCESS || res == IARM_RESULT_INVALID_STATE /* already inited or connected */) {
                    res = IARM_Bus_Connect();
                    LOGINFO("IARM_Bus_Connect: %d", res);
                    if (res == IARM_RESULT_SUCCESS || res == IARM_RESULT_INVALID_STATE /* already connected or not inited */) {
                        result = isConnected();
                        LOGERR("ARM_Bus_Connect result: %d res: %d retryCount :%d ",result, res, retryCount);
                    } else {
                        LOGERR("IARM_Bus_Connect failure:result :%d res: %d retryCount :%d ",result, res, retryCount);
                    }
                } else {
                    LOGERR("IARM_Bus_Init failure: result :%d res: %d retryCount :%d",result, res,retryCount);
                }

                if(result == false) usleep(100000);

            }while((result == false) && (retryCount++ < 20));
        }

        return result;
    }

    static bool isConnected()
    {
        IARM_Result_t res;
        int isRegistered = 0;
        res = IARM_Bus_IsConnected(NAME, &isRegistered);
        LOGINFO("IARM_Bus_IsConnected: res:%d  isRegistered (%d)", res, isRegistered);

        return (isRegistered == 1);
    }

    static constexpr const char* NAME = "Thunder_Plugins";
};
}
