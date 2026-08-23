######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "76ea76c3757b4f5af2692bf16eee6546602d6dc5"
RIALTO_BRANCH ?= "MediaCapabilitiesRialtoServerManager"
RIALTO_VERSION ?= "v0.24.1"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "f928591f7790c95815a0c2cb8f87347eeb9760a7"
RIALTO_GSTREAMER_BRANCH ?= "MediaPipelineCapabilitiesNewConfig"
RIALTO_GSTREAMER_VERSION ?= "v0.22.1"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "5837ac4cf67201ba258e8c5dd52777fd9aa5e26b"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.11.0"
RIALTO_OCDM_PR ?= "r0"
