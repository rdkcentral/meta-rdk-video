DESCRIPTION = "Apparmor generic profiles RDK"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://rdk-apparmor-profiles/LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

PV = "1.3.0"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit pkgconfig autotools systemd
SRCREV_rdk-apparmor-profiles = "459fec5ca9aa6706a6357afffa0bf3763b502b4c"
SRC_URI = "${CMF_GITHUB_ROOT}/rdk-apparmor-profiles.git;${CMF_GITHUB_SRC_URI_SUFFIX};destsuffix=git/rdk-apparmor-profiles;name=rdk-apparmor-profiles"

S = "${WORKDIR}/git"
PACKAGE_BEFORE_PN += "${PN}-optimized"
do_compile[noexec] = "1"
do_install() {
    install -d ${D}${systemd_system_unitdir}
    # Our startup/init script

    install -d ${D}/etc/apparmor/

    install -m 0755 ${S}/rdk-apparmor-profiles/apparmor_parse.sh ${D}${sysconfdir}/apparmor/apparmor_parse.sh
    install -m 0644 ${S}/rdk-apparmor-profiles/apparmor.service ${D}${systemd_system_unitdir}/apparmor.service

    for i in ${S}/rdk-apparmor-profiles/generic_profiles/*; do
        install -m 0644 "$i" ${D}${sysconfdir}/apparmor.d/
    done
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
