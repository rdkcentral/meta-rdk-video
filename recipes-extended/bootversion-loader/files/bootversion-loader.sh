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
     echo -e "BOOT_INIT is set since $file_bootversion is not present"
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

echo -e "MigrationStatus: $MigrationStatus"
#comparing slot1 and slot2 FW Class
if [ "$v_FW_Class" != "$s1_FW_Class" ]; then
	# migration fw is run for first time, migration not completed
	echo "NOT_STARTED" > $file_MigrationStatus
	echo "BOOT_TYPE=BOOT_MIGRATION" > $file_bootType
	echo -e "BOOT_MIGRATION is set since FW_Class is not same"
else
     if [ "$MigrationStatus" != "MIGRATION_COMPLETED" ]; then
          echo "BOOT_TYPE=BOOT_MIGRATION" > $file_bootType
	  echo -e "BOOT_MIGRATION since MigrationStatus is not equal to MIGRATION_COMPLETED"
     elif [ "$MigrationStatus" == "MIGRATION_COMPLETED" ]; then
	     if [ "$v_version" == "$s1_version" ]; then
	         echo "BOOT_TYPE=BOOT_NORMAL" > $file_bootType
	    	 echo -e "BOOT_NORMAL since Version is equal and MigrationStatus is MIGRATION_COMPLETED"
	     else
	         echo "BOOT_TYPE=BOOT_UPDATE" > $file_bootType
	         echo -e "BOOT_UPDATE since Version is not equal"
             fi
     fi
fi
