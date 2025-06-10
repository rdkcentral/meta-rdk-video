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

#pragma once
#include "cJSON.h"
#include "Module.h"
#include "UtilsLogging.h"
#include "tracing/Logging.h"
#include "UtilsJsonRpc.h"
#include <mutex>



namespace WPEFramework {
namespace Plugin {

    class EntOSMigrationTestApp: public PluginHost::IPlugin, public PluginHost::JSONRPC
    {         
        public:
            // We do not allow this plugin to be copied !!
            EntOSMigrationTestApp(const EntOSMigrationTestApp&) = delete;
            EntOSMigrationTestApp& operator=(const EntOSMigrationTestApp&) = delete;

            EntOSMigrationTestApp();
            virtual ~EntOSMigrationTestApp();

            BEGIN_INTERFACE_MAP(EntOSMigrationTestApp)
            INTERFACE_ENTRY(PluginHost::IPlugin)
            INTERFACE_ENTRY(PluginHost::IDispatcher)
            END_INTERFACE_MAP

            //  IPlugin methods
            // -------------------------------------------------------------------------------------------------------
            const string Initialize(PluginHost::IShell* service) override;
            void Deinitialize(PluginHost::IShell* service) override;
            string Information() const override;
            void RegisterAll();
            void UnregisterAll();
            void PopulateMigrationDataStore();
            void PopulateMigrationDataStoreSchema();
            void extractFromSchema(cJSON* node, const string& parentKey);
            void ParseInputJson(cJSON* node, const string& parentKey);
            bool validateKey(const string& key,cJSON* value);
           

       
            uint32_t ApplyDeviceSettings(const JsonObject& parameters, JsonObject& response);
	   // void ApplyDeviceSettings();
            
        private:
            PluginHost::IShell* _service{};
            uint32_t _connectionId{};
            std :: unordered_map<string,std::vector<string>> enumMap; 
            std :: unordered_map<string, std::pair<double, double>> numberRangeMap; 

            std :: unordered_map<string, cJSON*> parserData;
    };

} // namespace Plugin
} // namespace WPEFramework
