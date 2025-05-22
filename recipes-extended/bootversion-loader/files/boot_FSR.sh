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
RA_Web_Store="/opt/persistent/rdkservices/ResidentApp/wpe/local-storage/http_platco.thor.local_50050.localstorage"
file_bootType="/tmp/bootType"
file_MigrationStatus="/opt/secure/persistent/MigrationStatus"
file_DataStore="/opt/secure/migration/migration_data_store.json"
file_Migration_Ready_check="/opt/secure/migration/iui_not_fully_ready_for_migration"

ftue_key_available="null"
current_bootType=$(<"$file_bootType")
current_bootType=${current_bootType:10}

do_ftue_check () {
Output=$(sqlite3 $RA_Web_Store "select * from ItemTable where key = 'ftue';" 2>&1)
ftue_key_available=${Output:0:4}
}

do_FSR () {
    touch /tmp/data/.trigger_reformat
    sh /rebootNow.sh -s boot_FSR -o "Rebooting the box for triggering FSR..."
}

echo -e "This is a Special Build for Handling FSR on Non-Activated Devices T2" 

if [ -e "$file_Migration_Ready_check" ]; then
	echo -e "The folder is present so consider this for implementation"
        #do_FSR
else
     echo -e "something is wrong with this build JP It is not working...."
fi
