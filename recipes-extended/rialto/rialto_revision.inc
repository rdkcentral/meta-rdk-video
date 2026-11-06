######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "75c5ca6be5e5afa5f9f44b15432c6d6cae4c7429"
RIALTO_BRANCH ?= "TimerFix_86"
RIALTO_VERSION ?= "v0.22.4-r5"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "bf8dcf8e17cc6d0ce950d566fbc7f3989a77a1f1"
RIALTO_GSTREAMER_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_GSTREAMER_VERSION ?= "v0.20.1-r3"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "bbdc66c8a9817e344812c586238ec4abf1d6fa53"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.10.0"
RIALTO_OCDM_PR ?= "r0"
