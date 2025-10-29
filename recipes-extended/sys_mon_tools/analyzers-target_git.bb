SUMMARY = "Sys mon tool key simulator recipe"

DESCRIPTION = "Sys mon tool key simulator recipe"

SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "${RDK_RELEASE}+git${SRCPV}"

SRCREV = "5d7e810bcaf8411acb3a084926ae9887ed969b03"
SRC_URI = "${CMF_GIT_ROOT}/rdk/components/generic/sys_mon_tools/analyzers/scripts/target;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GIT_BRANCH}"
S = "${WORKDIR}/git"

do_compile[noexec] = "1"

#Changed from datadir to bindir as per FHS standard
do_install() {
	install -d ${D}${bindir}
	install -m 0755 ${S}/*.sh ${D}${bindir}
}

FILES:${PN} += "${bindir}/*"
