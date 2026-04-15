PACKAGECONFIG:append = " legacy-rpc-v1"

# dev-build override — this branch (dev-topic/RDKEMW-16441) is never merged to develop.
# Update SRCREV after each push to firebolt-cpp-transport topic/RDKEMW-16441.
SRC_URI = "git://github.com/rdkcentral/firebolt-cpp-transport.git;branch=topic/RDKEMW-16441;protocol=https"
SRCREV = "b730ddae7df7c3cf177dd672cf621c2eb299ff63"
S = "${WORKDIR}/git"
