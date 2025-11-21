SUMMARY = "This recipe provides the sceneset component for RDK "

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

inherit cmake pkgconfig systemd

DEPENDS += "wpeframework entservices-apis"

SRC_URI = "${CMF_GITHUB_ROOT}/sceneset;${CMF_GITHUB_SRC_URI_SUFFIX};name=sceneset"
SRCREV_FORMAT = "sceneset"

S = "${WORKDIR}/git"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${S}/systemd/sceneset.service ${D}${systemd_unitdir}/system/sceneset.service
}

FILES:${PN} += "${bindir}/*"
FILES:${PN} += "${systemd_unitdir}/system/*"

SYSTEMD_SERVICE:${PN} = "sceneset.service"

SRCREV_sceneset = "b9fc1bca0c1b42c72825ae1adecc07a7b6170c75"

