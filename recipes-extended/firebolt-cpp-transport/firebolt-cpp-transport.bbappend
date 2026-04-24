PACKAGECONFIG:append = " legacy-rpc-v1"

# Dev build override — point at git instead of the (unreleased) tarball.
# Remove SRC_URI / SRCREV / S overrides once the tarball release is available.
SRC_URI = "git://github.com/rdkcentral/firebolt-cpp-transport.git;branch=fix/RDKEMW-17439;protocol=https;nobranch=1"
SRCREV = "be5cb9edcee8b45f199805e44eec889a565e0f83"
S = "${WORKDIR}/git"
