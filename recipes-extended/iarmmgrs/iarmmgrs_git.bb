SUMMARY = "IARMMGRS applications"
SECTION = "console/utils"

LICENSE = "Apache-2.0 & ISC"
LIC_FILES_CHKSUM = "file://LICENSE;md5=83a31d934b0cc2ab2d44a329445b4366"


PV = "1.1.9"
PR = "r0"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SAVEDDIR := "${THISDIR}"

SRCREV = "0223e8afc6d4fcda29e6300313d1dc0b5ba5279d"
SRC_URI = "${CMF_GITHUB_ROOT}/iarmmgrs;${CMF_GITHUB_SRC_URI_SUFFIX};name=iarmmgrs"
SRCREV_FORMAT = "iarmmgrs"
#SRC_URI:append = " file://irmgr.diff"
S = "${WORKDIR}/git"

DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec', " ", d)}"
DEPENDS:append = " safec-common-wrapper"

# Telemetry Support
DEPENDS:append = " telemetry"

PARALLEL_MAKE = ""
DEPENDS="curl yajl dbus iarmbus rdk-logger hdmicec devicesettings virtual/vendor-devicesettings-hal \
         ermgr iarmmgrs-hal-headers openssl systemd libsyswrapper rfc libunpriv boost c-ares \
         deepsleep-manager-headers power-manager-headers wpeframework-clientlibraries"
DEPENDS:append:client = " virtual/mfrlib"
DEPENDS:append = " virtual/mfrlib"
DEPENDS:append = " virtual/vendor-devicesettings-hal "
DEPENDS:append = " virtual/vendor-deepsleepmgr-hal virtual/vendor-pwrmgr-hal "
RDEPENDS:${PN}:append = " devicesettings rfc"
RDEPENDS:${PN}_client_morty += " virtual/mfrlib"
RDEPENDS:${PN} += "${VIRTUAL-RUNTIME_mfrlib} devicesettings"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'directfb', 'directfb', '', d)}"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"


inherit pkgconfig breakpad-logmapper syslog-ng-config-gen
SYSLOG-NG_FILTER = "uimgr"
SYSLOG-NG_SERVICE_uimgr += "dsmgr.service mfrmgr.service sysmgr.service"
#The log rate and destination are mentioned at iarmbus_git.bb, to avoid duplication of variables set we have commented the below variables.
#SYSLOG-NG_DESTINATION_uimgr = "uimgr_log.txt"
#SYSLOG-NG_LOGRATE_uimgr = "very-high"

#key-simulator
CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'enable_eu_remote', ' -D_SKQ_KEY_MAP_1_', '', d)}"

CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec',  ' `pkg-config --cflags libsafec`', '-fPIC', d)}"
CXXLAGS:append:client = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec',  ' `pkg-config --cflags libsafec`', '-fPIC', d)}"

LDFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --libs libsafec`', '', d)}"
CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"
CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"
CFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_STB', ' -DMFR_TEMP_CLOCK_READ ', '', d)} "
CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_STB', ' -DMFR_TEMP_CLOCK_READ ', '', d)} "

CFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'enable_hdmiin_support', ' -DHAS_HDMI_IN_SUPPORT ', '', d)}"
CFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'enable_compositein_support', ' -DHAS_COMPOSITE_IN_SUPPORT ', '', d)}"
CFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'enable_spdif_support', ' -DHAS_SPDIF_SUPPORT ', '', d)}"
CFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'enable_headphone_support', ' -DHAS_HEADPHONE_SUPPORT ', '', d)}"

LDFLAGS += "-lrfcapi -lz"

EXTRA_OECONF = " --enable-yocto"
# this component doesn't build with -Wl,-as-needed, remove the flag for now
ASNEEDED = ""

INCLUDE_DIRS = " \
    -I${S}/mfr/include \
    -I${S}/sysmgr/include \
    -I${S}/dsmgr \
    -I=${includedir}/wdmp-c \
    -I=${includedir}/rdk/safeclib \
    -I=${includedir}/rdk/iarmbus \
    -I=${includedir}/rdk/halif/power-manager \
    -I=${includedir}/rdk/halif/deepsleep-manager \
    -I=${includedir}/rdk/halif/ds-hal \
    -I=${includedir}/ccec/drivers/include \
    -I=${includedir}/ccec/drivers/ \
    -I=${includedir} \
    -I=${includedir}/rdk/ds \
    -I=${includedir}/rdk/ds-hal \
    -I=${includedir}/rdk/ds-rpc \
    -I=${includedir}/rdk/iarmmgrs-hal \
    -I=${includedir}/directfb \
    -I=${includedir}/glib-2.0 \
    -I=${includedir}/WPEFramework/powercontroller \
    -I=${libdir}/glib-2.0/include \
    -I${S}/deviceUpdateMgr \
    -I${S}/utils \
    -I${S}/deviceUpdateMgr/include \
    -I${S}/ipMgr/include \
    -I${S}/vrexmgr/include \
    -I=${includedir}/rdk/servicemanager/helpers \
    -I=${includedir}/rdk/servicemanager \
    "

MFR_LIB ?= '\"libRDKMfrLib.so\"'
MFR_LIB_NAME ?= "-lRDKMfrLib"
# FIXME
# rdk_build.sh has this and we might need to do something with it:
# export _ENABLE_WAKEUP_KEY=-D_ENABLE_WAKEUP_KEY
# export USE_GREEN_PEAK_RF4CE_INTERFACE=-DUSE_GREEN_PEAK_RF4CE_INTERFACE
# export _ENABLE_RESET_LOGIC=-D_ENABLE_RESET_LOGIC
#MADAN
CFLAGS += "-D_ENABLE_RESET_LOGIC -D_ENABLE_WAKEUP_KEY -DENABLE_SD_NOTIFY "
CFLAGS:remove = "-DRF4CE_GPMSO_API"

# note: we really on 'make -e' to control LDFLAGS and CFLAGS from here. This is
# far from ideal, but this is to workaround the current component Makefile

# TODO
# FIXME
# Disabling vrexmgr frpm building now since it has dependency with
# green peak header files which is now residing @ rf4ce. Reference:
# JIRA: XRE-6537.
#
#MADAN
LDFLAGS += " -lpthread -lglib-2.0 -ldbus-1 -lIARMBus -lsystemd -lsecure_wrapper -lprivilege -ldl -lWPEFrameworkPowerController"
CFLAGS += "-std=c++11 -fPIC -D_REENTRANT -Wall -I./include ${INCLUDE_DIRS}"

CFLAGS:append:client = " -DMEDIA_CLIENT"
CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'wifi', '-DENABLE_MFR_WIFI', '', d)}"
CFLAGS:append = " -DUSE_YAJL2"
CFLAGS +=  "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_UK', '-DENABLE_EU_RESOLUTION', \
             bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_IT', '-DENABLE_EU_RESOLUTION', \
             bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_DE', '-DENABLE_EU_RESOLUTION', \
             bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_AU', '-DENABLE_EU_RESOLUTION', \
             bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_STB', '-DENABLE_FLEX2_RESOLUTION', \
             '', d), d), d), d), d)}"
CXXLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_UK', '-DENABLE_EU_RESOLUTION', \
             bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_IT', '-DENABLE_EU_RESOLUTION', \
             bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_DE', '-DENABLE_EU_RESOLUTION', \
             bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_AU', '-DENABLE_EU_RESOLUTION', \
             bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_STB', '-DENABLE_FLEX2_RESOLUTION', \
             '', d), d), d), d), d)}"
EXTRA_OEMAKE += "-e MAKEFLAGS="

do_compile() {

    oe_runmake -B -C ${S}/utils/
    #LDFLAGS="-lsystemd ${LDFLAGS}" CFLAGS="-DENABLE_SD_NOTIFY ${CFLAGS}" oe_runmake -B -C ${S}/sysmgr/
    LDFLAGS="${LDFLAGS}" CFLAGS="-DENABLE_SD_NOTIFY ${CFLAGS}" oe_runmake -B -C ${S}/sysmgr/
    CFLAGS=" ${CFLAGS}" LDFLAGS="-lds -lds-hal -ldshalsrv -ldl -L${S}/utils -liarmUtils ${LDFLAGS}" oe_runmake -B -C ${S}/dsmgr/

    if [ "${@bb.utils.contains('PACKAGECONFIG', 'mfr', 'mfr', '', d)}" != "" ]; then

        #Pass the mfr versioned lib
        libfile=$(echo ${MFR_LIB} | sed 's/^"//' | sed 's/"$//')
        mfr_build_dep_chain="${RECIPE_SYSROOT}${libdir}/${libfile}"
        echo "mfr mfr_build_dep_chain: ${mfr_build_dep_chain}"
        if [ -L "${mfr_build_dep_chain}" ]; then
            versioned_lib=$(readlink -f "${mfr_build_dep_chain}")
            echo "mfr resolved versioned_lib: ${versioned_lib}"
            MFR_VERSIONED_LIB="\"$(basename ${versioned_lib})\""
        fi
        echo "mfr versioned lib: ${MFR_VERSIONED_LIB}"

        export COMCAST_PLATFORM=XI4
        export CFLAGS="${CFLAGS} -DENABLE_SD_NOTIFY -DRDK_MFRLIB_NAME='${MFR_VERSIONED_LIB}'"
        export LDFLAGS="${LDFLAGS} ${MFR_LIB_NAME} -L${S}/utils -liarmUtils -lsystemd -ldl"
        oe_runmake -B -C ${S}/mfr
    fi
}

inherit update-rc.d coverity systemd pkgconfig
INITSCRIPT_NAME = "iarmmgrsd"
INITSCRIPT_PARAMS = "defaults 76"

do_install() {

    install -d ${D}${bindir}
    install -d ${D}${sysconfdir}



    for i in sysmgr rdmmgr receiver; do
        install -d ${D}${includedir}/rdk/iarmmgrs/$i
        install -m 0644 ${S}/$i/include/*.h ${D}${includedir}/rdk/iarmmgrs/$i
    done

    install -d ${D}${bindir}
    for i in dsmgr/*Main sysmgr/*Main; do
        install -m 0755 ${S}/$i ${D}${bindir}
    done

    install -d ${D}${libdir}
    install -m 0755 ${S}/utils/libiarmUtils.so ${D}${libdir}/libiarmUtils.so.0.0.0
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${S}/conf/dsmgr.service ${D}${systemd_unitdir}/system
    install -m 0644 ${S}/conf/sysmgr.service ${D}${systemd_unitdir}/system
    ln -rsf ${D}${libdir}/libiarmUtils.so.0.0.0  ${D}${base_libdir}/libiarmUtils.so

    if [ "${@bb.utils.contains('PACKAGECONFIG', 'mfr', 'mfr', '', d)}" != "" ]; then
        install -d ${D}${includedir}/rdk/iarmmgrs/mfr ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/mfr/include/*.h ${D}${includedir}/rdk/iarmmgrs/mfr
        install -m 0755 ${S}/mfr/*Main ${D}${bindir}
        install -m 0644 ${S}/conf/mfrmgr.service ${D}${systemd_unitdir}/system
    fi
}

do_install:append() {
    install -d ${D}${libdir}
    ln -sf libiarmUtils.so.0.0.0 ${D}${libdir}/libiarmUtils.so
}

PACKAGECONFIG ??= ""
PACKAGECONFIG[mfr] = "-DUSE_MFR,,,"

SYSTEMD_SERVICE:${PN} += "dsmgr.service"
SYSTEMD_SERVICE:${PN} += "sysmgr.service"

SYSTEMD_SERVICE:${PN} += "${@bb.utils.contains('PACKAGECONFIG', 'mfr', 'mfrmgr.service', '', d)}"
FILES:${PN} += "${systemd_unitdir}/system/*.service"
FILES:${PN} += "${libdir}/*"
FILES_SOLIBSDEV = ""
SOLIBS = ".so"
INSANE_SKIP:${PN} += "dev-so"
# Breakpad processname and logfile mapping
BREAKPAD_LOGMAPPER_PROCLIST = "dsMgrMain,IARMDaemonMain,mfrMgrMain,sysMgrMain"
BREAKPAD_LOGMAPPER_LOGLIST = "uimgr_log.txt"

DEPENDS:append:client = " ${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV',' sqlite3  ', '',d)}"
RDEPENDS:${PN}:client += "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV',' sqlite3  ', '',d)}"
RDEPENDS:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV',' sqlite3', '',d)}"

INCLUDE_DIRS += " \
        -I${S}/hal/include \
        "
CFLAGS += "${INCLUDE_DIRS} -DENABLE_DEEP_SLEEP -DENABLE_DEEPSLEEP_WAKEUP_EVT "

CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_usa_remote',' -DPLATCO=1 -DXMP_TAG_OWNER_SUPPORT=1 ', '',d)}"


CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV',' -DOFFLINE_MAINT_REBOOT -DENABLE_FACTORY_RESET_VIA_FP_POWER_BUTTON -DFTUE_CHECK_ENABLED -DTHERMAL_REBOOT_DEF -DPANEL_SERIALIZATION_TYPES ', '',d)}"

PACKAGECONFIG:client = " mfr "

CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV','-DMEDIA_CLIENT', '',d)}"
CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV','-DMEDIA_CLIENT', '',d)}"

CFLAGS += "-D_DISABLE_RESET_SEQUENCE"
CFLAGS += "-D_DISABLE_SCHD_REBOOT_AT_DEEPSLEEP"
CFLAGS += "-DPLATCO_BOOTTO_STANDBY"
CFLAGS += "-DENABLE_THERMAL_PROTECTION"
CFLAGS += "-DUSE_WAKEUP_TIMER_EVT"

SRC_URI:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV',' file://0001-set-debus-address-path-dsmgr-service.patch ', '',d)}"
SRC_URI:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV',' file://0002-hdmi_in_essos_resmgr_env.patch ', '',d)}"
SRC_URI:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV',' file://0003-add_dsmgr_service_env_settings.patch ', '',d)}"

inherit syslog-ng-config-gen

SYSLOG-NG_FILTER += "uimgr"

LDFLAGS += "-lRDKMfrLib "

LDFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_TV',' -lsqlite3 ', '',d)}"

LDFLAGS += " -ldshalcli -lds -liarmmgrs-deepsleep-hal"

do_compile:append() {
    LDFLAGS="-ldshalcli -lds -liarmmgrs-deepsleep-hal -lrfcapi ${LDFLAGS}"  CFLAGS=" ${CFLAGS}" oe_runmake -B -C ${S}/mfr/test_mfr/
}

do_install:append(){
    if ${@bb.utils.contains('DISTRO_FEATURES', 'debug-variant', 'true', 'false', d)}; then
        install -m 0755 ${S}/mfr/test_mfr/test_mfr_client ${D}${bindir}
    fi
        install -m 0755 ${S}/mfr/test_mfr/mfr_scrubAllBanks ${D}${bindir}
        install -m 0755 ${S}/mfr/test_mfr/mfr_deletePDRI ${D}${bindir}
        install -m 0755 ${S}/mfr/test_mfr/mfr_wifiEraseAllData ${D}${bindir}
        install -m 0755 ${S}/mfr/test_mfr/mfr_wifiSetCredentials ${D}${bindir}
        install -m 0755 ${S}/mfr/test_mfr/mfr_wifiGetCredentials ${D}${bindir}

}

DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'ctrlm', 'ctrlm-headers', '', d)}"

INCLUDE_DIRS += " -I${S}/vrexmgr/include \
		  -I=${includedir} \
		"
CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'ctrlm', '-DUSE_UNIFIED_CONTROL_MGR_API_1', '', d)}"

# Flag required for rf4ce module to work
CFLAGS += "-DRF4CE_GPMSO_API"

# setting FULL_VERSION_NAME_VALUE="YOCTO" is workaround for RDKRF4CE-9
# We cant access image names from recipes
#
do_compile:append() {
    oe_runmake -C ${S}/test/
    CXXFLAGS=" ${CFLAGS} ${CPPFLAGS} -DSTB_VERSION_STRING=\\'YOCTO\\'" \
    LDFLAGS=" -lcurl -lyajl ${LDFLAGS}" \

    CPPFLAGS=" ${CFLAGS}" LDFLAGS=" ${LDFLAGS} -lyajl -lcurl -lssl -lcrypto" oe_runmake -B -C ${S}/deviceUpdateMgr/
}

CTRLM_ENABLED = "${@bb.utils.contains('DISTRO_FEATURES', 'ctrlm', 'true', 'false', d)}"

do_install:append() {
    if [ "${CTRLM_ENABLED}" = "false" ]; then
        install -m 0755 ${S}/deviceUpdateMgr/deviceUpdateMgrMain ${D}${bindir}

        # Install json config files
        install -m 0644 ${S}/deviceUpdateMgr/deviceUpdateConfig.json ${D}${sysconfdir}/deviceUpdateConfig.json
        install -m 0644 ${S}/vrexmgr/vrexPrefs.json ${D}${sysconfdir}/vrexPrefs.json

        # Temporary directory and link until vrexMgr is fixed to look in /etc
        install -d ${D}/mnt/nfs/env
        ln -sf ${sysconfdir}/vrexPrefs.json ${D}/mnt/nfs/env/vrexPrefs.json

        # Install header files
        install -d ${D}${includedir}/rdk/iarmmgrs/vrexmgr
        install -d ${D}${includedir}/rdk/iarmmgrs/deviceUpdateMgr
        install -m 0644 ${S}/vrexmgr/include/*.h ${D}${includedir}/rdk/iarmmgrs/vrexmgr
        install -m 0644 ${S}/deviceUpdateMgr/include/*.h ${D}${includedir}/rdk/iarmmgrs/deviceUpdateMgr

        # Install service
        install -m 0644 ${S}/conf/deviceupdatemgr.service ${D}${systemd_unitdir}/system

        # Install binaries
        install -m 0755 ${S}/deviceUpdateMgr/deviceUpdateMgrMain ${D}${bindir}

    fi
}


SYSTEMD_SERVICE:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'ctrlm', '', 'deviceupdatemgr.service', d)}"

FILES:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'ctrlm', '', '${sysconfdir}/deviceUpdateConfig.json', d)}"
FILES:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'ctrlm', '', '${bindir}/deviceUpdateMgrMain', d)}"
FILES:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'ctrlm', '', '${sysconfdir}/vrexPrefs.json', d)}" 

# Temporary directory and link until vrexMgr is fixed to look in /etc
FILES:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'ctrlm', '', '/mnt/nfs/env', d)}"


ALLOW_EMPTY:${PN} = "1"
