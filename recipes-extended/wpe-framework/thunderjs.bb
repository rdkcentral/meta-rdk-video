SUMMARY = "A flexible and extensible JS library to interact with Thunder (WPEframework)"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=cbffef879caf8b1db2d5173ff472c28c"

SRC_URI = "git://github.com/rdkcentral/ThunderJS.git;protocol=https;branch=master"

# Last Check-in at Aug 28
SRCREV = "903f843ebbe9136609fbe348b1e39c306489c0b1"
S = "${WORKDIR}/git"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

DEPENDS = "wpeframework"

do_configure[noexec] = "1"

do_compile() {
    # As the lightening++ and Spark is thro node, we do not need `isomorphic-ws`.
    # We can added it later if needed; as it takes 24K disk space.
    sed -i  s/isomorphic-ws/ws/g ${S}/module/thunderJS.js
}

do_install() {
    mkdir -p ${D}/home/root/node_modules/
    cp -r ${S}/module/thunderJS.js ${D}/home/root/node_modules/thunderJS.js
}

FILES:${PN} = "/home/root/*"


