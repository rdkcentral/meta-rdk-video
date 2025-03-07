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

file_read="/version.txt"
file_write="/opt/.bootversion"
file_temp="/tmp/boots1.txt"

imagename=$(grep "^imagename" $file_read)
version=$(grep "^VERSION" $file_read |  tr "=" ":")
FW_Class=$(grep "^FW_CLASS" $file_read | tr "=" ":")


#case 1: file does not exist
if [ ! -e "$file_write" ]; then
    touch $file_write
    echo -e "bootversion-loader: $file_write does not exist so creating the file and writing values to slot1 \n"
    echo "$imagename" > $file_write
    echo "$version" >> $file_write
    echo "$FW_Class" >> $file_write
    echo -e "bootversion-loader: updated slot1 of $file_write successfully\n"
#case 2: file already exist
else
   echo -e "bootversion-loader: $file_write already exists so writing values to slot1 and moving the values from slot1 to slot2 \n"
   #copy slot 1 to temp
   touch $file_temp
   head -n 3 $file_write > $file_temp
   #empty the file
   > $file_write
   #write new content to slot 1
   echo "$imagename" > $file_write
   echo "$version" >> $file_write
   echo "$FW_Class" >> $file_write
   #write the slot 1 content to slot2
   cat $file_temp >> $file_write
   echo -e "bootversion-loader: updated $file_write with both slots successfully\n"
   rm -rf $file_temp
fi
