#!/bin/sh
##########################################################################
# If not stated otherwise in this file or this component's LICENSE
# file the following copyright and licenses apply:
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
##########################################################################
#Script to clear browser cache on bootup after CDL

CUR_IMAGE=$(grep "^imagename" /version.txt | cut -d ':' -f2)
PREV_IMAGE=$(cat /opt/.browser_cache_clear_version)
BROWSER_CACHE_DIR=$(grep diskcachedir /etc/WPEFramework/plugins/ResidentApp.json | cut -d "\"" -f4)
CACHE_CLEAR_VER_FILE=/opt/.browser_cache_clear_version

if [ -f $CACHE_CLEAR_VER_FILE ] && [ "x$CUR_IMAGE" == "x$PREV_IMAGE" ]; then
    echo "Browser cache is not removed, previous reboot is not due to CDL"
else
    echo "Removing browser cache on bootup, Cache Dir : $BROWSER_CACHE_DIR"
    rm -rf $BROWSER_CACHE_DIR
    echo $CUR_IMAGE > $CACHE_CLEAR_VER_FILE
fi
