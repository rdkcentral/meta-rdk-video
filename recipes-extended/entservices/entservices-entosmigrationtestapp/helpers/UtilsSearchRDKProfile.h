/**
* If not stated otherwise in this file or this component's LICENSE
* file the following copyright and licenses apply:
*
* Copyright 2019 RDK Management
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

#include <fstream>
#include <iostream>
#include <core/core.h>
#include <string>
#include <plugins/plugins.h>
using namespace std;

#define RDK_PROFILE "RDK_PROFILE="
#define PROFILE_TV "TV"
#define PROFILE_STB "STB"

typedef enum profile {
    NOT_FOUND = -1,
    STB = 0,
    TV,
    MAX
} profile_t;

profile_t profileType = NOT_FOUND;

profile_t searchRdkProfile(void) {

    const char* devPropPath = "/etc/device.properties";
    char line[256], *rdkProfile = NULL;
    profile_t ret = NOT_FOUND;
    FILE* file;

    file = fopen(devPropPath, "r");
    if (file == NULL) {
        printf("File not found issue \n");
        return NOT_FOUND;
    }

    while (fgets(line, sizeof(line), file)) {
        rdkProfile = strstr(line, RDK_PROFILE);
        if (rdkProfile != NULL) {
            rdkProfile += strlen(RDK_PROFILE); // Move past the 'RDK_PROFILE='
            printf("Found RDK_PROFILE: %s \n", rdkProfile);
            break;
        }
    }

    if (rdkProfile != NULL) {
        if (strncmp(rdkProfile, PROFILE_TV, strlen(PROFILE_TV)) == 0) {
            ret = TV;
        } else if (strncmp(rdkProfile, PROFILE_STB, strlen(PROFILE_STB)) == 0) {
            ret = STB;
        }
    } else {
        printf("Found RDK_PROFILE: NOT_FOUND \n");
        ret = NOT_FOUND;
    }
    fclose(file);
    return ret;
}
