######################################################################
# RIALTO
######################################################################
#
# Rialto provides a solution to implement the AV (audio and video) pipelines of containerised native applications
# and browsers without exposing hardware-specific handles and critical system resources inside the application containers
#
# Please contact DL-Rialto@sky.uk if you want to change this file or in case of problems

RIALTO_SRCREV ?= "5e8856835f0f1e2ea8349157544236c63513f9d1"
RIALTO_BRANCH ?= "feature/RDKEMW-17569"
RIALTO_VERSION ?= "v0.15.2-r7"
RIALTO_PR ?= "r0"

RIALTO_GSTREAMER_SRCREV ?= "0c3befcc42212de7cbb1113113e9387318b9b1c2"
RIALTO_GSTREAMER_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_GSTREAMER_VERSION ?= "v0.14.0-r2"
RIALTO_GSTREAMER_PR ?= "r0"

RIALTO_OCDM_SRCREV ?= "4f8e4556754cde3498d960d47fd0827f997ea43b"
RIALTO_OCDM_BRANCH ?= "${CMF_GITHUB_MASTER_BRANCH}"
RIALTO_OCDM_VERSION ?= "v0.9.3"
RIALTO_OCDM_PR ?= "r0"
