inherit cmake
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'webkitbrowser-plugin', ' -DCMAKE_WPEWEBKIT_JSBINDINGS=1', '', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'gstreamer1', '', ' -DCMAKE_USE_GST_0.10=1', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', ' -DCMAKE_SYSTEMD_JOURNAL=1', '', d)}"
EXTRA_OECMAKE += " -DCMAKE_BUILD_TYPE=Debug"
EXTRA_OECMAKE += " -DCMAKE_AAMP_RFC_REQUIRED=1"
EXTRA_OECMAKE += " -DCMAKE_INBUILT_AAMP_DEPENDENCIES=1"

EXTRA_OECMAKE:remove_rpi = "${@bb.utils.contains('DISTRO_FEATURES', 'webkit', '-DCMAKE_QT5WEBKIT_JSBINDINGS=1', '', d)}"

PACKAGECONFIG[opencdm]   = "-DCMAKE_USE_OPENCDM=1,-DCMAKE_USE_OPENCDM=0,wpeframework"
PACKAGECONFIG[playready] = "-DCMAKE_USE_PLAYREADY=1,-DCMAKE_USE_PLAYREADY=0,wpeframework"
PACKAGECONFIG[widevine]  = "-DCMAKE_USE_WIDEVINE=1,-DCMAKE_USE_WIDEVINE=0,wpeframework"
PACKAGECONFIG[clearkey]  = "-DCMAKE_USE_CLEARKEY=1,-DCMAKE_USE_CLEARKEY=0,wpeframework"
PACKAGECONFIG[opencdm_adapter] = "-DCMAKE_USE_THUNDER_OCDM_API_0_2=1 -DCMAKE_CDM_DRM=1 -DCMAKE_USE_OPENCDM_ADAPTER=1,-DCMAKE_USE_OPENCDM_ADAPTER=0,wpeframework"
