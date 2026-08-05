######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "120d0255423eecba9479dfccb885f76da3d53353"
RIALTO_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_VERSION ?= "v0.22.5"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "459651b3d33b40f4f6161a9789786d653cd762f5"
RIALTO_GSTREAMER_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_GSTREAMER_VERSION ?= "v0.20.1"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "795ec4e839d564579842bcb23929b5f28e2b7d2c"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.10.5"
RIALTO_OCDM_PR ?= "r0"
