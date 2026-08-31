SUMMARY = "ENTServices AVOutput plugin"
LICENSE = "CLOSED"

PV = "2.1.3"
PR = "r2"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-avoutput;${CMF_GITHUB_SRC_URI_SUFFIX}"

# Release version - 2.1.3
SRCREV = "69cc807fb4579a617eb4a77e59c64c1de3552a2b"

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
# DS_COMRPC: iarmbus include path for libIARM.h in AVOutputBase.h when DS removed
CXXFLAGS:append = " -I${STAGING_INCDIR}/rdk/iarmbus"
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

# DS_COMRPC migration: comment out dsMgr.h in shared headers (DS_IARM/ still has it; DS_COMRPC/ path doesn't need it)
do_configure:prepend() {
    # DS_COMRPC migration: comment out DS C++ API headers not available without devicesettings
    for f in ${S}/AVOutputBase.h ${S}/AVOutputSTB.h; do
        [ -f "$f" ] || continue
        sed -i 's|#include "dsMgr.h"|//#include "dsMgr.h" /* DS_COMRPC: removed with devicesettings */|g' "$f"
        sed -i 's|#include "hdmiIn.hpp"|//#include "hdmiIn.hpp" /* DS_COMRPC: removed with devicesettings */|g' "$f"
        sed -i 's|#include "host.hpp"|//#include "host.hpp" /* DS_COMRPC: removed with devicesettings */|g' "$f"
        sed -i 's|#include "manager.hpp"|//#include "manager.hpp" /* DS_COMRPC: removed with devicesettings */|g' "$f"
        sed -i 's|#include "audioOutputPort.hpp"|//#include "audioOutputPort.hpp" /* DS_COMRPC: removed with devicesettings */|g' "$f"
        sed -i 's|#include "videoOutputPort.hpp"|//#include "videoOutputPort.hpp" /* DS_COMRPC: removed with devicesettings */|g' "$f"
    done
    # DS_COMRPC migration: remove -lds link from CMakeLists.txt (ds library removed)
    # ds) closes the target_link_libraries() call — replace with just ) to keep cmake valid
    sed -i 's/^[[:space:]]*ds)[[:space:]]*$/        )/' ${S}/CMakeLists.txt
}

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
