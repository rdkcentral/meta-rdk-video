RIPPLE_VERSION = "1.23.0"
CARGO_SRC_DIR = ""

# services inherited
inherit systemd cargo

# '--frozen' is enabled by default via meta-lts-mixin's cargo class,
# which blocks updates to Cargo.lock. Since Ripple needs extra crates
# not listed in Cargo.lock, they are added via firebolt-dependencies.inc.
# Removing '--frozen' allows the build to include these crates.

CARGO_BUILD_FLAGS:remove = "--frozen"

#dependencies from crates.io
require include/firebolt-dependencies.inc

#Dependency for cryptographic operations
DEPENDS += " openssl"

OPEN_RIPPLE_SRCURI = "${CMF_GITHUB_ROOT}/Ripple.git;protocol=${CMF_GITHUB_PROTOCOL};nobranch=1;destsuffix=Ripple"
OPEN_RIPPLE_SRCVER = "${@bb.utils.contains('DISTRO_FEATURES','FIREBOLT_RIPPLE_EDGE','${AUTOREV}','${RIPPLE_VERSION}',d)}"
OPEN_RIPPLE_S="${WORKDIR}/Ripple"

