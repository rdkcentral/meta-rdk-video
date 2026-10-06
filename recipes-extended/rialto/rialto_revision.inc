######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "777877e6f23ab297741ae8a87bed60342d640e30"
RIALTO_BRANCH ?= "release/v0.22.4"
RIALTO_VERSION ?= "v0.22.4-r2"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "4c58d1c3d5677d9daa513fcc01fced6a67557824"
RIALTO_GSTREAMER_BRANCH ?= "release/v0.20.1"
RIALTO_GSTREAMER_VERSION ?= "v0.20.1-r2"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "0ef215962e7c44505f615df73a257cafeccf6778"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.10.0"
RIALTO_OCDM_PR ?= "r0"
