######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "40f7d7b111b53f398622f8b21fb3d6523653f845"
RIALTO_BRANCH ?= "feature/RDKEMW-28580"
RIALTO_VERSION ?= "v1.0.0"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "1ba783fd9e1d96872c41175cb3df2a814bed5e60"
RIALTO_GSTREAMER_BRANCH ?= "feature/RDKEMW-28580"
RIALTO_GSTREAMER_VERSION ?= "v1.0.0"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "ab99856aa75738ff5173b3b66c697364d8b679d5"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v1.0.0"
RIALTO_OCDM_PR ?= "r0"
