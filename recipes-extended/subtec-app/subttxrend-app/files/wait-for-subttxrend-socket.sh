#!/bin/sh

# Tell appsserviced to open the subtitle westeros socket
/usr/bin/appsservicectl start-subtitles-client

# Just wait for it to be created
# systemd file has a timeout so will not loop forever
while [ ! -S /tmp/westeros-asplayer-subtitles ]
do
    /bin/sleep 1
done
