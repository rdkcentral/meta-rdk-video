######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "60d1315837649be4d7a996a08a00a52357d1c139"
RIALTO_BRANCH ?= "topic/RialtoTestMultipleChanges"
RIALTO_VERSION ?= "v0.24.1"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "b0b68782eaec0eb9824e49455e204aedece86f1a"
RIALTO_GSTREAMER_BRANCH ?= "topic/RialtoTestMultipleChanges"
RIALTO_GSTREAMER_VERSION ?= "v0.22.1"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "5837ac4cf67201ba258e8c5dd52777fd9aa5e26b"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.11.0"
RIALTO_OCDM_PR ?= "r0"
