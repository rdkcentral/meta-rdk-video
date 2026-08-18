SUMMARY = "ENTServices powermanager plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=be650d9617f9f9d24bcaccf78a97b28b"

PV = "1.4.7"
PV:vdevice_x86-64-mw = "1.4.7.1"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-powermanager;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://rdkservices.ini \
          "

# Release version - 1.4.7
SRCREV = "924d72b6974967859ffa44d66eb9860a2aa08541"
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
    -DPOWERMANAGER_AIDL_HELPER_ARCHIVE=${B}/libdeepsleep_aidl_helpers.a \
    -DAIDL_DEEPSLEEP_INCLUDE_DIR=${WORKDIR}/aidl-headers \
    -DAIDL_BOOT_INCLUDE_DIR=${WORKDIR}/aidl-headers \
"

DEPENDS += "power-manager-headers wpeframework wpeframework-tools-native"
DEPENDS:append:vdevice_x86-64-mw = " deepsleep-vendor libbinder"

# boot-vendor must finish its AIDL generation before configure, but adding it to
# DEPENDS causes its sysroot payload to collide with headers already staged by
# other providers. Keep it as a task dependency only so the generated files are
# available under TMPDIR/work without extending this recipe's sysroot from it.
do_configure:vdevice_x86-64-mw[depends] += " boot-vendor:do_populate_sysroot"

CXXFLAGS:append:vdevice_x86-64-mw = " -I${WORKDIR}/aidl-headers -I${STAGING_INCDIR}/rdk/halif/power-manager -I${STAGING_INCDIR}/rdk/halif/deepsleep-manager -I${STAGING_INCDIR}/binder -I${STAGING_INCDIR}/android -Wno-error=unknown-pragmas -Wno-error=format"
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
POWERMANAGER_DEPS:vdevice_x86-64-mw = "iarmbus vdevice-noop virtual/mfrlib entservices-apis entservices-helpers"

POWERMANAGER_RDEPS = "virtual/mfrlib entservices-apis entservices-helpers"
POWERMANAGER_RDEPS:vdevice_x86-64-mw = "virtual/mfrlib entservices-apis entservices-helpers"

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
    AIDL_CPP_DIR=$(find ${TMPDIR}/work \
        -path "*/deepsleep-vendor/*/rdk-halif-aidl-build/current/cpp/com/rdk/hal" \
        ! -path "*/package/*" \
        ! -path "*/packages-split/*" \
        ! -path "*/image/*" \
        -type d 2>/dev/null | head -n 1)
    if [ -z "${AIDL_CPP_DIR}" ]; then
        bbfatal "Unable to locate generated AIDL C++ sources for deepsleep-vendor under ${TMPDIR}/work"
    fi

    AIDL_CUR_DIR=$(dirname "$(dirname "$(dirname "$(dirname "${AIDL_CPP_DIR}")")")")
    AIDL_HDR_DIR="${AIDL_CUR_DIR}/h"
    AIDL_HELPER_DIR="${WORKDIR}/aidl-headers/com/rdk/hal/deepsleep"
    if [ ! -d "${AIDL_HDR_DIR}/com" ]; then
        bbfatal "Unable to locate generated AIDL headers for deepsleep-vendor under ${AIDL_HDR_DIR}"
    fi

    rm -rf "${WORKDIR}/aidl-headers"
    install -d "${AIDL_HELPER_DIR}"
    cp -r "${AIDL_HDR_DIR}/com" "${WORKDIR}/aidl-headers/"

    for f in IDeepSleep Capabilities KeyCode WakeUpTrigger; do
        if [ ! -f "${AIDL_CPP_DIR}/deepsleep/${f}.cpp" ]; then
            bbfatal "Missing generated AIDL source ${AIDL_CPP_DIR}/deepsleep/${f}.cpp"
        fi
        cp "${AIDL_CPP_DIR}/deepsleep/${f}.cpp" "${AIDL_HELPER_DIR}/"
    done

    BOOT_CPP_DIR=$(find ${TMPDIR}/work \
        -path "*/boot-vendor/*/rdk-halif-aidl-build/current/cpp/com/rdk/hal" \
        ! -path "*/package/*" \
        ! -path "*/packages-split/*" \
        ! -path "*/image/*" \
        -type d 2>/dev/null | head -n 1)
    if [ -z "${BOOT_CPP_DIR}" ]; then
        bbfatal "Unable to locate generated AIDL C++ sources for boot-vendor under ${TMPDIR}/work"
    fi

    BOOT_CUR_DIR=$(dirname "$(dirname "$(dirname "$(dirname "${BOOT_CPP_DIR}")")")")
    BOOT_HDR_DIR="${BOOT_CUR_DIR}/h"
    BOOT_HELPER_DIR="${WORKDIR}/aidl-headers/com/rdk/hal/boot"
    if [ ! -d "${BOOT_HDR_DIR}/com" ]; then
        bbfatal "Unable to locate generated AIDL headers for boot-vendor under ${BOOT_HDR_DIR}"
    fi

    install -d "${BOOT_HELPER_DIR}"
    cp -r "${BOOT_HDR_DIR}/com" "${WORKDIR}/aidl-headers/"

    for f in BootReason Capabilities IBoot PowerSource ResetType; do
        if [ ! -f "${BOOT_CPP_DIR}/boot/${f}.cpp" ]; then
            bbfatal "Missing generated AIDL source ${BOOT_CPP_DIR}/boot/${f}.cpp"
        fi
        cp "${BOOT_CPP_DIR}/boot/${f}.cpp" "${BOOT_HELPER_DIR}/"
    done
}

do_compile:prepend:vdevice_x86-64-mw() {
    OBJ_DIR="${B}/aidl_helpers"
    mkdir -p "${OBJ_DIR}"

    AIDL_CPP_DIR=$(find ${TMPDIR}/work \
        -path "*/deepsleep-vendor/*/rdk-halif-aidl-build/current/cpp/com/rdk/hal" \
        ! -path "*/package/*" \
        ! -path "*/packages-split/*" \
        ! -path "*/image/*" \
        -type d 2>/dev/null | head -n 1)
    if [ -z "${AIDL_CPP_DIR}" ]; then
        bbfatal "Unable to locate generated AIDL C++ sources for deepsleep-vendor under ${TMPDIR}/work"
    fi

    AIDL_HDR_DIR="${WORKDIR}/aidl-headers"
    if [ ! -d "${AIDL_HDR_DIR}/com" ]; then
        bbfatal "Unable to locate staged AIDL headers for deepsleep-vendor under ${AIDL_HDR_DIR}"
    fi

    HELPER_DIR="${AIDL_CPP_DIR}/deepsleep"
    INCFLAGS="-I${AIDL_HDR_DIR} -I${STAGING_INCDIR} -I${STAGING_INCDIR}/binder -I${STAGING_INCDIR}/android -std=gnu++17 -Wno-error=unknown-pragmas"

    for f in IDeepSleep Capabilities KeyCode WakeUpTrigger; do
        ${CXX} ${CXXFLAGS} ${INCFLAGS} -fPIC \
            -c "${HELPER_DIR}/${f}.cpp" -o "${OBJ_DIR}/${f}.o"
    done

    ${AR} rcs "${B}/libdeepsleep_aidl_helpers.a" \
        "${OBJ_DIR}/IDeepSleep.o" \
        "${OBJ_DIR}/Capabilities.o" \
        "${OBJ_DIR}/KeyCode.o" \
        "${OBJ_DIR}/WakeUpTrigger.o"
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
