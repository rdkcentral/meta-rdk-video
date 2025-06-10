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

find_package(PkgConfig)

find_path(PLABELS_INCLUDE_DIRS NAMES pbnj_utils.hpp PATH_SUFFIXES pbnj_utils)

find_library(PLABELS_LIBRARIES NAMES plabels)

set(PLABELS_FLAGS -DUSE_PLABELS=1 -DRDKLOG_ERROR= -DRDKLOG_INFO= CACHE PATH "Flags for pbnj_utils")

mark_as_advanced(
        PLABELS_FLAGS
        PLABELS_INCLUDE_DIRS
        PLABELS_LIBRARIES)
