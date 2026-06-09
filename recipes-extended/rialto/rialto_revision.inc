######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "efdbf2ec66b8f5fbb381371d0fb6c95d7df30ddf"
RIALTO_BRANCH ?= "feature/RDKEMW-17771"
RIALTO_VERSION ?= "0.21.0"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "48de19ae1f061700c03c7662c6d99476d1be11b6"
RIALTO_GSTREAMER_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_GSTREAMER_VERSION ?= "0.19.0"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "7ca4fcc051b67e511b17ce1c088be3c742dde684"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "0.10.0"
RIALTO_OCDM_PR ?= "r0"
