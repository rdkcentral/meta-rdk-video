SUMMARY = "Memory insight utility and runner service"
SECTION = "console/utils"
DESCRIPTION = "meminsight: system/process memory statistics collection tool with systemd runner service."

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=70514b59ff7b36bbbc30d093c6814d8e"

#SRC_URI = "${CMF_GITHUB_ROOT}/meminsight;${CMF_GITHUB_SRC_URI_SUFFIX};name=meminsight"
SRC_URI = "git://github.com/rdkcentral/meminsight.git;branch=sample;protocol=https"
SRC_URI:append = " file://meminsight-runner.service "

SRCREV = "${AUTOREV}"
PV = "1.0"
S = "${WORKDIR}/git"
SRCREV_FORMAT = "meminsight"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit systemd

SYSTEMD_SERVICE:${PN} = "meminsight-runner.service"

do_compile() {
    oe_runmake
}

do_install() {
    # Install the binary
    install -d ${D}${bindir}
    install -m 0755 ${S}/xMemInsight ${D}${bindir}/xMemInsight

    # Install the systemd service
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/meminsight-runner.service ${D}${systemd_unitdir}/system/
}

FILES:${PN} += "${bindir}/xMemInsight"
FILES:${PN} += "${systemd_unitdir}/system/meminsight-runner.service"
