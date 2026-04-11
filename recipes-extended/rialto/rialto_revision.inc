######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

# Source revisions - update these to trigger Jenkins builds
RIALTO_HEAD ?= "6688f0aea9e132d1f37e79202b7fd73d0980de48"
RIALTO_GSTREAMER_HEAD ?= "214538922ac739bc069568ed4b4f8391314d8deb"
RIALTO_OCDM_HEAD ?= "0ef215962e7c44505f615df73a257cafeccf6778"

# Component versions
RIALTO_VERSION ?= "0.19.0"
RIALTO_GSTREAMER_VERSION ?= "0.17.0"
RIALTO_OCDM_VERSION ?= "0.10.0"

# Package revisions
RIALTO_PR ?= "r0"
RIALTO_GSTREAMER_PR ?= "r0"
RIALTO_OCDM_PR ?= "r0"
