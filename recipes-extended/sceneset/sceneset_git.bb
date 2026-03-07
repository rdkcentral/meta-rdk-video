SUMMARY = "This recipe provides the sceneset component for RDK "

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

PV = "0.1.0"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit cmake pkgconfig systemd

EXTRA_OECMAKE += "-DSCENESET_DEFAULT_APPNAME='${SCENESET_DEFAULT_APPNAME}' \
                 -DFACTORY_APP_PATH='${FACTORY_APP_PATH}' \
                 -DAPP_PREINSTALL_DIRECTORY='${APP_PREINSTALL_DIRECTORY}'"

DEPENDS += "wpeframework entservices-apis"

SRCREV = "bd0bfe2c11788158b8025756d6b61f38304177a7"
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
