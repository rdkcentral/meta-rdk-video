######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems


RIALTO_SRCREV ?= "90d3a7062824f35a4f6d47e886d3bb5c36f8fae9"
RIALTO_BRANCH ?= "MediaPipelineCapabilitiesNewConfig"
RIALTO_VERSION ?= "v0.22.3"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "a69d2c775fd0ca1497c216ca23dc42c94f83c07f"
RIALTO_GSTREAMER_BRANCH ?= "MediaPipelineCapabilitiesNewConfig"
RIALTO_GSTREAMER_VERSION ?= "v0.20.1"
RIALTO_GSTREAMER_PR ?= "r0"


RIALTO_OCDM_SRCREV ?= "0ef215962e7c44505f615df73a257cafeccf6778"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.10.0"
RIALTO_OCDM_PR ?= "r0"
