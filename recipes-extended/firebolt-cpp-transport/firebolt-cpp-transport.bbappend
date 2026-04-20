# <<<rdkforge-begin>>>
# rdkforge dev-build override for topic/RDKEMW-16441
# Remove this file before bumping PV for a release build.
SRC_URI = "git://github.com/rdkcentral/firebolt-cpp-transport.git;protocol=https;branch=topic/RDKEMW-16441"
SRCREV  = "b730ddae7df7c3cf177dd672cf621c2eb299ff63"
S       = "${WORKDIR}/git"
# <<<rdkforge-end>>>

PACKAGECONFIG:append = " legacy-rpc-v1"
