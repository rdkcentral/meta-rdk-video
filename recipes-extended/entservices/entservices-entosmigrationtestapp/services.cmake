# If not stated otherwise in this file or this component's Licenses.txt file the
# following copyright and licenses apply:
#
# Copyright 2016 RDK Management
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# features
#

add_definitions (-DUSE_SOUND_PLAYER)

add_definitions (-DUSE_IARM)
option(USE_IARM "USE_IARM" ON)

add_definitions (-DUSE_IARM_BUS)
option(USE_IARM_BUS "USE_IARM_BUS" ON)

add_definitions (-DUSE_IARMBUS)

add_definitions (-DUSE_TR_69)

add_definitions (-DHAS_API_SYSTEM)
add_definitions (-DHAS_API_POWERSTATE)
add_definitions(-DRDK_LOG_MILESTONE)

add_definitions (-DUSE_DS)

option(PLUGIN_WAREHOUSE "PLUGIN_WAREHOUSE" ON)
option(HAS_API_HDMI_INPUT "HAS_API_HDMI_INPUT" ON)
option(PLUGIN_COPILOT "PLUGIN_COPILOT" OFF)
option(PLUGIN_FRAMERATE "PLUGIN_FRAMERATE" ON)
option(PLUGIN_STORAGE_MANAGER "PLUGIN_STORAGE_MANAGER" OFF)
option(PLUGIN_DEVICEDIAGNOSTICS "PLUGIN_DEVICEDIAGNOSTICS" ON)
option(PLUGIN_SOUNDPLAYER "PLUGIN_SOUNDPLAYER" OFF)
option(PLUGIN_TELEMETRY "PLUGIN_TELEMETRY" ON)
option(PLUGIN_LEDCONTROL "PLUGIN_LEDCONTROL" ON)
option(PLUGIN_CONTINUEWATCHING "PLUGIN_CONTINUEWATCHING" ON)


#add_definitions (-DCLIENT_VERSION_STRING)=\\\"$(VERSION_FULL_VALUE)\\\"
#add_definitions (-DSTB_VERSION_STRING)=\\\"$(FULL_VERSION_NAME_VALUE)\\\"
#add_definitions (-DSTB_TIMESTAMP_STRING)=\\\"$(STB_TIMESTAMP_VALUE)\\\"

#add_definitions (-DHAS_API_TTSSETTINGSSERVICE)
#add_definitions (-DHAS_API_TTSSESSIONSERVICE)
#add_definitions (-DHAS_API_TTSRESOURCESERVICE)
add_definitions (-DPLUGIN_CONTINUEWATCHING)
option(PLUGIN_CONTINUEWATCHING "PLUGIN_CONTINUEWATCHING" ON)

if(PLUGIN_CONTINUEWATCHING)
    if(CONTINUEWATCHING_DISABLE_SECAPI)
        add_definitions (-DDISABLE_SECAPI)
    endif()
endif()


if(PLUGIN_CONTINUEWATCHING)
    if(CONTINUEWATCHING_DISABLE_SECAPI)
        add_definitions (-DDISABLE_SECAPI)
    endif()
endif()

if (DISABLE_GEOGRAPHY_TIMEZONE)
    add_definitions (-DDISABLE_GEOGRAPHY_TIMEZONE)
endif()

if (BUILD_ENABLE_SYSTIMEMGR_SUPPORT)
    message("Building with SYSTIMEMGR_SUPPORT enabled")
   add_definitions (-DENABLE_SYSTIMEMGR_SUPPORT)
endif()

if (BUILD_DBUS)
    message("Building for DBUS")

    add_definitions (-DBUILD_DBUS)
    option(BUILD_DBUS "BUILD_DBUS" ON)
    add_definitions (-DIARM_USE_DBUS)
    option(IARM_USE_DBUS "IARM_USE_DBUS" ON)
endif()

if (BUILD_ENABLE_THERMAL_PROTECTION)
    add_definitions (-DBUILD_ENABLE_THERMAL_PROTECTION)
    add_definitions (-DENABLE_THERMAL_PROTECTION)
endif()

if (BUILD_ENABLE_DEVICE_MANUFACTURER_INFO)
    message("Building with device manufacturer info")
    add_definitions (-DENABLE_DEVICE_MANUFACTURER_INFO)
endif()

if (SUPPRESS_MAINTENANCE)
    message("Enable SUPPRESS_MAINTENANCE")
    add_definitions (-DSUPPRESS_MAINTENANCE)
endif()

if (BUILD_ENABLE_CLOCK)
    message("Building with clock support")
    add_definitions (-DCLOCK_BRIGHTNESS_ENABLED)
endif()

if (BUILD_ENABLE_EXTENDED_ALL_SEGMENTS_TEXT_PATTERN)
    add_definitions (-DUSE_EXTENDED_ALL_SEGMENTS_TEXT_PATTERN)
endif()

if(ENABLE_SYSTEM_GET_STORE_DEMO_LINK)
    message("Building with System Service getStoreDemoLink")
    add_definitions (-DENABLE_SYSTEM_GET_STORE_DEMO_LINK)
endif()

if (BUILD_ENABLE_TELEMETRY_LOGGING)
    message("Building with telemetry logging")
    add_definitions (-DENABLE_TELEMETRY_LOGGING)
endif()

if (BUILD_ENABLE_LINK_LOCALTIME)
    message("Building with link localtime")
    add_definitions (-DENABLE_LINK_LOCALTIME)
endif()

add_definitions (-DENABLE_DEEP_SLEEP)

# only on LLama
if(BUILD_ENABLE_APP_CONTROL_AUDIOPORT_INIT)
   add_definitions (-DAPP_CONTROL_AUDIOPORT_INIT)
endif()

if(NET_DISABLE_NETSRVMGR_CHECK)
    add_definitions (-DNET_DISABLE_NETSRVMGR_CHECK)
endif()

if (ENABLE_WHOAMI)
    message("Enable WHOAMI")
    add_definitions (-DENABLE_WHOAMI=ON)
endif()

if (ENABLE_RFC_MANAGER)
    message("Using binary for RFC Maintenance task")
    add_definitions (-DENABLE_RFC_MANAGER=ON)
endif()

if (DISABLE_DCM_TASK)
    message("Disabling DCM Maintenance task")
    add_definitions (-DDISABLE_DCM_TASK=ON)
endif()

if(BUILD_ENABLE_ERM)
        add_definitions(-DENABLE_ERM)
endif()
