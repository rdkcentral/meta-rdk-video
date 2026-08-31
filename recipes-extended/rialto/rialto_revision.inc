######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "4c25e3861251f01279f4718eb21b5e0e1f9dc164"
RIALTO_BRANCH ?= "SuspendOption2"
RIALTO_VERSION ?= "v0.24.1"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "b128a2f30a8232ba9beb3cb5f0fb4b3094fc56ad"
RIALTO_GSTREAMER_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_GSTREAMER_VERSION ?= "v0.22.1"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "066a15414391072e24524bcabb8e593dd979bf3b"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.11.0"
RIALTO_OCDM_PR ?= "r0"
