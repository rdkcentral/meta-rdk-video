EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'webkitbrowser-plugin', ' -DCMAKE_WPEWEBKIT_JSBINDINGS=1', '', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', ' -DCMAKE_SYSTEMD_JOURNAL=1', '', d)}"
EXTRA_OECMAKE += " -DCMAKE_BUILD_TYPE=Debug"
EXTRA_OECMAKE += " -DCMAKE_INBUILT_AAMP_DEPENDENCIES=1"

EXTRA_OECMAKE:remove_rpi = "${@bb.utils.contains('DISTRO_FEATURES', 'webkit', '-DCMAKE_QT5WEBKIT_JSBINDINGS=1', '', d)}"

