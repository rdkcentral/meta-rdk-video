SUMMARY = "WPE Framework User Interface"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=66fe57b27abb01505f399ce4405cfea0"

PR = "r0"
PV = "4.4.1"
SRCREV = "b4f54fd06fc4e6c748f21d3f62d3f8763f58d6d1"

SRC_URI = "git://github.com/rdkcentral/ThunderUI.git;protocol=https;branch=R4_4"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

S = "${WORKDIR}/git"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
	rm -rf ${D}${datadir}/WPEFramework/Controller/UI/*
	mkdir -p ${D}${datadir}/WPEFramework/Controller/UI
	cp -r ${S}/src/* ${D}${datadir}/WPEFramework/Controller/UI
	cp -r ${S}/dist/* ${D}${datadir}/WPEFramework/Controller/UI
}

FILES:${PN} += "${datadir}/WPEFramework/Controller/UI/*"
