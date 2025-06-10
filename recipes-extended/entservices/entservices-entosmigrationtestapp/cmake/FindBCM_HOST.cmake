# - Try to find bcm_host.
# Once done, this will define
#
#  BCM_HOST_FOUND - the bcm_host is available
#  BCM_HOST::BCM_HOST - The bcm_host library and all its dependecies
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

if(BCM_HOST_FIND_QUIETLY)
    set(_BCM_HOST_MODE QUIET)
elseif(BCM_HOST_FIND_REQUIRED)
    set(_BCM_HOST_MODE REQUIRED)
endif()

find_package(PkgConfig)
pkg_check_modules(PC_BCM_HOST ${_BCM_HOST_MODE} bcm_host)

if(${PC_BCM_HOST_FOUND})
    find_library(BCM_HOST_LIBRARY bcm_host
        HINTS ${PC_BCM_LIBDIR} ${PC_BCM_LIBRARY_DIRS}
    )
    set(BCM_LIBRARIES ${PC_BCM_HOST_LIBRARIES})

    include(FindPackageHandleStandardArgs)
    find_package_handle_standard_args(BCM_HOST DEFAULT_MSG PC_BCM_HOST_FOUND PC_BCM_HOST_INCLUDE_DIRS BCM_HOST_LIBRARY PC_BCM_HOST_LIBRARIES)
    mark_as_advanced(PC_BCM_HOST_INCLUDE_DIRS PC_BCM_HOST_LIBRARIES)

    if(BCM_HOST_FOUND AND NOT TARGET BCM_HOST::BCM_HOST)
        add_library(BCM_HOST::BCM_HOST UNKNOWN IMPORTED)

        set_target_properties(BCM_HOST::BCM_HOST
            PROPERTIES
                IMPORTED_LINK_INTERFACE_LANGUAGES "C"
                IMPORTED_LOCATION "${BCM_HOST_LIBRARY}"
                INTERFACE_COMPILE_DEFINITIONS "PLATFORM_RPI"
                INTERFACE_COMPILE_OPTIONS "${PC_BCM_HOST_CFLAGS_OTHER}"
                INTERFACE_INCLUDE_DIRECTORIES "${PC_BCM_HOST_INCLUDE_DIRS}"
                INTERFACE_LINK_LIBRARIES "${PC_BCM_HOST_LIBRARIES}"
                )
    endif()
endif()
