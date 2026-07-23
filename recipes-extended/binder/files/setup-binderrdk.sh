#!/bin/bash

echo "Creating binderfs mount directory..."
mkdir -p /dev/binderfs

if ! mountpoint -q /dev/binderfs; then
    echo "Mounting binder filesystem..."
    if ! mount -t binder binder /dev/binderfs; then
        echo "Error: Failed to mount binder filesystem."
        exit 1
    fi
else
    echo "Binder filesystem already mounted."
fi

echo "Creating symbolic link for the binder device..."
ln -sf /dev/binderfs/binder /dev

echo "Binder setup completed."

