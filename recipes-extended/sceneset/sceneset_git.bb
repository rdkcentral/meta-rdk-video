SUMMARY = "This recipe provides the sceneset component for RDK "

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

PV = "0.4.0"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit cmake pkgconfig systemd

# Config exposed variable is FACTORY_APPS_PATH, map it as FACTORY_APP_PATH

EXTRA_OECMAKE += "-DSCENESET_DEFAULT_APPNAME='${SCENESET_DEFAULT_APPNAME}' \
                 -DFACTORY_APP_PATH='${FACTORY_APPS_PATH}' \
                 -DAPP_PREINSTALL_DIRECTORY='${APP_PREINSTALL_DIRECTORY}'"

DEPENDS += "wpeframework entservices-apis ralf-utils"
RDEPENDS:${PN} += " ralf-utils"

#RDK logging support
inherit syslog-ng-config-gen

SYSLOG-NG_FILTER = "sceneset"
SYSLOG-NG_SERVICE_sceneset = "sceneset.service"
# Logs to /opt/logs/sceneset.log
SYSLOG-NG_DESTINATION_sceneset = "sceneset.log"
SYSLOG-NG_LOGRATE_sceneset = "high"

SRCREV = "2f71df021fdf26c382d2211411b8122f69134f79"
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
