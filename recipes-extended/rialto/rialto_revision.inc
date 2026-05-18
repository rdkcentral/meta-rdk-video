######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "5eee279616b3f35a5afdf513131aa830543d1e28"
RIALTO_BRANCH ?= "feature/RialtoHDMIIssue"
RIALTO_VERSION ?= "v0.21.0"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "86f6e2112fbbd9ab773224f7032106bfe1867554"
RIALTO_GSTREAMER_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_GSTREAMER_VERSION ?= "0.19.0"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "7ca4fcc051b67e511b17ce1c088be3c742dde684"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "0.10.1"
RIALTO_OCDM_PR ?= "r0"
