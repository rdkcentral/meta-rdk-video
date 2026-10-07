PACKAGECONFIG:append = " legacy-rpc-v1"

# Dev build: pinned to RDKEMW-21295 branch commit (waitTime_ms 3000→5000ms fix +
# FIREBOLT_WAIT_TIME_MS env override).
# Remove or comment out the lines below to revert to the prod tarball recipe.
SRCREV = "c4bb2678a0f89cb70d5da9e056a1bd799532771a"
SRC_URI = "git://github.com/rdkcentral/firebolt-cpp-transport.git;protocol=https;branch=support/RDKEMW-21295"
S = "${WORKDIR}/git"
PV = "0.0.0+git${SRCPV}"
LIC_FILES_CHKSUM = "file://LICENSE;md5=724ed260f33bc706a8fbafbbb35a316b"
