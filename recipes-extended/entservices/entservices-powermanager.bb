SUMMARY = "ENTServices powermanager plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=be650d9617f9f9d24bcaccf78a97b28b"

PV = "1.4.8"
PV:vdevice_x86-64-mw = "1.4.7.1"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-powermanager;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://rdkservices.ini \
          "

# Release version - 1.4.8
SRCREV = "d0e98b3eec72b635203149cb293e269dbca91bdf"
SRCREV:vdevice_x86-64-mw = "c7519329de6b1af6ac9e8a64694ffc64bf8830c3"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

EXTRA_OECMAKE += " -DENABLE_RFC_MANAGER=ON"
EXTRA_OECMAKE += " -DBUILD_ENABLE_THERMAL_PROTECTION=ON "
EXTRA_OECMAKE:append:vdevice_x86-64-mw = " \
    -DENABLE_POWERMANAGER_AIDL=ON \
    -DPOWERMANAGER_AIDL_STAGING_INCLUDE_DIR=${STAGING_INCDIR} \
    -DPOWERMANAGER_AIDL_HELPER_ARCHIVE:STRING='${STAGING_LIBDIR}/mw/rdk-halif-aidl/libdeepsleep-v0.1.0.0-cpp.so;${STAGING_LIBDIR}/mw/rdk-halif-aidl/libbootreason-v0.1.0.0-cpp.so' \
    -DAIDL_DEEPSLEEP_INCLUDE_DIR=${STAGING_INCDIR}/mw/deepsleep/0.1.0.0/include \
    -DAIDL_BOOT_INCLUDE_DIR=${STAGING_INCDIR}/mw/bootreason/0.1.0.0/include \
    -DBINDER_INCLUDE_DIR=${STAGING_INCDIR}/android \
    -DBINDER_LIBRARY=${STAGING_DIR_HOST}${prefix}/mw/lib/binder/libbinder.so \
    -DUTILS_LIBRARY=${STAGING_DIR_HOST}${prefix}/mw/lib/binder/libutils.so \
"

DEPENDS += "power-manager-headers wpeframework wpeframework-tools-native"
DEPENDS:append:vdevice_x86-64-mw = " deepsleep-manager-headers"

CXXFLAGS:append:vdevice_x86-64-mw = " \
    -I${STAGING_INCDIR}/mw/deepsleep/0.1.0.0/include \
    -I${STAGING_INCDIR}/mw/bootreason/0.1.0.0/include \
    -I${STAGING_INCDIR}/mw/common/0.2.0.0/include \
    -I${STAGING_INCDIR}/mw/include \
    -I${STAGING_INCDIR}/rdk/halif/power-manager \
    -I${STAGING_INCDIR}/rdk/halif/deepsleep-manager \
    -I${STAGING_INCDIR}/android \
    -Wno-error=attributes \
    -Wno-error=class-memaccess \
    -Wno-error=format \
    -Wno-error=unknown-pragmas \
    -Wno-error=write-strings \
"
LDFLAGS:append:vdevice_x86-64-mw = " \
    -L${STAGING_DIR_HOST}${prefix}/mw/lib/binder \
    -L${STAGING_LIBDIR}/mw/rdk-halif-aidl \
"
RDEPENDS:${PN} += "wpeframework"

TARGET_LDFLAGS += " -Wl,--no-as-needed -ltelemetry_msgsender -Wl,--as-needed "

CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/wdmp-c/ "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/trower-base64/ "
CXXFLAGS += " -DRFC_ENABLED "
# enable filtering for undefined interfaces and link local ip address notifications
CXXFLAGS += " -DNET_DEFINED_INTERFACES_ONLY -DNET_NO_LINK_LOCAL_ANNOUNCE "
CXXFLAGS += " -Wall -Werror "
CXXFLAGS:remove_morty = " -Wall -Werror "
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"

INCLUDE_DIRS = " \
    -I=${includedir}/rdk/halif/power-manager \
    -I=${includedir}/WPEFramework/powercontroller \
    "

CXXFLAGS += " -DPLATCO_BOOTTO_STANDBY"
CXXFLAGS += " -DOFFLINE_MAINT_REBOOT"

CFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_STB', ' -DMFR_TEMP_CLOCK_READ ', '', d)} "
CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_STB', ' -DMFR_TEMP_CLOCK_READ ', '', d)} "

# ----------------------------------------------------------------------------

PACKAGECONFIG ?= " breakpadsupport \
    telemetrysupport \
    powermanager \
"

