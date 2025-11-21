
SUMMARY = "Firebolt user interface"
SECTION = "rdk/samples"
LICENSE = " Apache-2.0 & MIT & OFL-1.1 & CC0-1.0 & BitstreamVera"
LIC_FILES_CHKSUM = "file://${S}/../../LICENSE;md5=fac1f1de1b2231cdc801d64ac2607c6b"

SRC_URI = "${CMF_GIT_ROOT}/components/opensource/RDK_apps;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GIT_BRANCH}"
SRCREV = "e43f3cc668c22a2563c3e0d883c2cf4eae5eec37"

S = "${WORKDIR}/git/FireboltUI/dist"

#Ligtening application, no need for configuration/compilation
do_compile[noexec] = "1"
do_configure[noexec] = "1"

RDEPENDS:${PN}-dev = ""

do_install() {
   install -d ${D}/opt/www/fireboltui/
   cp -r ${S}/* ${D}/opt/www/fireboltui/
}


FILES:${PN} += "/opt/www/*"
