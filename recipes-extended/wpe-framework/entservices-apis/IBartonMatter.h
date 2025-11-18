/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2025 RDK Management
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

#include "Module.h"
#include <vector>
#include <string>

namespace WPEFramework {
namespace Exchange {

// @json 1.0.0 @text:keep

struct EXTERNAL IBartonMatter : virtual public Core::IUnknown {
    enum { ID = ID_BARTONMATTER };

    /* Allow client/UI to read the current status of the device*/
    //@text ReadResource
    //@brief read the current state of the matter device
    //@param uri: device unique ID
    //@param resourceType: the resource field to read to the data for e.g. isON,Label,manufracture
    //@param result: true/false mapped to device on/off state
    virtual Core::hresult ReadResource(std::string uri /* @in*/, std::string resourceType /* @in*/, std::string &result /* @out*/)=0;

    /* Allow client/UI to change the state of the device*/
    //@text WriteResource
    //@brief change the state of the device by writing the state
    //@param uri: unique device ID 
    //@param resourceType the resource field to write to the data for e.g. isON,Label
    //@param value state to write into the device
    virtual Core::hresult WriteResource(std::string uri /* @in*/, std::string resourceType /* @in*/, std::string value /* @in*/)=0;

    /** Allow the plugin to initialize to use service object */
    // @text InitializeCommissioner
    // @brief Initialize the commissioner
    virtual Core::hresult InitializeCommissioner() = 0;

    /** starts the commissioning process for given passcode*/
    //@text CommissionDevice
    //@brief commissions the device with the provided passcode
    //@param passcode code-of-possession
    virtual Core::hresult CommissionDevice(const std::string passcode /* @in*/) = 0;


    /** Creates the BartonMatter plugin */
    // @text setWifiCred
    // @brief sets the wifi credentials
    // @param ssid wifi ssid.
    // @param password wifi password
    virtual Core::hresult SetWifiCredentials(const std::string ssid /* @in */, const std::string password /* @in */) = 0;

    /** List the all connected devices*/
    // @text ListDevices
    // @brief returns the list of all connected devices
    //@param deviceList: list off all connected devices
     virtual Core::hresult ListDevices(std::string& deviceList /* @out */) =0;
};

} // Exchange
} // WPEFramework
