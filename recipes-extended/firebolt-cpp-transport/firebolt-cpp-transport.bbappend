# dev-build override: pull from topic/nojira-yet-just-logging (enhanced disconnect/subscribe logging)
# Remove this file's SRC_URI/SRCREV overrides before bumping PV for a release build.
SRC_URI = "git://github.com/rdkcentral/firebolt-cpp-transport.git;branch=topic/nojira-yet-just-logging;protocol=https"
SRCREV = "652654dbc982e262158b98aacb332b8da0331b87"
S = "${WORKDIR}/git"

PACKAGECONFIG:append = " legacy-rpc-v1"
