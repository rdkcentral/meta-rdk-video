#!/bin/bash
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

#Packages to be installed for TDK Agent should be hosted in server and the IP address has to be passed as an argument to the script (For example:http://x.x.x.x/TDK_ipks/$IMAGE_NAME)
#Packages to be installed for TDK Agent hosted in http://96.118.12.177/TDK_ipks/$IMAGE_NAME
#Check for Input parameter(Server IP)otherwise give default IP
if [[ "$1" != "" ]]; then
    SERVER_IP="$1"
fi
#make a directory and create a soft link to opkg location
mkdir -p /opt/opkg
ln -sf /var/lib/opkg /opt/opkg

#configure ethernet
#udhcpc -i eth1

#get image name for determining the ipk location
IMAGE_NAME=$(cat /version.txt | grep imagename | cut -d: -f 2 | tr -d '\r\n')

#install tdk package
opkg --nodeps install http://$SERVER_IP/TDK_ipks/$IMAGE_NAME/tdk.ipk
opkg --nodeps install http://$SERVER_IP/TDK_ipks/$IMAGE_NAME/libservicemanagerstub.ipk

#install the dependency packages for tdk
opkg --nodeps -f /var/InstallTDK/opkg.conf -d opt install http://$SERVER_IP/TDK_ipks/$IMAGE_NAME/jsonrpc.ipk
opkg --nodeps -f /var/InstallTDK/opkg.conf -d opt install http://$SERVER_IP/TDK_ipks/$IMAGE_NAME/libjsoncpp.ipk

#move the dependency libraries to proper locations
mv /opt/TDK/lib/usr/lib/* /opt/TDK/lib/
rm -rf /opt/TDK/lib/usr/

#copy the opkg info to proper locations
cat /opt/TDK/lib/var/lib/opkg/status >> /opt/opkg/status
cp /opt/TDK/lib/var/lib/opkg/info/* /opt/opkg/info

#remove unwanted folders
rm -rf /opt/TDK/lib/var/

#Reboot the STB after installation
sh /rebootNow.sh -s InstallTDK -o "Rebooting the box after TDK installation..."
