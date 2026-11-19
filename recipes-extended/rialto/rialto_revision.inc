######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "77f4ecfb11758c7d0b60a2bab3b029ae79ac4866"
RIALTO_BRANCH ?= "OnLicenseRelease"
RIALTO_VERSION ?= "v1.0.0"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "66bd7b0568fe316789bfc9332427a32ab0234f9f"
RIALTO_GSTREAMER_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_GSTREAMER_VERSION ?= "v1.0.0"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "b06a0fed0b08d75ee2a09f84da56295ed694abfa"
RIALTO_OCDM_BRANCH ?= "OnLicenseRelease"
RIALTO_OCDM_VERSION ?= "v1.0.0"
RIALTO_OCDM_PR ?= "r0"
