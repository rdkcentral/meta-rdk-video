/*
* If not stated otherwise in this file or this component's LICENSE file the
* following copyright and licenses apply:
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
*/

#include "EntOSMigrationTestApp.h"
#include "cJSON.h"
#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <string>
#include <unordered_map>
#include <algorithm>
//#include <UtilsSynchro.hpp>
#include "UtilsCStr.h"
#include "UtilsIarm.h"
#include "UtilsJsonRpc.h"
#include "UtilsString.h"
#include "UtilsisValidInt.h"
//#include "dsRpc.h"

#include "UtilsSynchroIarm.hpp"

#include <cstdio>
#include <cstdlib>
#include <cstring>

#define API_VERSION_NUMBER_MAJOR 1
#define API_VERSION_NUMBER_MINOR 0
#define API_VERSION_NUMBER_PATCH 1

#define MIGRATION_DATA_STORE_PATH "/opt/secure/migration/migration_data_store.json"
#define MIGARTION_SCHEMA_PATH "/opt/secure/migration/entos_settings_migration.schema.json"

// TODO: remove this
#define registerMethod(...) for (uint8_t i = 1; GetHandler(i); i++) GetHandler(i)->Register<JsonObject, JsonObject>(__VA_ARGS__)
//#define registerMethodLockedApi(...) for (uint8_t i = 1; GetHandler(i); i++) Utils::Synchro::RegisterLockedApiForHandler(GetHandler(i), __VA_ARGS__)

namespace WPEFramework
{

    namespace {

        static Plugin::Metadata<Plugin::EntOSMigrationTestApp> metadata(
            // Version (Major, Minor, Patch)
            API_VERSION_NUMBER_MAJOR, API_VERSION_NUMBER_MINOR, API_VERSION_NUMBER_PATCH,
            // Preconditions
            {},
            // Terminations
            {},
            // Controls
            {}
        );
    }

    namespace Plugin
    {

    /*
     *Register MigrationPreparer module as wpeframework plugin
     **/
    SERVICE_REGISTRATION(EntOSMigrationTestApp, API_VERSION_NUMBER_MAJOR, API_VERSION_NUMBER_MINOR, API_VERSION_NUMBER_PATCH);

    EntOSMigrationTestApp::EntOSMigrationTestApp() : _service(nullptr)
    {
        SYSLOG(Logging::Startup, (_T("EntOSMigrationTestApp Constructor")));
        LOGINFO("EntOSMigrationTestApp's Constructor called");
        printf("EntOSMigrationTestApp's Constructor called");

        PopulateMigrationDataStore();
        PopulateMigrationDataStoreSchema();
        
    }

    EntOSMigrationTestApp::~EntOSMigrationTestApp()
    {
        SYSLOG(Logging::Shutdown, (string(_T("EntOSMigrationTestApp Destructor"))));
        LOGINFO("EntOSMigrationTestApp's destructor called");
        printf("EntOSMigrationTestApp's destructor called");        

        
    }

    void EntOSMigrationTestApp :: extractFromSchema(cJSON* node, const string& parentKey) {
        if (!node || !cJSON_IsObject(node)) return;

        cJSON* properties = cJSON_GetObjectItem(node, "properties");
        if (properties && cJSON_IsObject(properties)) {
            cJSON* child = nullptr;
            cJSON_ArrayForEach(child, properties) {
                if (!child->string) continue;
                string key = child->string;
                string fullKey = parentKey.empty() ? key : parentKey + "/" + key;
                extractFromSchema(child, fullKey);
            }
            return;
        }

        cJSON* typeNode = cJSON_GetObjectItem(node, "type");
        if (typeNode && cJSON_IsString(typeNode)) {
            const char* typeStr = typeNode->valuestring;

            if (strcmp(typeStr, "array") == 0) {
                // For array type, check "items"
                cJSON* itemsNode = cJSON_GetObjectItem(node, "items");
                if (itemsNode) {
                    // If "items" is array: multiple item schemas, else one schema
                    if (cJSON_IsArray(itemsNode)) {
                        int size = cJSON_GetArraySize(itemsNode);
                        for (int i = 0; i < size; i++) {
                            cJSON* item = cJSON_GetArrayItem(itemsNode, i);
                            extractFromSchema(item, parentKey);
                        }
                    } else {
                        extractFromSchema(itemsNode, parentKey);
                    }
                }
                return;
            }

            if (strcmp(typeStr, "object") == 0) {
                // Object can have "properties"
                cJSON* props = cJSON_GetObjectItem(node, "properties");
                if (props && cJSON_IsObject(props)) {
                    cJSON* child = nullptr;
                    cJSON_ArrayForEach(child, props) {
                        if (!child->string) continue;
                        string key = child->string;
                        string fullKey = parentKey.empty() ? key : parentKey + "/" + key;
                        extractFromSchema(child, fullKey);
                    }
                }
                return;
            }

            if (strcmp(typeStr, "string") == 0) {
                // Extract enum array for this string key
                cJSON* enumNode = cJSON_GetObjectItem(node, "enum");
                if (enumNode && cJSON_IsArray(enumNode)) {
			std::vector<string> enums;
                    int size = cJSON_GetArraySize(enumNode);
                    for (int i = 0; i < size; i++) {
                        cJSON* item = cJSON_GetArrayItem(enumNode, i);
                        if (cJSON_IsString(item))
                            enums.push_back(item->valuestring);
                    }
                    if (!enums.empty())
                        enumMap[parentKey] = enums;
                }
                return;
            }

            if (strcmp(typeStr, "number") == 0 || strcmp(typeStr, "integer") == 0) {
                // Extract min, max if available
                cJSON* minNode = cJSON_GetObjectItem(node, "minimum");
                cJSON* maxNode = cJSON_GetObjectItem(node, "maximum");
                if (minNode && cJSON_IsNumber(minNode) && maxNode && cJSON_IsNumber(maxNode)) {
                    numberRangeMap[parentKey] = {minNode->valuedouble, maxNode->valuedouble};
                }
                return;
            }
        }
    }



