DESCRIPTION = "Apparmor generic profiles RDK"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://rdk-apparmor-profiles/LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

PV = "1.1.0"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit pkgconfig autotools systemd
SRC_URI = "${CMF_GITHUB_ROOT}/rdk-apparmor-profiles.git;${CMF_GITHUB_SRC_URI_SUFFIX};destsuffix=git/rdk-apparmor-profiles;name=rdk-apparmor-profiles"
SRCREV_rdk-apparmor-profiles = "0e9b3f4d100c23a99427891f6dcb417e77fe7b00"

S = "${WORKDIR}/git"
PACKAGE_BEFORE_PN += "${PN}-optimized"
do_compile[noexec] = "1"
do_install() {
    install -d ${D}${systemd_system_unitdir}
    # Our startup/init script

    install -d ${D}/etc/apparmor/

    install -m 0755 ${S}/rdk-apparmor-profiles/apparmor_parse.sh ${D}${sysconfdir}/apparmor/apparmor_parse.sh
    install -m 0644 ${S}/rdk-apparmor-profiles/apparmor.service ${D}${systemd_system_unitdir}/apparmor.service
}
SYSTEMD_SERVICE:${PN}:append = " apparmor.service"
FILES:${PN} += "${systemd_system_unitdir}/apparmor.service"
FILES:${PN}-optimized = "${sysconfdir}/apparmor/parser.conf \
                         ${sysconfdir}/apparmor/subdomain.conf \
                         ${sysconfdir}/init.d/apparmor \
                         ${base_libdir}/apparmor/functions \
                         ${base_libdir}/apparmor/rc.apparmor.functions \
                         ${bindir}/aa-enabled \
                         ${bindir}/aa-exec"
