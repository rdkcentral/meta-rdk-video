######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

# Source revisions/branches - update these to trigger Jenkins builds
RIALTO_SRCREV ?= "6688f0aea9e132d1f37e79202b7fd73d0980de48"
RIALTO_GSTREAMER_SRCREV ?= "214538922ac739bc069568ed4b4f8391314d8deb"
RIALTO_OCDM_SRCREV ?= "0ef215962e7c44505f615df73a257cafeccf6778"

RIALTO_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_GSTREAMER_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"

# Component versions
RIALTO_VERSION ?= "0.19.0"
RIALTO_GSTREAMER_VERSION ?= "0.17.0"
RIALTO_OCDM_VERSION ?= "0.10.0"

# Package revisions
RIALTO_PR ?= "r0"
RIALTO_GSTREAMER_PR ?= "r0"
RIALTO_OCDM_PR ?= "r0"
