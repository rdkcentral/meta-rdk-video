FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

EXTRA_OECONF:append = "${@bb.utils.contains('DISTRO_FEATURES', 'tee_enabled', ' --enable-tee ', '', d)}"

SRC_URI:append:ntp-dtt-rdkdefault = " file://systimemgr_ntp-dtt-rdkdefault.conf "

CXXFLAGS += " -Wall -Werror"

do_install:append:ntp-dtt-rdkdefault() {
   install -d ${D}${sysconfdir}
   install ${WORKDIR}/systimemgr_ntp-dtt-rdkdefault.conf ${D}${sysconfdir}/systimemgr.conf
}

