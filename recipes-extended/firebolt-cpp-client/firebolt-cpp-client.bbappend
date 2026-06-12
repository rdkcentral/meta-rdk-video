# Dev override: build firebolt-cpp-client from branch tip instead of release tarball
SRC_URI = "git://github.com/rdkcentral/firebolt-cpp-client.git;protocol=https;branch=feature/RDKEMW-17483_rc2"
SRCREV = "${AUTOREV}"
S = "${WORKDIR}/git"

# Keep package version monotonic while using dynamic git revisions
PV:append = "+git${SRCPV}"
