LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=24ad7308dd1ab37b3f813fa022b0595b"

SRC_URI = "\
    ${CMF_GIT_ROOT}/rdk/components/generic/streamfs;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GIT_BRANCH} \
    file://streamfs.service \
"

PV = "1.0+git${SRCPV}"
SRCREV = "90914ec0a6a3991fe33096277769bbc089442fca"

S = "${WORKDIR}/git"

DEPENDS = "boost curl glog fuse gtest systemd"

FILES:${PN}-dev = "${includedir}"

FILES:${PN}="${libdir} ${bindir} ${systemd_unitdir}/system/*"

inherit cmake systemd

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/streamfs.service ${D}${systemd_unitdir}/system
}

SYSTEMD_SERVICE:${PN} = "streamfs.service"

EXTRA_OECMAKE = ""
