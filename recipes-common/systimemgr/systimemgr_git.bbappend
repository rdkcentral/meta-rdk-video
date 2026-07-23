FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

EXTRA_OECONF:append = "${@bb.utils.contains('DISTRO_FEATURES', 'tee_enabled', ' --enable-tee ', '', d)}"

SRC_URI:append:ntp-dtt-drm-tee  = " file://systimemgr_ntp-dtt-drm-tee.conf "

SRC_URI:append:ntp-dtt-rdkdefault = " file://systimemgr_ntp-dtt-rdkdefault.conf "

CXXFLAGS += " -Wall -Werror"

do_install:append:ntp-dtt-rdkdefault() {
   install -d ${D}${sysconfdir}
   install ${WORKDIR}/systimemgr_ntp-dtt-rdkdefault.conf ${D}${sysconfdir}/systimemgr.conf
}

do_install:append:ntp-dtt-drm-tee() {
   install -d ${D}${sysconfdir}
   install ${WORKDIR}/systimemgr_ntp-dtt-drm-tee.conf ${D}${sysconfdir}/systimemgr.conf
   rm -f ${D}${systemd_unitdir}/system/systimemgr.service.d/secure.conf
   find ${D}${systemd_unitdir}/system/systimemgr.service.d -type d -empty -delete
}
