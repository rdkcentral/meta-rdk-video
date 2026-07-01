PACKAGECONFIG:append = " legacy-rpc-v1"

# Dev build: pinned to the runtime-configurable-logging branch commit.
# Remove or comment out to revert to the prod tarball recipe.
SRCREV = "3a742f8b551b0c1ae09fbddaca5f04e45d900d47"
SRC_URI = "git://github.com/rdkcentral/firebolt-cpp-transport.git;protocol=https;branch=nojira/runtime-configurable-looging"
S = "${WORKDIR}/git"
PV = "0.0.0+git${SRCPV}"
EXTRA_OECMAKE:append = " -DFIREBOLT_GIT_REF=${SRCREV}"
