######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "c5c14f346e538415ba3ec6f7e2eb5b8e26ff2641"
RIALTO_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_VERSION ?= "v0.26.0"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "6c8b54dd2835b7ef16ea278dfb8f2010f1722705"
RIALTO_GSTREAMER_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_GSTREAMER_VERSION ?= "v0.24.0"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "990e56634478bcb3d86c4e5ef18e66ce787f1245"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.12.0"
RIALTO_OCDM_PR ?= "r0"