    void EntOSMigrationTestApp :: ParseInputJson(cJSON* node, const string& parentKey) 
    {
        if (cJSON_IsObject(node)) {
            cJSON* child = nullptr;
            cJSON_ArrayForEach(child, node) {
                if (!child->string) continue;
                string newKey = parentKey.empty() ? child->string : parentKey + "/" + child->string;
                ParseInputJson(child, newKey);
            }
        } else {
            // store the value
            parserData[parentKey] = node;
        }
    }


    void EntOSMigrationTestApp :: PopulateMigrationDataStore()
    {
        printf("EntOSMigrationTestApp'S PopulateMigrationDataStore Method called");
        char *jsonDoc = NULL;
        FILE *file = fopen(MIGRATION_DATA_STORE_PATH, "r");

        if (!file)
            return ;

        fseek(file, 0, SEEK_END);
        long numbytes = ftell(file);
        jsonDoc = (char*)malloc(sizeof(char)*(numbytes + 1));
        if(jsonDoc == NULL) {
            fclose(file);
            return ;
        }

        fseek(file, 0, SEEK_SET);
        if(numbytes >= 0) {
            (void)fread(jsonDoc, numbytes, 1, file);
        }
        fclose(file);
        file =NULL;
        jsonDoc[numbytes] = '\0';

        cJSON *parserRoot = cJSON_Parse(jsonDoc);
        free(jsonDoc);
        jsonDoc = NULL;
        if (!parserRoot) {
            std::cout << "Failed to parse data.json\n";
            cJSON_Delete(parserRoot);
            return ;
        }
        ParseInputJson(parserRoot, "");  

        cJSON_Delete(parserRoot);
        

    }

    void EntOSMigrationTestApp :: PopulateMigrationDataStoreSchema()
    {
        printf("EntOSMigrationTestApp'S PopulateMigrationDataStoreSchema Method called");
        char *jsonDoc = NULL;
        std::string retVal = "";
        FILE *file = fopen(MIGARTION_SCHEMA_PATH, "r");

        if (!file)
            return ;

        fseek(file, 0, SEEK_END);
        long numbytes = ftell(file);
        jsonDoc = (char*)malloc(sizeof(char)*(numbytes + 1));
        if(jsonDoc == NULL) {
            fclose(file);
            return ;
        }

        fseek(file, 0, SEEK_SET);
        if(numbytes >= 0) {
            (void)fread(jsonDoc, numbytes, 1, file);
        }
        fclose(file);
        file = NULL;
        jsonDoc[numbytes] = '\0';

        cJSON *schema = cJSON_Parse(jsonDoc);
        free(jsonDoc);
        jsonDoc = NULL;


        if (!schema) {
        std::cout << "Failed to parse schema.json\n";
        cJSON_Delete(schema);
        return ;
        }

        extractFromSchema(schema, "");

        cJSON_Delete(schema);
    }

