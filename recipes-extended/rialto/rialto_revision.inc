######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "f606e0852a074c7a4dc86dabcb7bfdf7682dba37"
RIALTO_BRANCH ?= "MediaCapabilitiesRialtoServerManager"
RIALTO_VERSION ?= "v0.22.3"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "9fde9510b07534cfeaed7e96e7bcda100e829df1"
RIALTO_GSTREAMER_BRANCH ?= "MediaPipelineCapabilitiesNewConfig"
RIALTO_GSTREAMER_VERSION ?= "v0.20.1"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "0ef215962e7c44505f615df73a257cafeccf6778"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.10.0"
RIALTO_OCDM_PR ?= "r0"
