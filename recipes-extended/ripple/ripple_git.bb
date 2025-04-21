SUMMARY = "Firebolt reference implementation for RDK"
HOMEPAGE = "https://github.com/rdkcentral/Ripple"
SECTION = "console/utils"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

require ripple-versions.inc

SRC_URI += "${OPEN_RIPPLE_SRCURI}"
SRCREV_rmain = "${OPEN_RIPPLE_SRCVER}"

SRC_URI += " \
    file://0001-strip-abort-on-panic.patch \
    file://ripple-start.sh \
    file://ripple.service \
    "

SRC_URI += "git://github.com/rdkcentral/firebolt.git;protocol=https;branch=main;subpath=requirements/1.3.0/specifications;destsuffix=firebolt_specs"
SRCREV = "7b01285cd575cff11142e94796d5fb894ee0f441"


SRCREV_FORMAT ="rmain"
PV = "${RIPPLE_VERSION}"

#Working directory for open components
S = "${OPEN_RIPPLE_S}"

#Announce as firebolt provider
PROVIDES = "virtual/firebolt"
RPROVIDES:${PN} = "virtual/firebolt"

#Runtime dependency on wpeframework
RDEPENDS:${PN} = "wpeframework"

RDEPENDS:${PN} += "bash"

#RDK logging support
inherit syslog-ng-config-gen pkgconfig

SYSLOG-NG_FILTER = "ripple"
SYSLOG-NG_SERVICE_ripple = "ripple.service"
# Logs to /opt/logs/ripple.log
SYSLOG-NG_DESTINATION_ripple = "ripple.log"
SYSLOG-NG_LOGRATE_ripple = "high"

CARGO_BUILD_FLAGS += " --features 'sysd'"

#Cargo default to install binaries and libraries. Just install systemd services
do_install:append() {
	install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/ripple.service ${D}${systemd_unitdir}/system/ripple.service
        install -m 0755 ${WORKDIR}/ripple-start.sh ${D}${bindir}
    install -d ${D}${sysconfdir}/ripple/openrpc/
    install -m 0644 ${OPEN_RIPPLE_S}/examples/reference-manifest/IpStb/firebolt-device-manifest.json ${D}${sysconfdir}/firebolt-device-manifest.json
    install -m 0644 ${OPEN_RIPPLE_S}/examples/reference-manifest/IpStb/firebolt-extn-manifest.json ${D}${sysconfdir}/firebolt-extn-manifest.json
    install -m 0644 ${OPEN_RIPPLE_S}/examples/reference-manifest/IpStb/firebolt-app-library.json ${D}${sysconfdir}/firebolt-app-library.json
    # Install firebolt-open-rpc.json from the cloned repo
    install -m 0644 ${WORKDIR}/firebolt_specs/firebolt-open-rpc.json ${D}${sysconfdir}/ripple/openrpc/firebolt-open-rpc.json
    #TODO This should be a packageoption instead.
    rm ${D}${libdir}/rust/liblauncher.so
}

FILES:${PN} += "${bindir}/*"
FILES:${PN} += "${libdir}/*"
FILES:${PN} += "${systemd_unitdir}/system/*"
FILES:${PN} += "${sysconfdir}/*"
SYSTEMD_SERVICE:${PN} = "ripple.service"
INSANE_SKIP:${PN}:append = "already-stripped"
