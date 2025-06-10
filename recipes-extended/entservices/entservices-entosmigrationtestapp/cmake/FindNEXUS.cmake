# - Try to find Nexus.
# Once done this will define
#  NEXUS_FOUND  - System has Nexus
#  NEXUS::NEXUS - The Nexus library
#
###
# If not stated otherwise in this file or this component's LICENSE
# file the following copyright and licenses apply:
#
# Copyright 2024 RDK Management
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
###

find_path(LIBNEXUS_INCLUDE nexus_config.h
        PATH_SUFFIXES refsw)

find_library(LIBNEXUS_LIBRARY nexus)

if(EXISTS "${LIBNEXUS_LIBRARY}")
    find_library(LIBB_OS_LIBRARY b_os)
    find_library(LIBNEXUS_CLIENT_LIBRARY nexus_client)
    find_library(LIBNXCLIENT_LIBRARY nxclient)

    include(FindPackageHandleStandardArgs)
    find_package_handle_standard_args(NEXUS DEFAULT_MSG LIBNEXUS_INCLUDE LIBNEXUS_LIBRARY)
    mark_as_advanced(LIBNEXUS_INCLUDE LIBNEXUS_LIBRARY)

    if(NEXUS_FOUND AND NOT TARGET NEXUS::NEXUS)
        add_library(NEXUS::NEXUS UNKNOWN IMPORTED)
        set_target_properties(NEXUS::NEXUS PROPERTIES
                IMPORTED_LINK_INTERFACE_LANGUAGES "C"
                INTERFACE_INCLUDE_DIRECTORIES "${LIBNEXUS_INCLUDE}"
                    )

        if(NOT EXISTS "${LIBNEXUS_CLIENT_LIBRARY}")
            message(STATUS "Nexus in Proxy mode")
            set_target_properties(NEXUS::NEXUS PROPERTIES
                    IMPORTED_LOCATION "${LIBNEXUS_LIBRARY}"
                    )
        else()
            message(STATUS "Nexus in Client mode")
            set_target_properties(NEXUS::NEXUS PROPERTIES
                    IMPORTED_LOCATION "${LIBNEXUS_CLIENT_LIBRARY}"
                    )
        endif()

        if(NOT EXISTS "${LIBNXCLIENT_LIBRARY}")
            set_target_properties(NEXUS::NEXUS PROPERTIES
                    INTERFACE_COMPILE_DEFINITIONS NO_NXCLIENT
                    )
        endif()

        if(EXISTS "${LIBB_OS_LIBRARY}")
            set_target_properties(NEXUS::NEXUS PROPERTIES
                    IMPORTED_LINK_INTERFACE_LIBRARIES "${LIBB_OS_LIBRARY}"
                    )
        endif()
    endif()
    set_target_properties(NEXUS::NEXUS PROPERTIES
            INTERFACE_COMPILE_DEFINITIONS "PLATFORM_BRCM"
            )
else()
    if(NEXUS_FIND_REQUIRED)
        message(FATAL_ERROR "LIBNEXUS_LIBRARY not available")
    elseif(NOT NEXUS_FIND_QUIETLY)
        message(STATUS "LIBNEXUS_LIBRARY not available")
    endif()
endif()
