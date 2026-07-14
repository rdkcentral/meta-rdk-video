SUMMARY = "This recipe compiles and installs hdmicec component."
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "1.0.11"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRCREV_hdmicec = "e36b4909b25dda88eef12091c03d8c54b5b5fd8b"
SRCREV_hdmicec:vdevice_x86-64-mw = "82f231cd32434963f635aa2ffeabfedda6d0341f"
SRC_URI = "${CMF_GITHUB_ROOT}/hdmicec;${CMF_GITHUB_SRC_URI_SUFFIX};name=hdmicec"
SRCREV_FORMAT = "hdmicec"

DEPENDS = "glib-2.0 dbus iarmbus devicesettings devicesettings-hal-headers hdmicecheader virtual/vendor-hdmicec-hal iarmmgrs-hal-headers telemetry"
DEPENDS:remove:vdevice_x86-64-mw = "devicesettings devicesettings-hal-headers iarmmgrs-hal-headers"

RDEPENDS:${PN} = " devicesettings telemetry"
RDEPENDS:${PN}:remove:vdevice_x86-64-mw = "devicesettings"

DEPENDS += "safec-common-wrapper"
DEPENDS:append = " rdk-halif-aidl"
DEPENDS:append:vdevice_x86-64-mw = " rdk-halif-aidl libbinder"

ASNEEDED = ""
ALLOW_EMPTY:${PN} = "1"

INSANE_SKIP:${PN} += "file-rdeps"
INSANE_SKIP:${PN}:remove:vdevice_x86-64-mw = "file-rdeps"

S = "${WORKDIR}/git"

DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec', " ", d)}"

inherit systemd autotools pkgconfig coverity breakpad-logmapper syslog-ng-config-gen logrotate_config
#SYSLOG-NG_FILTER = "cec"
#SYSLOG-NG_SERVICE_cec = "cecdaemon.service cecdevmgr.service"
#SYSLOG-NG_DESTINATION_cec = "cec_log.txt"
#SYSLOG-NG_LOGRATE_cec = "medium"

LOGROTATE_NAME="cec"
LOGROTATE_LOGNAME_cec="cec_log.txt"
#HDD_ENABLE
LOGROTATE_SIZE_cec="5242880"
LOGROTATE_ROTATION_cec="1"
#HDD_DISABLE
LOGROTATE_SIZE_MEM_cec="128000"
LOGROTATE_ROTATION_MEM_cec="1"

CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec',  ' `pkg-config --cflags libsafec`', '-fPIC', d)}"

CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec',  ' `pkg-config --cflags libsafec`', '-fPIC', d)}"

LDFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --libs libsafec`', '', d)}"
CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"
CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"

CFLAGS:append = " -I${STAGING_INCDIR}/com/rdk/hal/hdmicec -I${STAGING_INCDIR}/binder -I${STAGING_INCDIR}/android"
CXXFLAGS:append = " -I${STAGING_INCDIR}/com/rdk/hal/hdmicec -I${STAGING_INCDIR}/binder -I${STAGING_INCDIR}/android"
CFLAGS:append:vdevice_x86-64-mw = " -I${STAGING_INCDIR}/com/rdk/hal/hdmicec -I${STAGING_INCDIR}/binder -I${STAGING_INCDIR}/android"
CXXFLAGS:append:vdevice_x86-64-mw = " -I${STAGING_INCDIR}/com/rdk/hal/hdmicec -I${STAGING_INCDIR}/binder -I${STAGING_INCDIR}/android"

INCLUDE_DIRS = " \
    -I=${includedir}/rdk/halif/ds-hal \
    "


do_compile:prepend() {
        case ":${OVERRIDES}:" in
                *:vdevice_x86-64-mw:*)
                        return 0
                        ;;
        esac

        OBJ_DIR="${B}/aidl_stubs"
        mkdir -p "${OBJ_DIR}"
        
        STUB_DIR="${STAGING_INCDIR}/com/rdk/hal/hdmicec"
        HAL_DIR="${STAGING_INCDIR}/com/rdk/hal"
        # Base include root containing com/rdk/hal — same as STAGING_INCDIR in the
        # normal case; overridden below when using a fallback location.
        BASE_INCDIR="${STAGING_INCDIR}"

        # Walk four locations for AIDL-generated cpp stubs in priority order:
        #  1. recipe sysroot       (normal fully-assembled build)
        #  2. sysroots-components  (stale sysroot on developer machine)
        #  3. rdk-halif-aidl image/ (fresh build, do_populate_sysroot pending)
        #  4. rdk-halif-aidl cmake out/ (cmake output before do_install runs)
        if [ ! -f "${STUB_DIR}/IHdmiCec.cpp" ]; then
            _sc=$(find "${TMPDIR}/sysroots-components" \
                    -path "*rdk-halif-aidl/usr/include/com/rdk/hal/hdmicec" \
                    -type d 2>/dev/null | head -1)
            if [ -z "${_sc}" ]; then
                _sc=$(find "${TMPDIR}/work" \
                        -path "*rdk-halif-aidl/*/image/usr/include/com/rdk/hal/hdmicec" \
                        -type d 2>/dev/null | head -1)
            fi
            if [ -z "${_sc}" ]; then
                _sc=$(find "${TMPDIR}/work" \
                        -path "*rdk-halif-aidl*/rdk-halif-aidl/out/hdmicec/*/cpp/com/rdk/hal/hdmicec" \
                        -type d 2>/dev/null | head -1)
            fi
            if [ -n "${_sc}" ]; then
                bbnote "AIDL cpp stubs not in recipe sysroot; using: ${_sc}"
                STUB_DIR="${_sc}"
                HAL_DIR=$(dirname "${_sc}")
                # The include root is 4 levels above STUB_DIR (.../usr/include/com/rdk/hal/hdmicec)
                BASE_INCDIR=$(dirname $(dirname $(dirname $(dirname "${_sc}"))))
            else
                bbfatal "Cannot find AIDL hdmicec cpp stubs; check that rdk-halif-aidl:do_compile succeeded"
            fi
        fi

        INCFLAGS="-I${BASE_INCDIR} -I${STUB_DIR} -I${STAGING_INCDIR}/binder -I${STAGING_INCDIR}/android"
        for f in IHdmiCec IHdmiCecController IHdmiCecEventListener Property SendMessageStatus State; do
                ${CXX} ${CXXFLAGS} ${INCFLAGS} -fPIC \
                        -c "${STUB_DIR}/${f}.cpp" -o "${OBJ_DIR}/${f}.o"
        done
        ${CXX} ${CXXFLAGS} ${INCFLAGS} -fPIC \
                -c "${HAL_DIR}/PropertyValue.cpp" -o "${OBJ_DIR}/PropertyValue.o"
        ${AR} rcs "${B}/libhdmicec_aidl_stubs.a" \
                "${OBJ_DIR}/IHdmiCec.o" \
                "${OBJ_DIR}/IHdmiCecController.o" \
                "${OBJ_DIR}/IHdmiCecEventListener.o" \
                "${OBJ_DIR}/Property.o" \
                "${OBJ_DIR}/SendMessageStatus.o" \
                "${OBJ_DIR}/State.o" \
                "${OBJ_DIR}/PropertyValue.o"

        # If any rdk-halif-aidl headers or link libraries are missing from the
        # recipe sysroot (stale sysroot), copy them from sysroots-components or
        # rdk-halif-aidl image.  Check each subdirectory independently so a
        # partially-populated sysroot still gets the missing pieces.
        _NEED_HDRS=0
        for _d in binder com android utils android-base log cutils; do
            if [ ! -d "${STAGING_INCDIR}/${_d}" ]; then
                _NEED_HDRS=1
                break
            fi
        done
        _NEED_LIBS=0
        if [ ! -f "${STAGING_LIBDIR}/libbinder.so" ]; then
            _NEED_LIBS=1
        fi
        if [ "${_NEED_HDRS}" = "1" ] || [ "${_NEED_LIBS}" = "1" ]; then
            _broot=$(find "${TMPDIR}/sysroots-components" \
                    -path "*rdk-halif-aidl/usr/include/binder" \
                    -type d 2>/dev/null | head -1)
            if [ -z "${_broot}" ]; then
                _broot=$(find "${TMPDIR}/work" \
                        -path "*rdk-halif-aidl/*/image/usr/include/binder" \
                        -type d 2>/dev/null | head -1)
            fi
            if [ -n "${_broot}" ]; then
                _inc=$(dirname "${_broot}")
                _lib=$(dirname "${_inc}")/lib
                bbnote "rdk-halif-aidl headers/libs missing from recipe sysroot; copying from $(dirname ${_inc})"
                if [ "${_NEED_HDRS}" = "1" ]; then
                    for _d in binder android utils android-base log cutils com; do
                        if [ -d "${_inc}/${_d}" ] && [ ! -d "${STAGING_INCDIR}/${_d}" ]; then
                            cp -r "${_inc}/${_d}" "${STAGING_INCDIR}/${_d}"
                        fi
                    done
                fi
                if [ "${_NEED_LIBS}" = "1" ] && [ -d "${_lib}" ]; then
                    for _so in "${_lib}"/*.so; do
                        [ -e "${_so}" ] && cp -a "${_so}" "${STAGING_LIBDIR}/"
                    done
                fi
            else
                bbwarn "rdk-halif-aidl headers/libs not found in sysroots-components or image"
            fi
        fi
}



do_install:append() {
#        install -d ${D}${includedir}/rdk/hdmicec
#        install -d ${D}${includedir}/ccec/drivers
#        install -m 0644 ${S}/ccec/drivers/include/ccec/drivers/iarmbus/CecIARMBusMgr.h ${D}${includedir}/ccec/drivers
#        install -d ${D}${systemd_unitdir}/system
#        install -m 0644 ${S}/cecdaemon.service ${D}${systemd_unitdir}/system
#        install -m 0644 ${S}/cecdevmgr.service ${D}${systemd_unitdir}/system
#        install -d ${D}${base_libdir}/rdk
}

do_configure:append() {
        case ":${OVERRIDES}:" in
                *:vdevice_x86-64-mw:*)
                        return 0
                        ;;
        esac

    # Patch the generated Makefile to:
    #  1. link the AIDL stubs archive into libRCEC.so so typeinfo symbols are defined
    #  2. add -lbinder so android::BBinder/android::BpBinder typeinfo is resolved at
        #     runtime from the binder provider in the target image
    sed -i \
                                "s|^libRCEC_la_LIBADD = .*|libRCEC_la_LIBADD = ${B}/libhdmicec_aidl_stubs.a \${top_builddir}/osal/src/libRCECOSHal.la|" \
                                "${B}/ccec/src/Makefile"

    sed -i \
                                's|libRCEC_la_LDFLAGS = -lpthread|libRCEC_la_LDFLAGS = -lpthread -lbinder -lutils -llog -lbase|' \
                                "${B}/ccec/src/Makefile"
}

do_configure:append:vdevice_x86-64-mw() {
                # Patch the generated Makefile to:
                #  1. link the AIDL stubs archive into libRCEC.so so typeinfo symbols are defined
                #  2. add -lbinder so android::BBinder/android::BpBinder typeinfo is resolved at
                #     runtime from libbinder.so (which rdk-halif-aidl installs)
                sed -i \
                        's|libRCEC_la_LIBADD = -lRCECOSHal|libRCEC_la_LIBADD = -lhal_aidl -lRCECOSHal|' \
                        ${B}/ccec/src/Makefile

                sed -i \
                        's|libRCEC_la_LDFLAGS = -lpthread|libRCEC_la_LDFLAGS = -lpthread -lbinder -lutils -llog -lbase|' \
                        ${B}/ccec/src/Makefile
}

# entservices-hdmicecsource still looks for the legacy HAL soname.
# On x86 we only build libRCEC/libRCECOSHal, so provide a compatibility symlink.
do_install:append:vdevice_x86-64-mw() {
        if [ -e "${D}${libdir}/libRCEC.so" ] && [ ! -e "${D}${libdir}/libRCECHal.so" ]; then
                ln -sf libRCEC.so ${D}${libdir}/libRCECHal.so
        fi
}

FILES:${PN}:append:vdevice_x86-64-mw = " ${libdir}/libRCECHal.so"

#SYSTEMD_SERVICE:${PN} = "cecdaemon.service"
#SYSTEMD_SERVICE:${PN} = "cecdevmgr.service"
#FILES:${PN} += "${systemd_unitdir}/system/cecdaemon.service"
#FILES:${PN} += "${systemd_unitdir}/system/cecdevmgr.service"
# Breakpad processname and logfile mapping
#BREAKPAD_LOGMAPPER_PROCLIST = "CecDaemonMain"
#BREAKPAD_LOGMAPPER_LOGLIST = "cec_log.txt"
