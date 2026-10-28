######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "25113be45d1ef90c47dc5e160386d8aed6b0ed39"
RIALTO_BRANCH ?= "feature/RDKEMW-27657-Pipeline"
RIALTO_VERSION ?= "v0.25.0"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "252b1e7d8db9d90fd12e5fa4de81ff00d5c0e7b7"
RIALTO_GSTREAMER_BRANCH ?= "feature/RDKEMW-27657-Pipeline"
RIALTO_GSTREAMER_VERSION ?= "v0.23.0"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "5837ac4cf67201ba258e8c5dd52777fd9aa5e26b"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.11.0"
RIALTO_OCDM_PR ?= "r0"
