######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "4019e1c38207df0f3647ba4f08d565d9dcba9ff5"
RIALTO_BRANCH ?= "feature/RDKEMW-15080_env"
RIALTO_VERSION ?= "v0.26.0"
RIALTO_PR ?= "rn0"

RIALTO_GSTREAMER_SRCREV ?= "fd85b97f08804ddba8872ddf97ad189e690afc55"
RIALTO_GSTREAMER_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_GSTREAMER_VERSION ?= "v0.24.0"
RIALTO_GSTREAMER_PR ?= "rn0"

RIALTO_OCDM_SRCREV ?= "990e56634478bcb3d86c4e5ef18e66ce787f1245"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.12.0"
RIALTO_OCDM_PR ?= "rn0"
