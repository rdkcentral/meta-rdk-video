LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c9b91a55d27709f93abca83684156b8e"

SRC_URI = "${CMF_GIT_ROOT}/rdk/components/generic/streamfs_fcc;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GIT_BRANCH}"

# Modify these as desired
PV = "1.0+git${SRCPV}"
SRCREV = "71af748f66c1aac362255c818fe6217c626e9eb0"

S = "${WORKDIR}/git"

DEPENDS = "glog boost streamfs"
RDEPENDS:${PN} = "boost-filesystem boost-regex"
inherit cmake

FILES:${PN}="${libdir}"
