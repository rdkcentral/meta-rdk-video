SUMMARY = "ENTServices AVOutput plugin"
LICENSE = "CLOSED"

PV = "2.1.3"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-avoutput;${CMF_GITHUB_SRC_URI_SUFFIX}"

# Release version - 2.1.3
SRCREV = "ff0e7728c56444481e3032a98b98e092ab766c86"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV', "tvsettings-hal-headers ", "", d)}"
DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV', "virtual/vendor-tvsettings-hal ", "", d)}"
# DS_COMRPC path: 'devicesettings' (libds.so + dsMgr) removed from DEPENDS/RDEPENDS.
# 'devicesettings-hal-headers' kept as build-time-only dep: provides dsError.h,
# dsTypes.h etc. for shared headers (AVOutputBase.h, AVOutputSTB.h) — no runtime
# libds.so or dsMgr dependency. Rollback: restore 'devicesettings' to both lines.
DEPENDS += "wpeframework wpeframework-tools-native entservices-apis boost devicesettings-hal-headers entservices-helpers"
RDEPENDS:${PN} += "wpeframework"

TARGET_LDFLAGS += " -Wl,--no-as-needed -ltelemetry_msgsender -Wl,--as-needed "

CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/wdmp-c/ "
# ds-hal include path: needed by shared headers (AVOutputBase.h -> dsMgr.h -> dsTypes.h,
# AVOutputSTB.h -> dsError.h). devicesettings-hal-headers provides the files; this -I
# exposes them as bare includes. No libds.so or dsMgr runtime dependency.
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/rdk/halif/ds-hal/ "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/trower-base64/ "
CXXFLAGS += " -DRFC_ENABLED "
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV', ' -DDEVICE_TYPE=AVOutputTV', '', d)}"
# enable filtering for undefined interfaces and link local ip address notifications
CXXFLAGS += " -DNET_DEFINED_INTERFACES_ONLY -DNET_NO_LINK_LOCAL_ANNOUNCE "
CXXFLAGS += " -Wall -Werror "
CXXFLAGS:remove_morty = " -Wall -Werror "
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"

PACKAGECONFIG ?= " breakpadsupport \
    telemetrysupport \
"

PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV', ' avoutput', '', d)}"

PACKAGECONFIG[breakpadsupport]      = ",,breakpad-wrapper,breakpad-wrapper"
PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"
PACKAGECONFIG[avoutput]             = "-DPLUGIN_AVOUTPUT=ON -DAVOUTPUT_TV=true,,entservices-helpers,entservices-helpers"
EXTRA_OECMAKE += " -DDS_COMRPC=ON"
EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DSECAPI_LIB=sec_api \
"


FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/*.so ${datadir}/WPEFramework/*"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
