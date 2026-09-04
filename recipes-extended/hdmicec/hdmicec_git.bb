SUMMARY = "This recipe compiles and installs hdmicec component."
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "1.0.11"
PV:vdevice_x86-64-mw = "1.0.11.1"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRCREV_hdmicec = "7c46960036c15c66727d06b65454273715563c8a"
SRCREV_hdmicec:vdevice_x86-64-mw = "57df60fdf8866460613735af1d2e39caa3939242"
SRC_URI = "${CMF_GITHUB_ROOT}/hdmicec;${CMF_GITHUB_SRC_URI_SUFFIX};name=hdmicec"
SRCREV_FORMAT = "hdmicec"

DEPENDS = "glib-2.0 dbus iarmbus devicesettings devicesettings-hal-headers hdmicecheader virtual/vendor-hdmicec-hal iarmmgrs-hal-headers telemetry"
DEPENDS:remove:vdevice_x86-64-mw = "devicesettings devicesettings-hal-headers iarmmgrs-hal-headers"

RDEPENDS:${PN} = " devicesettings telemetry"
RDEPENDS:${PN}:remove:vdevice_x86-64-mw = "devicesettings"
RDEPENDS:${PN}:append:vdevice_x86-64-mw = " rdk-halif-aidl-mw-hdmicec rdk-halif-aidl-mw-common libbinderrdk"

DEPENDS += "safec-common-wrapper"
DEPENDS:append:vdevice_x86-64-mw = " rdk-halif-aidl-mw libbinderrdk"

ASNEEDED = ""
ALLOW_EMPTY:${PN} = "1"

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
LDFLAGS:append:vdevice_x86-64-mw = " \
    -L${STAGING_DIR_HOST}${prefix}/mw/lib/binder -L${STAGING_LIBDIR}/mw/rdk-halif-aidl \
"
CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"
CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"

CFLAGS:append:vdevice_x86-64-mw = " -I${STAGING_INCDIR}/mw/hdmicec/0.1.0.0/include -I${STAGING_INCDIR}/mw/common/0.2.0.0/include -I${STAGING_INCDIR}/mw/include -I${STAGING_INCDIR}/android"
CXXFLAGS:append:vdevice_x86-64-mw = " -I${STAGING_INCDIR}/mw/hdmicec/0.1.0.0/include -I${STAGING_INCDIR}/mw/common/0.2.0.0/include -I${STAGING_INCDIR}/mw/include -I${STAGING_INCDIR}/android"

INCLUDE_DIRS = " \
    -I=${includedir}/rdk/halif/ds-hal \
    "

do_install:append() {
#        install -d ${D}${includedir}/rdk/hdmicec
#        install -d ${D}${includedir}/ccec/drivers
#        install -m 0644 ${S}/ccec/drivers/include/ccec/drivers/iarmbus/CecIARMBusMgr.h ${D}${includedir}/ccec/drivers
#        install -d ${D}${systemd_unitdir}/system
#        install -m 0644 ${S}/cecdaemon.service ${D}${systemd_unitdir}/system
#        install -m 0644 ${S}/cecdevmgr.service ${D}${systemd_unitdir}/system
#        install -d ${D}${base_libdir}/rdk
}

do_configure:append:vdevice_x86-64-mw() {
    # Patch the generated Makefile to:
        #  1. link the AIDL helpers archive into libRCEC.so so typeinfo symbols are defined
    #  2. add -lbinder so android::BBinder/android::BpBinder typeinfo is resolved at
                #     runtime from libbinder.so
    sed -i \
                                                "s|^libRCEC_la_LIBADD = .*|libRCEC_la_LIBADD = -lhdmicec-cpp \${top_builddir}/osal/src/libRCECOSHal.la|" \
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
