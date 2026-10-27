######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "1a01a355473d3d0fd12b3ee809759fdf01f00c26"
RIALTO_BRANCH ?= "PersistentLicenseSupport"
RIALTO_VERSION ?= "v0.25.0"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "e4325dd947928542017b820c29b3dcf9bc80edef"
RIALTO_GSTREAMER_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_GSTREAMER_VERSION ?= "v0.23.0"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "831b1fbacef0c7a65acdd62960423441fbb6b825"
RIALTO_OCDM_BRANCH ?= "PersistentLicenseSupport"
RIALTO_OCDM_VERSION ?= "v0.11.0"
RIALTO_OCDM_PR ?= "r0"
