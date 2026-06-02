#!/bin/sh

pidstat -h -u -r -d -p ALL 1 > /opt/logs/ds-processes-load.log