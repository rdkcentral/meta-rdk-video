RIPPLE_VERSION = "1.23.0"
CARGO_SRC_DIR = ""

# services inherited
inherit systemd cargo

#dependencies from crates.io
require include/firebolt-dependencies.inc

#Dependency for cryptographic operations
DEPENDS += " openssl"

OPEN_RIPPLE_SRCURI = "${CMF_GITHUB_ROOT}/Ripple.git;protocol=${CMF_GITHUB_PROTOCOL};nobranch=1;name=rmain;destsuffix=Ripple"
OPEN_RIPPLE_SRCVER = "${@bb.utils.contains('DISTRO_FEATURES','FIREBOLT_RIPPLE_EDGE','${AUTOREV}','${RIPPLE_VERSION}',d)}"
OPEN_RIPPLE_S="${WORKDIR}/Ripple"

