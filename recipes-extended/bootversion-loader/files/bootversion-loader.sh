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

file_version="/version.txt"
file_bootversion="/opt/.bootversion"
file_bootType="/tmp/bootType"
file_MigrationStatus="/opt/secure/persistent/MigrationStatus"
file_updateStatus="/opt/.updateStatus"
file_bootversion_bak="/opt/.bootversion.bak"

if [ -z $LOG_PATH ]; then
    LOG_PATH="/opt/logs/"
fi

BOOTTYPE_LOG_FILE="$LOG_PATH/boottypescript.log"

boottypeLog() {
    echo "`/bin/timestamp`: $0: $*" >> $BOOTTYPE_LOG_FILE
}

#bootversion backup 
if [ -e "$file_updateStatus" ]; then
     status=$(<"$file_updateStatus")
     if [ "$status" == "INPROGRESS" ]; then
         boottypeLog "Update was in progress, $file_bootversion is incomplete. Looking for backup file"
           if [ -e "$file_bootversion_bak" ]; then
               boottypeLog "Found backup file, restoring $file_bootversion from $file_bootversion_bak"
               cp -f $file_bootversion_bak $file_bootversion
           else
               boottypeLog "No backup file found, cannot restore $file_bootversion and removing incomplete $file_bootversion"
               rm -rf $file_bootversion
           fi
     elif [ "$status" == "COMPLETED" ]; then
         boottypeLog "Update previously completed, $file_bootversion file is backedup as $file_bootversion_bak"
         cp -f $file_bootversion $file_bootversion_bak
         echo "INPROGRESS" > $file_updateStatus
         boottypeLog "Update in progress..."
     fi
else
     boottypeLog "$file_updateStatus file is not present, No update was in progress, creating $file_updateStatus"
     if [ -e "$file_bootversion" ]; then
          boottypeLog "Found $file_bootversion file, creating $file_bootversion_bak from $file_bootversion"
          cp -f $file_bootversion $file_bootversion_bak
     fi
     echo "INPROGRESS" > $file_updateStatus
     boottypeLog "Update in progress..."
fi

# /version.txt image details
v_imagename=$(grep "^imagename" $file_version)
v_version=$(grep "^VERSION" $file_version |  tr "=" ":")
v_FW_Class=$(grep "^FW_CLASS" $file_version | tr "=" ":")

# if /opt/.bootversion does not exist initially on migration from rdkv to rdke
if [ ! -e "$file_bootversion" ]; then
     # s1 = v
     echo "$v_imagename" > $file_bootversion
     echo "$v_version" >> $file_bootversion
     echo "$v_FW_Class" >> $file_bootversion
     echo "BOOT_TYPE=BOOT_INIT" > $file_bootType
     boottypeLog "BOOT_INIT is set since $file_bootversion is not present"
	 echo "COMPLETED" > $file_updateStatus
     boottypeLog "Update completed."
     exit 0
fi

# S1 image details from /opt/.bootversion
s1_imagename=$(grep -m 1 "imagename" $file_bootversion)
s1_version=$(grep -m 1 "VERSION" $file_bootversion)
s1_FW_Class=$(grep -m 1 "FW_CLASS" $file_bootversion)

#copy slot information
     # s1 = v
     echo "$v_imagename" > $file_bootversion
     echo "$v_version" >> $file_bootversion
     echo "$v_FW_Class" >> $file_bootversion
     # s2 = s1
     echo "$s1_imagename" >> $file_bootversion
     echo "$s1_version" >> $file_bootversion
     echo "$s1_FW_Class" >> $file_bootversion


MigrationStatus=$(tr181 -g Device.DeviceInfo.Migration.MigrationStatus 2>&1)

boottypeLog "MigrationStatus: $MigrationStatus"
#comparing slot1 and slot2 FW Class
if [ "$v_FW_Class" != "$s1_FW_Class" ]; then
	# migration fw is run for first time, migration not completed
	echo "NOT_STARTED" > $file_MigrationStatus
	echo "BOOT_TYPE=BOOT_MIGRATION" > $file_bootType
	boottypeLog "BOOT_MIGRATION is set since FW_Class is not same"
else
     if [ "$MigrationStatus" != "MIGRATION_COMPLETED" ] && [ "$MigrationStatus" != "NOT_NEEDED" ]; then
          echo "BOOT_TYPE=BOOT_MIGRATION" > $file_bootType
	      boottypeLog "BOOT_MIGRATION since MigrationStatus is not equal to MIGRATION_COMPLETED"
     elif [ "$MigrationStatus" == "MIGRATION_COMPLETED" ] || [ "$MigrationStatus" == "NOT_NEEDED" ]; then
	     if [ "$v_version" == "$s1_version" ]; then
	         echo "BOOT_TYPE=BOOT_NORMAL" > $file_bootType
	    	 boottypeLog "BOOT_NORMAL since Version is equal and MigrationStatus is MIGRATION_COMPLETED"
	     else
	         echo "BOOT_TYPE=BOOT_UPDATE" > $file_bootType
	         boottypeLog "BOOT_UPDATE since Version is not equal"
             fi
     fi
fi

#update the read permission to migration datastore files
current_bootType=$(<"$file_bootType")
current_bootType=${current_bootType:10}
if [ "$current_bootType" == "BOOT_MIGRATION" ]; then
    migrationDSFile="/opt/secure/migration/migration_data_store.json"
    migrationDir="/opt/secure/migration"

    # Check if the directory exists
    if [ -d "$migrationDir" ]; then
        boottypeLog "changed the permission of $migrationDir by +x"
        chmod +x "$migrationDir"
    fi

    # Check if the file exists
    if [ -f "$migrationDSFile" ]; then
        boottypeLog "changed the permission of $migrationDSFile by +r"
        chmod +r "$migrationDSFile"
    fi
fi

echo "COMPLETED" > $file_updateStatus
boottypeLog "Update completed."
