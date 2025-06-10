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

#include <stdio.h>

namespace Utils {
inline void syncPersistFile(const string file)
{
    FILE* fp = NULL;
    fp = fopen(file.c_str(), "r");
    if (fp == NULL) {
        printf("fopen NULL\n");
        return;
    }
    fflush(fp);
    fsync(fileno(fp));
    fclose(fp);
}

inline void persistJsonSettings(const string strFile, const string strKey, const JsonValue& jsValue)
{
    WPEFramework::Core::File file;
    file = strFile.c_str();

    file.Open(false);
    if (!file.IsOpen())
        file.Create();

    JsonObject cecSetting;
    cecSetting.IElement::FromFile(file);
    file.Destroy();
    file.Create();
    cecSetting[strKey.c_str()] = jsValue;
    cecSetting.IElement::ToFile(file);

    file.Close();

    //Sync the settings
    Utils::syncPersistFile(strFile);

    return;
}
}