    const string EntOSMigrationTestApp::Initialize(PluginHost::IShell* service)
    {
        LOGINFO("EntOSMigrationTestApp's Initialize method called");
        printf("EntOSMigrationTestApp's Initialize method called");
        string message="";

        ASSERT(nullptr != service);
        ASSERT(nullptr == _service);

        SYSLOG(Logging::Startup, (_T("EntOSMigrationTestApp::Initialize: PID=%u"), getpid()));

        _service = service;
        _service->AddRef();
        
        RegisterAll();
        
        if (0 != message.length())
        {
           Deinitialize(service);
        }

        return message;
    }

    void EntOSMigrationTestApp::Deinitialize(PluginHost::IShell* service)
    {
        LOGINFO("EntOSMigrationTestApp'S Deinitialize Method called");
        printf("EntOSMigrationTestApp'S Deinitialize Method called");
        ASSERT(_service == service);

        SYSLOG(Logging::Shutdown, (string(_T("EntOSMigrationTestApp::Deinitialize"))));

       
        UnregisterAll();
        _connectionId = 0;
        _service->Release();
        _service = nullptr;
        SYSLOG(Logging::Shutdown, (string(_T("EntOSMigrationTestApp de-initialised"))));
    }

    string EntOSMigrationTestApp::Information() const
    {
       return (string("{\"service\": \"") + string("org.rdk.EntOSMigrationTestApp") + string("\"}"));
    }

    void EntOSMigrationTestApp::RegisterAll()
    {
        printf("EntOSMigrationTestApp'S RegisterAll Method called");
        registerMethod("ApplyDeviceSettings", &EntOSMigrationTestApp::ApplyDeviceSettings, this);
    }

    void EntOSMigrationTestApp::UnregisterAll()
    {
        printf("EntOSMigrationTestApp'S UnregisterAll Method called");
    }

    bool EntOSMigrationTestApp :: validateKey(const string& key,cJSON* value)
    {
        // Check enum constraints first
        auto enumIt = enumMap.find(key);
        if (enumIt != enumMap.end()) {
            if (!cJSON_IsString(value)) {
                std::cout << "Value for key " << key << " is not string but enum expected.\n";
                return false;
            }
            const string valStr = value->valuestring;
            const std::vector<string>& allowed = enumIt->second;
            for (const string& s : allowed) {
                if (valStr == s)
                    return true;
            }
            std::cout << "Value '" << valStr << "' not in enum for key " << key << std::endl;
            return false;
        }

        // Check number range constraints
        auto numIt = numberRangeMap.find(key);
        if (numIt != numberRangeMap.end()) {
            if (!cJSON_IsNumber(value)) {
                std::cout << "Value for key " << key << " is not number but number expected.\n";
                return false;
            }
            double v = value->valuedouble;
            double minVal = numIt->second.first;
            double maxVal = numIt->second.second;
            if (v < minVal || v > maxVal) {
                std::cout << "Number value " << v << " out of range [" << minVal << "," << maxVal << "] for key " << key << std::endl;
                return false;
            }
            return true;
        }

        std::cout << "Schema validation failed , missing key " << key << "in input json file" << std::endl;
        return false;
    }
    uint32_t EntOSMigrationTestApp :: ApplyDeviceSettings(const JsonObject& parameters, JsonObject& response)
    //void EntOSMigrationTestApp :: ApplyDeviceSettings()
    {
        printf("EntOSMigrationTestApp'S ApplyDeviceSettings Method called");
        string settings;
        for(const auto& pair : parserData)
        {
            settings = pair.first;
            auto position = parserData.find(settings);
            if (position == parserData.end()) {
                std::cout << "Key not found in parser.json: " << settings << std::endl;
                // return false;
            }
            cJSON* value = position->second;

            
            if( settings=="sound/dolbyvolume")
            {
                bool valid = validateKey(settings,value);
                if(valid)
                {
                    //apply settings
                }
            }
            else if(settings == "sound/enhancespeech")
            {
                bool valid = validateKey(settings,value);
                if(valid)
                {
                    //apply settings
                }
            }
            else if (settings == "sound/opticalformat")
            {
                bool valid = validateKey(settings,value);
                if(valid)
                {
                    //apply settings
                }
            }
            else if (settings == "sound/hdmi/earc/audioformat")
            {
                bool valid = validateKey(settings,value);
                if(valid)
                {
                    //apply settings
                }
            }
            else if (settings == "system/timezone")
            {
                bool valid = validateKey(settings,value);
                if(valid)
                {
                    //apply settings
                }
            }
            
        }
	return 0;
    }

} // namespace Plugin
} // namespace WPEFramework
