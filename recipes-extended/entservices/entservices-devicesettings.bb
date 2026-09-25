SUMMARY = "ENTServices devicesettings plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "1.0.0"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-devicesettings;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://rdkservices.ini \
          "

# Release version - 1.0.0
SRCREV = "66032d32aed342f36b8cb1ab405b566cddf9c998"
SRCREV:vdevice_x86-64-mw = "d4d5f4adfd134deb4a9cfcae68b21e1be7eea2e4"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

DEPENDS += "wpeframework wpeframework-tools-native entservices-apis"
RDEPENDS:${PN} += "wpeframework"

TARGET_LDFLAGS += " -Wl,--no-as-needed -ltelemetry_msgsender -Wl,--as-needed "

CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/wdmp-c/ "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/trower-base64/ "
CXXFLAGS += " -DRFC_ENABLED "
# enable filtering for undefined interfaces and link local ip address notifications
CXXFLAGS += " -DNET_DEFINED_INTERFACES_ONLY -DNET_NO_LINK_LOCAL_ANNOUNCE "
CXXFLAGS += " -Wall -Werror "
CXXFLAGS:remove_morty = " -Wall -Werror "
#CXXFLAGS += "-DRDK_DSHAL_NAME="\""libds-hal.so.0\""""
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"

PACKAGECONFIG ?= " breakpadsupport \
    telemetrysupport \
    devicesettings \
"

PACKAGECONFIG[breakpadsupport]      = ",,breakpad-wrapper,breakpad-wrapper"
PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"

DEVICESETTINGS_DEPS = "iarmbus iarmmgrs devicesettings virtual/vendor-devicesettings-hal entservices-helpers"
DEVICESETTINGS_DEPS:vdevice_x86-64-mw = "iarmbus entservices-helpers rdk-halif-aidl-mw libbinderrdk vdevice-noop"

DEVICESETTINGS_RDEPS = "iarmbus devicesettings entservices-helpers"
DEVICESETTINGS_RDEPS:vdevice_x86-64-mw = "iarmbus entservices-helpers libbinderrdk rdk-halif-aidl-mw-hdmiinput rdk-halif-aidl-mw-common vdevice-noop"

PACKAGECONFIG[devicesettings]       = "-DPLUGIN_DEVICESETTINGS=ON,-DPLUGIN_DEVICESETTINGS=OFF,${DEVICESETTINGS_DEPS},${DEVICESETTINGS_RDEPS}"

# Pass component-specific HDMIInput AIDL/binder paths to CMake for vdevice.
EXTRA_OECMAKE:append:vdevice_x86-64-mw = " \
    -DAIDL_INCLUDE_DIR=${STAGING_INCDIR}/mw/hdmiinput/0.1.0.0/include \
    -DBINDER_INCLUDE_DIR=${STAGING_INCDIR}/android \
    -DHAL_AIDL_LIBRARY=${STAGING_LIBDIR}/mw/rdk-halif-aidl/libhdmiinput-v0.1.0.0-cpp.so \
    -DBINDER_LIBRARY=${STAGING_DIR_HOST}${prefix}/mw/lib/binder/libbinder.so \
    -DUTILS_LIBRARY=${STAGING_DIR_HOST}${prefix}/mw/lib/binder/libutils.so \
    -DIARMBUS_INCLUDE_DIRS:PATH=${RECIPE_SYSROOT}${includedir}/rdk/iarmbus \
    -DIARMRECEIVER_INCLUDE_DIRS:PATH= \
    -DDSHAL_INCLUDE_DIRS:PATH=${STAGING_INCDIR}/rdk/halif/ds-hal \
    -DOEMHAL_LIBRARIES:FILEPATH=${STAGING_LIBDIR}/libds-hal.so \
"

CXXFLAGS:append:vdevice_x86-64-mw = " \
    -I${STAGING_INCDIR}/mw/hdmiinput/0.1.0.0/include \
    -I${STAGING_INCDIR}/mw/common/0.2.0.0/include \
    -I${STAGING_INCDIR}/mw/include \
    -I${STAGING_INCDIR}/android \
    -Wno-error=attributes \
    -Wno-error=unknown-pragmas \
    -Wno-error=write-strings \
"

LDFLAGS:append:vdevice_x86-64-mw = " \
    -L${STAGING_DIR_HOST}${prefix}/mw/lib/binder \
    -L${STAGING_LIBDIR}/mw/rdk-halif-aidl \
"

do_configure:append:vdevice_x86-64-mw() {
    if [ ! -e "${STAGING_INCDIR}/rdk/halif/ds-hal/dsTypes.h" ]; then
        bbfatal "Unable to locate dsTypes.h from vdevice-noop under ${STAGING_INCDIR}/rdk/halif/ds-hal"
    fi
    if [ ! -d "${STAGING_INCDIR}/mw/hdmiinput/0.1.0.0/include" ]; then
        bbfatal "Unable to locate staged rdk-halif-aidl-mw-hdmiinput headers under ${STAGING_INCDIR}/mw/hdmiinput"
    fi
    if [ ! -e "${STAGING_LIBDIR}/mw/rdk-halif-aidl/libhdmiinput-v0.1.0.0-cpp.so" ]; then
        bbfatal "Unable to locate staged libhdmiinput-v0.1.0.0-cpp.so under ${STAGING_LIBDIR}/mw/rdk-halif-aidl"
    fi
}

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DSECAPI_LIB=sec_api \
"

do_install:append() {
    install -d ${D}${sysconfdir}/rfcdefaults
    if ${@bb.utils.contains_any("DISTRO_FEATURES", "rdkshell_ra second_form_factor", "true", "false", d)}
    then
      install -m 0644 ${WORKDIR}/rdkservices.ini ${D}${sysconfdir}/rfcdefaults/
    fi

    if ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'true', 'false', d)} == 'true'; then
        if [ -d "${D}/etc/WPEFramework/plugins" ]; then
            find ${D}/etc/WPEFramework/plugins/ -type f | xargs sed -i -r 's/"autostart"[[:space:]]*:[[:space:]]*true/"autostart":false/g'
        fi
    fi
}

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/*.so ${datadir}/WPEFramework/*"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
