#!/bin/sh

while true; do
    if systemctl -q is-active wpeframework.service; then
        all_services_ok=true

        # Check the OCDM Service
        if systemctl -q is-active wpeframework-ocdm.service; then
            echo "wpeframework-ocdm is running."
        else
            echo "SELFHEAL: wpeframework-ocdm is NOT running. Lets Restart"
            systemctl start wpeframework-ocdm
            all_services_ok=false
        fi

        # Check the Mediarite Service
        if systemctl -q is-active mediarite.service; then
            echo "mediarite is running."
        else
            echo "SELFHEAL: mediarite is NOT running. Lets Restart"
            systemctl start mediarite
            all_services_ok=false
        fi

        # Check the AirPlay Service
        if systemctl -q is-active airplay-daemon.service; then
            echo "airplay-daemon is running."
        else
            echo "SELFHEAL: airplay-daemon is NOT running. Lets Restart"
            systemctl start airplay-daemon
            all_services_ok=false
        fi

        if $all_services_ok; then
            break
        else
            echo "SELFHEAL: Re-run to ensure all is good.!"
            sleep 5
        fi
    else
        echo "SELFHEAL: WPEFramework is NOT running. Just wait 30s and try later"
        sleep 30
    fi
done
