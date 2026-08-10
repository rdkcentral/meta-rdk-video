SUMMARY = "Thunder Framework User Interface"
DESCRIPTION = "Thunder Framework User Interface web app"
HOMEPAGE = "https://github.com/rdkcentral/ThunderUI"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=66fe57b27abb01505f399ce4405cfea0"

SRC_URI = "git://github.com/rdkcentral/ThunderUI;protocol=https;nobranch=1"

SRCREV ?= "R${PV}"

S = "${WORKDIR}/git"

PRODUCT = "Thunder"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    rm -rf ${D}${datadir}/${PRODUCT}/Controller/UI/*
    mkdir -p ${D}${datadir}/${PRODUCT}/Controller/UI
    cp -r ${S}/src/* ${D}${datadir}/${PRODUCT}/Controller/UI
    cp -r ${S}/dist/* ${D}${datadir}/${PRODUCT}/Controller/UI
}

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

FILES:${PN} += "${datadir}/${PRODUCT}/Controller/UI/*"
