######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "5ee72a3cfaf279c958a66730d5a7acbe2ac85a8a"
RIALTO_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_VERSION ?= "v0.22.1"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "4b32e39daa8eb89dc5d38f258b721ad7306d6475"
RIALTO_GSTREAMER_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_GSTREAMER_VERSION ?= "v0.20.1"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "0ef215962e7c44505f615df73a257cafeccf6778"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.10.0"
RIALTO_OCDM_PR ?= "r0"
