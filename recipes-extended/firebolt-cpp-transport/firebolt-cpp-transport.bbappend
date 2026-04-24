# dev-build override: pull from topic/nojira-yet-just-logging (deadlock fixes: watchdog cv, subscribe/unsubscribe wait_for)
# Remove this file's SRC_URI/SRCREV overrides before bumping PV for a release build.
SRC_URI = "git://github.com/rdkcentral/firebolt-cpp-transport.git;branch=topic/nojira-yet-just-logging;protocol=https"
SRCREV = "8d32a1875816d221b8b97af50a364c4a61337e62"
S = "${WORKDIR}/git"

PACKAGECONFIG:append = " legacy-rpc-v1"
