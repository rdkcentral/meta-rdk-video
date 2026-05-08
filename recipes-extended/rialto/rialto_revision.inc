######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "c77df264dfa7d46452daf6157b55462075d3c375"
RIALTO_BRANCH ?= "topic/RDKEMW-13397"
RIALTO_VERSION ?= "0.21.0"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "7bf430bf9e1f38e98375d7ea2fe67d40702e8339"
RIALTO_GSTREAMER_BRANCH ?= "topic/RDKEMW-13397"
RIALTO_GSTREAMER_VERSION ?= "0.19.0"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "953e8cd8af930733ba846b49e08c68d188b72b99"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "0.10.1"
RIALTO_OCDM_PR ?= "r0"
