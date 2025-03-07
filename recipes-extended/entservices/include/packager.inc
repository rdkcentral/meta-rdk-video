#SRC_URI += "${@bb.utils.contains('DISTRO_FEATURES', 'rdm-packager', 'file://0001-Packager-support-for-rdm.patch', '', d)}"

#PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'rdm-packager', 'packager', '', d)}"

#PACKAGECONFIG[packager] = "-DPLUGIN_PACKAGER=ON, -DPLUGIN_PACKAGER=OFF, opkg, libopkg"
