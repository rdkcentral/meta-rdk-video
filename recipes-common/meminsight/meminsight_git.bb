SUMMARY = "Memory insight utility and runner service"
SECTION = "console/utils"
DESCRIPTION = "meminsight: system/process memory statistics collection tool with systemd runner service."
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1c020dfe1abb4e684874a44de1244c28"

SRC_URI = "${CMF_GITHUB_ROOT}/${BPN}.git;nobranch=1;protocol=${CMF_GIT_PROTOCOL}"

SRC_URI:append = " file://meminsight-runner.service \
                   file://meminsight-runner.path \
                   file://ntp-monitor.conf \
                   file://conf/client.conf \
                   file://conf/client-path.conf \
                   file://ntp_metrics_poll.c \
                   file://ntp-metrics-collector.service \
                   file://ntp-pcap-collector.service \
                   file://ntp-dns-pcap-collector.sh \
                   file://chronyc_metrics.c \
                   "

SRCREV = "f83f1804827cca0550d525d971f4337998d6ac1d"
PV = "1.0"
S = "${WORKDIR}/git"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit autotools systemd

do_compile:append() {
  #${CC} ${CFLAGS} ${LDFLAGS} ${WORKDIR}/ntp_metrics_poll.c -o ${B}/ntpmetrics_poll
   ${CC} ${CFLAGS} ${LDFLAGS} ${WORKDIR}/chronyc_metrics.c -o ${B}/chronymetrics_poll
}


do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/xmeminsight ${D}${bindir}/xmeminsight
    install -d ${D}${systemd_unitdir}/system
    install -d ${D}${sysconfdir}
    install -d ${D}${base_libdir}/rdk
   # install -m 0755 ${B}/ntpmetrics_poll ${D}${bindir}/ntpmetrics_poll
    install -m 0755 ${B}/chronymetrics_poll ${D}${bindir}/chronymetrics_poll
    install -m 0644 ${WORKDIR}/ntp-monitor.conf ${D}${sysconfdir}
    install -m 0644 ${WORKDIR}/meminsight-runner.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/meminsight-runner.path ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/ntp-metrics-collector.service ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/ntp-pcap-collector.service ${D}${systemd_unitdir}/system/
    install -m 0755 ${WORKDIR}/ntp-dns-pcap-collector.sh ${D}${base_libdir}/rdk
    install -d ${D}${systemd_unitdir}/system/meminsight-runner.service.d
    install -d ${D}${systemd_unitdir}/system/meminsight-runner.path.d
}

do_install:append:client() {
    install -m 0644 ${WORKDIR}/conf/client.conf ${D}${systemd_unitdir}/system/meminsight-runner.service.d/
    install -m 0644 ${WORKDIR}/conf/client-path.conf ${D}${systemd_unitdir}/system/meminsight-runner.path.d/
}


SYSTEMD_SERVICE:${PN} = "meminsight-runner.path"
SYSTEMD_SERVICE:${PN} += "ntp-metrics-collector.service"
SYSTEMD_SERVICE:${PN} += "ntp-pcap-collector.service"

FILES:${PN} += "${bindir}/xmeminsight"
FILES:${PN} += "${systemd_unitdir}/system/meminsight-runner.service"
FILES:${PN} += "${systemd_unitdir}/system/meminsight-runner.path"
FILES:${PN} += "${systemd_unitdir}/system/meminsight-runner.service.d/*.conf"
FILES:${PN} += "${systemd_unitdir}/system/meminsight-runner.path.d/*.conf"
FILES:${PN} += "${sysconfdir}/ntp-monitor.conf"
FILES:${PN} += "${systemd_unitdir}/system/ntp-metrics-collector.service"
FILES:${PN} += "${systemd_unitdir}/system/ntp-pcap-collector.service"
FILES:${PN} += "${base_libdir}/rdk/ntp-dns-pcap-collector.sh"

#FILES:${PN} += "${bindir}/ntpmetrics_poll"
FILES:${PN} += "${bindir}/chronymetrics_poll"

