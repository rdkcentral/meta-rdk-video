SUMMARY = "This recipe compiles rmf_mediastreamer code base."
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
PV = "${RDK_RELEASE}"
PR = "r0"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI = "${CMF_GIT_ROOT}/rdk/components/generic/rmf_tools/tenableHDCP;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GIT_BRANCH}"
SRCREV ?= "d44c7438284fc36c774f6fc3fbd322698fe5ee71"

S = "${WORKDIR}/git"

inherit autotools pkgconfig systemd coverity

DEPENDS += "devicesettings iarmbus iarmmgrs"
RDEPENDS:${PN} += " devicesettings"

DEPENDS += " devicesettings-hal-headers "
CXXFLAGS:append = " -I${STAGING_INCDIR}/rdk/halif/ds-hal/ "
CFLAGS:append = " -I${STAGING_INCDIR}/rdk/halif/ds-hal/ "

SRC_URI:append = "${@bb.utils.contains('DISTRO_FEATURES', 'disable_mfr_read_hdcpkey', ' file://0001-tenablehdcp-remove-mfr-dependency.patch file://0002-Fix-for-tenableHDCP-crash-and-added-hdmiservice.patch ', '', d)}"

SYSTEMD_SERVICE:${PN} = "hdcp.service"
FILES:${PN} += "${sysconfdir}/* ${systemd_unitdir}/system/hdcp.service"

do_install:append() {

install -d ${D}${systemd_unitdir}/system ${D}${sysconfdir}
install -m 0644 ${S}/conf/hdcp.service ${D}${systemd_unitdir}/system/hdcp.service

}

