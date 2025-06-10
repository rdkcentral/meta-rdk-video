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

#include <core/core.h>
#include <iostream>
#include <fstream>
#include <string>

using namespace std;

namespace Utils
{
auto MoveFile(
    const string &from,
    const string &to) -> bool
{
    using namespace WPEFramework::Core;

    File fileFrom(from);
    File fileTo(to);

    Directory(fileTo.PathName().c_str()).CreatePath();

    bool result =
        fileFrom.Exists() &&
            !fileTo.Exists() &&
            fileFrom.Open(true) &&
            fileTo.Create();

    if (result) {
        const uint32_t bufLen = 1024;

        uint8_t buffer[bufLen];

        do {
            auto len = fileFrom.Read(buffer, bufLen);
            if (len <= 0) {
                break;
            }

            auto ptr = buffer;

            do {
                auto count = fileTo.Write(ptr, len);
                if (count <= 0) {
                    result = false;
                    break;
                }

                len -= count;
                ptr += count;
            }
            while (len > 0);
        }
        while (result);

        if (result) {
            fileFrom.Destroy();
        }
        else {
            fileTo.Destroy();
        }
    }

    return result;
}

/**
* @brief Get the last non empty line from the input string, equivalent to "tr -s '\r' '\n' | tail -n 1"
* @param[in] input - The input string
* @param[out] res_str - The last non empty line from the input string
* @return whether or not a non empty line was found
*/
bool getLastLine(const std::string& input, std::string& res_str)
{
    string read_line = "";
    bool ret_value = false;

    if (!input.empty())
    {
        stringstream read_str(input);
        while (getline(read_str, read_line, '\n'))
        {
            if (!read_line.empty())
            {
                res_str = read_line;
                ret_value = true;
            }
        }
    }
    return ret_value;
}

}
