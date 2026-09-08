# Sample override for consuming a development version of firebolt-cpp-client.
#
# How to use:
# 1) Uncomment the block below.
# 2) Set the desired branch and pin SRCREV to a commit for reproducible builds.
# 3) Optionally use AUTOREV for rapid local iteration (not reproducible).
#
# NOTE: Keep this block commented in normal CI/release flows.

# Use a git checkout instead of the release tarball defined in the base recipe.
# Branch is parametric so dev builds can switch branch by changing one variable.
# FIREBOLT_CPP_CLIENT_GIT_BRANCH ?= "develop"
# PV = "0.6.4+git${SRCPV}"
#SRC_URI = "git://github.com/rdkcentral/firebolt-cpp-client.git;protocol=https;branch=${FIREBOLT_CPP_CLIENT_GIT_BRANCH}"
#
# Optional (local.conf) examples:
# FIREBOLT_CPP_CLIENT_GIT_BRANCH = "feature/my-dev-branch"
# SRCREV:pn-firebolt-cpp-client = "<replace-with-commit-sha>"

# Pin to a specific commit for deterministic builds.
# SRCREV = "<replace-with-commit-sha>"
# For local development only (non-reproducible), you can use:
# SRCREV = "${AUTOREV}"

# Source directory for git fetcher.
# S = "${WORKDIR}/git"

# Not used for git sources; clear tarball checksum from the base recipe override path.
# SRC_URI[sha256sum] = ""


FIREBOLT_CPP_CLIENT_GIT_BRANCH ?= "fix/videooutput-bad-marshalling"
SRC_URI = "git://github.com/rdkcentral/firebolt-cpp-client.git;protocol=https;branch=${FIREBOLT_CPP_CLIENT_GIT_BRANCH}"
SRCREV = "f703838baa2a9ab80e130ab5c598e698233d5a64"
S = "${WORKDIR}/git"
SRC_URI[sha256sum] = ""
