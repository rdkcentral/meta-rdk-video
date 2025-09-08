SUMMARY = "Memory insight utility and runner service"
SECTION = "console/utils"
DESCRIPTION = "meminsight: system/process memory statistics collection tool with systemd runner service."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1c020dfe1abb4e684874a44de1244c28"

SRC_URI = "${CMF_GITHUB_ROOT}/${BPN}.git;nobranch=1;protocol=${CMF_GIT_PROTOCOL}"

SRC_URI:append = " file://meminsight-runner.service \
                   file://meminsight-runner.path \
                   file://conf/client.conf \
                   file://conf/broadband.conf \
                   file://conf/client-path.conf \
                   file://conf/broadband-path.conf \
                   "

SRCREV = "f83f1804827cca0550d525d971f4337998d6ac1d"
PV = "1.0"
S = "${WORKDIR}/git"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit autotools systemd

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/xmeminsight ${D}${bindir}/xmeminsight
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/meminsight-runner.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/meminsight-runner.path ${D}${systemd_unitdir}/system/
    install -d ${D}${systemd_unitdir}/system/meminsight-runner.service.d
    install -d ${D}${systemd_unitdir}/system/meminsight-runner.path.d
}

do_install_append:client() {
    install -m 0644 ${WORKDIR}/conf/client.conf ${D}${systemd_unitdir}/system/meminsight-runner.service.d/
    install -m 0644 ${WORKDIR}/conf/client-path.conf ${D}${systemd_unitdir}/system/meminsight-runner.path.d/
}


SYSTEMD_SERVICE_${PN} = "meminsight-runner.path"

FILES_${PN} += "${bindir}/xmeminsight"
FILES_${PN} += "${systemd_unitdir}/system/meminsight-runner.service"
FILES_${PN} += "${systemd_unitdir}/system/meminsight-runner.path"
FILES_${PN} += "${systemd_unitdir}/system/meminsight-runner.service.d/*.conf"
FILES_${PN} += "${systemd_unitdir}/system/meminsight-runner.path.d/*.conf"

