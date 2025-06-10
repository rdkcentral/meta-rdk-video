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

#include <iostream>
#include <cstring>
#include <string>
#include <cstdlib>
#include <proc/readproc.h>
#include <vector>
#include <UtilsLogging.h>

using namespace std;

namespace Utils
{
/**
* @brief Kill all the processes with the given process name
* @param[in] input_pname - The given process name
* @return true if any process with the given name was killed, otherwise false is returned
*/
bool killProcess(string& input_pname)
{
    PROCTAB* proc = openproc(PROC_FILLMEM | PROC_FILLSTAT | PROC_FILLSTATUS);
    proc_t proc_info = {0};
    bool ret_value = false;

    if (proc != NULL)
    {
        memset(&proc_info, 0, sizeof(proc_info));
        while (readproc(proc, &proc_info) != NULL)
        {
            if (proc_info.cmd == input_pname)
            {
                if (0 == kill(proc_info.tid, SIGTERM))
                {
                    ret_value = true;
                    LOGINFO("Killed the process [%d] process name [%s]", proc_info.tid, proc_info.cmd);
                }
            }
        }
        closeproc(proc);
    }
    return ret_value;
}

/**
* @brief Get list of child processes with the given parent process ID, equivalent to "pgrep  -P <PPID>"
* @param[in] input_ppid - The given parent process ID
* @param[out] processIds - The list of child process IDs
* @return true if there are any child processes of the given parent process ID, otherwise false is returned
*/
bool getChildProcessIDs(int input_ppid, vector<int>& processIds)
{
    PROCTAB* proc = openproc(PROC_FILLMEM | PROC_FILLSTAT | PROC_FILLSTATUS);
    proc_t proc_info = {0};
    bool ret_value = false;

    if (proc != NULL)
    {
        memset(&proc_info, 0, sizeof(proc_info));
        while (readproc(proc, &proc_info) != NULL)
        {
            if (proc_info.ppid == input_ppid)
            {
                processIds.push_back(proc_info.tid);
                ret_value = true;
            }
        }
        closeproc(proc);
    }
    return ret_value;
}

}