POWERMANAGER_DEPS = "iarmbus iarmmgrs virtual/vendor-deepsleepmgr-hal virtual/vendor-pwrmgr-hal virtual/mfrlib entservices-apis entservices-helpers"
POWERMANAGER_DEPS:vdevice_x86-64-mw = "iarmbus vdevice-noop virtual/mfrlib entservices-apis entservices-helpers rdk-halif-aidl-mw libbinderrdk"

POWERMANAGER_RDEPS = "virtual/mfrlib entservices-apis entservices-helpers"
POWERMANAGER_RDEPS:vdevice_x86-64-mw = "virtual/mfrlib entservices-apis entservices-helpers libbinderrdk rdk-halif-aidl-mw-deepsleep rdk-halif-aidl-mw-bootreason rdk-halif-aidl-mw-common"

PACKAGECONFIG[breakpadsupport]      = ",,breakpad-wrapper,breakpad-wrapper"
PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"
PACKAGECONFIG[powermanager]         = "-DPLUGIN_POWERMANAGER=ON,-DPLUGIN_POWERMANAGER=OFF,${POWERMANAGER_DEPS},${POWERMANAGER_RDEPS}"

# ----------------------------------------------------------------------------

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DSECAPI_LIB=sec_api \
"

# Check if DisplayInfo backend is defined.
python () {
    machine_name = d.getVar('MACHINE')
    if 'raspberrypi4' in machine_name:
        d.appendVar('EXTRA_OECMAKE', ' -DBUILD_RPI=ON')
}

do_configure:prepend:vdevice_x86-64-mw() {
    for source in \
        ${S}/plugin/hal/PowerManagerFactory.cpp \
        ${S}/plugin/hal/PowerAidlImpl.h; do
        sed -i \
            -e 's|com/rdk/hal/boot/IBoot.h|com/rdk/hal/bootreason/IBootReason.h|g' \
            -e 's|com::rdk::hal::boot\>|com::rdk::hal::bootreason|g' \
            -e 's|\<IBoot\>|IBootReason|g' \
            -e 's|\<BootReason\>|BootCause|g' \
            -e 's|getBootReason|getBootCause|g' \
            -e 's|stringToBootReason|stringToBootCause|g' \
            "${source}"
    done
}

do_configure:append:vdevice_x86-64-mw() {
    if [ ! -e "${STAGING_INCDIR}/rdk/halif/deepsleep-manager/deepSleepMgr.h" ]; then
        bbfatal "Unable to locate staged deepSleepMgr.h from deepsleep-manager-headers"
    fi

    for header in \
        deepsleep/0.1.0.0/include/com/rdk/hal/deepsleep/IDeepSleep.h \
        bootreason/0.1.0.0/include/com/rdk/hal/bootreason/IBootReason.h \
        common/0.2.0.0/include/com/rdk/hal/PropertyValue.h; do
        if [ ! -e "${STAGING_INCDIR}/mw/${header}" ]; then
            bbfatal "Unable to locate staged AIDL header ${STAGING_INCDIR}/mw/${header}"
        fi
    done

    for library in libdeepsleep-v0.1.0.0-cpp.so libbootreason-v0.1.0.0-cpp.so; do
        if [ ! -e "${STAGING_LIBDIR}/mw/rdk-halif-aidl/${library}" ]; then
            bbfatal "Unable to locate staged ${library} under ${STAGING_LIBDIR}/mw/rdk-halif-aidl"
        fi
    done
}

do_install:append() {
    install -d ${D}${sysconfdir}/rfcdefaults
    if ${@bb.utils.contains_any("DISTRO_FEATURES", "rdkshell_ra second_form_factor", "true", "false", d)}
    then
      install -m 0644 ${WORKDIR}/rdkservices.ini ${D}${sysconfdir}/rfcdefaults/
    fi

    if ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'true', 'false', d)} == 'true'; then
        if [ -d "${D}/etc/WPEFramework/plugins" ]; then
            find ${D}/etc/WPEFramework/plugins/ -type f ! -name "PowerManager.json" | xargs -r sed -i -r 's/"autostart"[[:space:]]*:[[:space:]]*true/"autostart":false/g'
        fi
    fi
}

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/*.so ${datadir}/WPEFramework/*"

INSANE_SKIP:${PN} += "libdir staticdev dev-so dev-deps"
INSANE_SKIP:${PN}-dbg += "libdir"
