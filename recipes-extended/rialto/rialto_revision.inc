######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "ce58a2a2b1cc9f6508b35282a3c4f72910b82b2b"
RIALTO_BRANCH ?= "feature/RDKEMW-17771"
RIALTO_VERSION ?= "0.22.1"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "459651b3d33b40f4f6161a9789786d653cd762f5"
RIALTO_GSTREAMER_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_GSTREAMER_VERSION ?= "0.20.1"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "7ca4fcc051b67e511b17ce1c088be3c742dde684"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "0.10.0"
RIALTO_OCDM_PR ?= "r0"
