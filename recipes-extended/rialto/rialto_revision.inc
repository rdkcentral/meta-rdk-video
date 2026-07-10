######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "4269708f58178e33f4272fe6c7797a686c4394e0"
RIALTO_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_VERSION ?= "v0.22.4"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "0f0a1cecbf37ac6257b99c96c7aae3210f1d54e5"
RIALTO_GSTREAMER_BRANCH ?= "feature/RDKEMW-21388"
RIALTO_GSTREAMER_VERSION ?= "v0.20.1"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "7ca4fcc051b67e511b17ce1c088be3c742dde684"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.10.1"
RIALTO_OCDM_PR ?= "r0"
