SUMMARY = "This receipe compiles the ermgr component"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1d13a8bfca16dbdad01fe5f270451aaa"

PV = "${RDK_RELEASE}"
S = "${WORKDIR}/git"
SRCREV = "125b87e9dd639d333ea9915fb0b8e463a6adedfa"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
SRC_URI = "${CMF_GIT_ROOT}/rdk/components/generic/ermgr;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GIT_BRANCH}"

DEPENDS = "essos"
inherit autotools pkgconfig systemd syslog-ng-config-gen

SYSLOG-NG_FILTER = "wpeframework"
SYSLOG-NG_SERVICE_wpeframework = "ermgr.service"
#destination and lograte of wpeframework filter are already set in wpeframework recipe

acpaths = "-I cfg"

do_install() {
   install -d ${D}/usr/bin
   install -d ${D}${systemd_unitdir}/system
   cp -r ${B}/ermgr ${D}/usr/bin/
   install -m 0644 ${S}/conf/ermgr.service ${D}${systemd_unitdir}/system
   if ${@bb.utils.contains('DISTRO_FEATURES', 'use_westeros_essrmgr_uds', 'true', 'false', d)}; then
       sed -i "/^Before=/ s/ audioserver.service tvserver.service$//" ${D}${systemd_unitdir}/system/ermgr.service
       sed -i "/^WantedBy=/ s/ui-init.target$/wpeframework.service/" ${D}${systemd_unitdir}/system/ermgr.service
   fi
   # appsservice expects ERM UDS in /tmp folder
   sed -i "/^Environment=\"XDG_RUNTIME_DIR/ s/run\"$/tmp\"/" ${D}${systemd_unitdir}/system/ermgr.service
   sed -i -e '/Environment=.*/aEnvironment=\"ESSRMGR_DEBUG=7\"' ${D}${systemd_unitdir}/system/ermgr.service
}

SYSTEMD_SERVICE:${PN} += "ermgr.service"
FILES:${PN} += "${systemd_unitdir}/system/*.service"
FILES:${PN} += "/usr/bin/ermgr"
