######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "9cce9884d273ba2d293fdd487cbc4bc1b40e1a67"
RIALTO_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_VERSION ?= "v0.25.0"
RIALTO_PR ?= "c1.1"

RIALTO_GSTREAMER_SRCREV ?= "68a37fa44b98b9a2a8950cac6c5810c2a8927f63"
RIALTO_GSTREAMER_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_GSTREAMER_VERSION ?= "v0.23.0"
RIALTO_GSTREAMER_PR ?= "c1.1"

RIALTO_OCDM_SRCREV ?= "990e56634478bcb3d86c4e5ef18e66ce787f1245"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.11.0"
RIALTO_OCDM_PR ?= "ld"
