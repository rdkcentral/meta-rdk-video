#!/bin/sh

sleep 8

if [ -f /etc/os-release ];then
    /bin/systemd-notify --ready --status="wait Done..!"
fi
