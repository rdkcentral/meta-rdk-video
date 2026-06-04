FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

EXTRA_OECONF:append = "${@bb.utils.contains('DISTRO_FEATURES', 'tee_enabled', ' --enable-tee ', '', d)}"

