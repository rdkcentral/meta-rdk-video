# Dev build: pinned to the client-version-reporting branch commit.
# Remove or comment out to revert to the prod tarball recipe.
SRCREV = "6a122b36fbec815710c2ccf69eb042c075c08377"
SRC_URI = "git://github.com/rdkcentral/firebolt-cpp-client.git;protocol=https;branch=feat/client-version-reporting"
S = "${WORKDIR}/git"
PV = "0.0.0+git${SRCPV}"
EXTRA_OECMAKE:append = " -DFIREBOLT_GIT_REF=${SRCREV}"
