inherit cmake
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'webkitbrowser-plugin', ' -DCMAKE_WPEWEBKIT_JSBINDINGS=1', '', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'gstreamer1', '', ' -DCMAKE_USE_GST_0.10=1', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', ' -DCMAKE_SYSTEMD_JOURNAL=1', '', d)}"
EXTRA_OECMAKE += " -DCMAKE_AAMP_RFC_REQUIRED=1"

PACKAGECONFIG[opencdm]   = "-DCMAKE_USE_OPENCDM=1,-DCMAKE_USE_OPENCDM=0,wpeframework"
PACKAGECONFIG[playready] = "-DCMAKE_USE_PLAYREADY=1,-DCMAKE_USE_PLAYREADY=0,wpeframework"
PACKAGECONFIG[widevine]  = "-DCMAKE_USE_WIDEVINE=1,-DCMAKE_USE_WIDEVINE=0,wpeframework"
PACKAGECONFIG[clearkey]  = "-DCMAKE_USE_CLEARKEY=1,-DCMAKE_USE_CLEARKEY=0,wpeframework"
PACKAGECONFIG[opencdm_adapter] = "-DCMAKE_USE_THUNDER_OCDM_API_0_2=1 -DCMAKE_CDM_DRM=1 -DCMAKE_USE_OPENCDM_ADAPTER=1,-DCMAKE_USE_OPENCDM_ADAPTER=0,wpeframework"

inherit pkgconfig

DEPENDS += "iarmmgrs wpeframework"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'gstreamer1', 'gstreamer1.0  gstreamer1.0-plugins-base', 'gstreamer gst-plugins-base', d)}"
DEPENDS += " wpeframework-clientlibraries"
DEPENDS:append = " virtual/vendor-gst-drm-plugins essos "
RDEPENDS:${PN} += "devicesettings"
PACKAGECONFIG:append = " playready widevine clearkey"

DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec', '-DCMAKE_GST_SUBTEC_ENABLED=1 ', '', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'wpe_security_util_disable', ' -DDISABLE_SECURITY_TOKEN=ON ', '', d)}"

EXTRA_OECMAKE += "  -DCMAKE_WPEFRAMEWORK_REQUIRED=1 "

EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'sec_manager', ' -DCMAKE_USE_SECMANAGER=1 ', '', d)}"

RDEPENDS:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec', 'packagegroup-subttxrend-app', '', d)}"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec', 'closedcaption-hal-headers virtual/vendor-dvb virtual/vendor-closedcaption-hal', '', d)}"

#Ethan log is implemented by Dobby hence enabling it.
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_rialto', 'dobby', '', d)}"

CXXFLAGS += "-DCMAKE_LIGHTTPD_AUTHSERVICE_DISABLE=1 -I${STAGING_DIR_TARGET}${includedir}/WPEFramework/ "

CXXFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'wpe_security_util_disable', '', ' -lWPEFrameworkSecurityUtil ', d)}"
EXTRA_OECMAKE += " -DCMAKE_LIGHTTPD_AUTHSERVICE_DISABLE=1 "



RDEPENDS_${PN} +=  "${@bb.utils.contains('DISTRO_FEATURES', 'rdk_svp', 'gst-svp-ext', '', d)}"
DEPENDS += " wpe-webkit wpeframework-clientlibraries virtual/vendor-gst-drm-plugins essos virtual/vendor-secapi2-adapter"

