######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "e68cb17c4da3c8cb1794837b569699e780ebc7e5"
RIALTO_BRANCH ?= "MediaPipelineCapabilitiesNewConfig"
RIALTO_VERSION ?= "v0.22.3"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "dc0c97e8f3e456927b2faa3a3697180bbe869267"
RIALTO_GSTREAMER_BRANCH ?= "MediaPipelineCapabilitiesNewConfig"
RIALTO_GSTREAMER_VERSION ?= "v0.20.1"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "0ef215962e7c44505f615df73a257cafeccf6778"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.10.0"
RIALTO_OCDM_PR ?= "r0"
