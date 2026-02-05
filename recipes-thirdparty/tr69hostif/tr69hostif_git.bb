SUMMARY = "TR69 Host Interface"
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=99e7c83e5e6f31c2cbb811e186972945"

SRCREV = "4165c452affc7d33865f5572b8a909f7af56c531"

SRC_URI = "${CMF_GITHUB_ROOT}/tr69hostif;${CMF_GITHUB_SRC_URI_SUFFIX};name=tr69hostif"
PV = "1.3.0"
PR = "r1"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
S = "${WORKDIR}/git"

DEPENDS = "iarmbus iarmmgrs e2fsprogs libsoup libsyswrapper yajl \
           devicesettings procps glib-2.0 \
           cjson telemetry libtinyxml2\
	  "
DEPENDS:append = " rdk-logger libparodus parodus virtual/vendor-devicesettings-hal ${@bb.utils.contains('DISTRO_FEATURES', 'ENABLE_NETWORKMANAGER', '', 'netsrvmgr', d)}"

DEPENDS += " python-lxml-native"
DEPENDS:append = " python3-lxml-native"

DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth',' bluetooth-mgr', '',d)}"
RDEPENDS:${PN}:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth',' bluetooth-mgr', '',d)}"

DEPENDS += "safec-common-wrapper"
DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec', " ", d)}"

# Add wpeframework-clientlibraries dependency
DEPENDS += "wpeframework-clientlibraries"
RDEPENDS:${PN}:append = " wpeframework-clientlibraries "
LDFLAGS += "-lWPEFrameworkPowerController"

# Add remotedebugger dependency
DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'rrd', ' remotedebugger', " ", d)}"
RDEPENDS:${PN}:append  = " ${@bb.utils.contains('DISTRO_FEATURES', 'rrd',' remotedebugger', '',d)}"
CXXFLAGS:append     = "${@bb.utils.contains('DISTRO_FEATURES', 'rrd', ' -DUSE_REMOTE_DEBUGGER', '', d)}"
CXXFLAGS:append     = "${@bb.utils.contains('DISTRO_FEATURES', 'rrd', ' -I=${includedir}/rrd/ -I=${includedir}/rdk/iarmmgrs/rdmmgr/', '', d)}"

inherit pkgconfig breakpad-logmapper syslog-ng-config-gen useradd logrotate_config

SYSLOG-NG_FILTER = "parodus tr69hostif"
SYSLOG-NG_SERVICE_tr69hostif = "tr69hostif.service"
SYSLOG-NG_DESTINATION_tr69hostif = "tr69hostif.log"
SYSLOG-NG_LOGRATE_tr69hostif = "high"
SYSLOG-NG_SERVICE_parodus = "parodus.service"
SYSLOG-NG_DESTINATION_parodus = "parodus.log"
SYSLOG-NG_LOGRATE_parodus = "high"

LOGROTATE_NAME="parodus tr69hostif"
LOGROTATE_LOGNAME_parodus="parodus.log"
LOGROTATE_LOGNAME_tr69hostif="tr69hostif.log"
#HDD_ENABLE
LOGROTATE_SIZE_parodus="128000"
LOGROTATE_ROTATION_parodus="3"
LOGROTATE_SIZE_tr69hostif="1572864"
LOGROTATE_ROTATION_tr69hostif="3"
#HDD_DISABLE
LOGROTATE_SIZE_MEM_parodus="128000"
LOGROTATE_ROTATION_MEM_parodus="3"
LOGROTATE_SIZE_MEM_tr69hostif="1572864"
LOGROTATE_ROTATION_MEM_tr69hostif="3"

NONROOT_USER_DIR ?= "/home/non-root/"
NONROOT_USER ?= "non-root"
CPPFLAGS:append = " -DRDKCLISSA "

USERADD_PACKAGES = "${PN}"
USERADD_PARAM:${PN} = "-d /home/non-root -r -m -s /bin/sh non-root -g non-root;"
GROUPADD_PARAM:${PN} = "-r non-root;"

CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --cflags libsafec`', ' -fPIC', d)}"
CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --cflags libsafec`', ' -fPIC', d)}"

LDFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --libs libsafec`', '', d)}"
CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"

LDFLAGS:remove = "-lproc-3.2.8"
LDFLAGS += "-lprocps"

CXXFLAGS:append:rdktv = " -DUSE_NONSECURE_TR181_LOCALSTORE "
CXXFLAGS:append:rdktv = " -DENABLE_VIDEO_TELEMETRY "
CXXFLAGS += " -DFETCH_PRODUCTCLASS_FROM_MFRLIB "
CXXFLAGS += " -Wall -Werror -Wno-cpp"

DEPENDS += " rbus "

LDFLAGS:append = " -lrbus "
CXXFLAGS:append = " -I${includedir}/rbus "

RDEPENDS:${PN} += "devicesettings bash libsoup"
RDEPENDS:${PN} += "${PN}-conf"

RDEPENDS:${PN}:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'ENABLE_NETWORKMANAGER', '', 'netsrvmgr', d)}"
EXTRA_OECONF += "--disable-silent-rules --enable-InterfaceStack --enable-IPv6 --enable-notification --enable-yocto --enable-SpeedTest"
EXTRA_OECONF += " --enable-parodus"
EXTRA_OECONF:append = " --enable-powercontroller=yes"

#Enable sd_notify
EXTRA_OECONF:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '--enable-systemd-notify', '', d)}"
EXTRA_OECONF:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'NEW_HTTP_SERVER_DISABLE', '--disable-new-http-server', '', d)}"

PACKAGECONFIG ??= ""
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'wifi','wifi', '',d)}"
PACKAGECONFIG[wifi] = "--enable-wifi,,,"
PACKAGECONFIG[xre] = "--enable-xre,,"
PACKAGECONFIG[moca] = "--enable-moca,,virtual/mocadriver"
PACKAGECONFIG[moca2] = "--enable-moca2,,virtual/mocadriver"
PACKAGECONFIG[rf4ce] = "--enable-rf4ce,,"
PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth','bluetooth', '',d)}"
PACKAGECONFIG[bluetooth] = "--enable-bt,,bluetooth-mgr,bluetooth-mgr"
DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth',' bluetooth-mgr', '',d)}"
RDEPENDS:${PN}:append  = " ${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth',' bluetooth-mgr', '',d)}"

PACKAGECONFIG[emmc] = "--enable-emmc,--disable-emmc"

INCLUDE_DIRS += "\
	-I${PKG_CONFIG_SYSROOT_DIR}/usr/include/rdk/ds \
	-I${PKG_CONFIG_SYSROOT_DIR}/usr/include/rdk/ds-hal \
    -I${PKG_CONFIG_SYSROOT_DIR}/usr/include/rdk/halif/ds-hal \
	-I${PKG_CONFIG_SYSROOT_DIR}/usr/include/rdk/ds-rpc \
	-I${PKG_CONFIG_SYSROOT_DIR}/usr/include/rdk/iarmbus \
	-I${PKG_CONFIG_SYSROOT_DIR}/usr/include/rdk/iarmmgrs/tr69Bus \
	-I${PKG_CONFIG_SYSROOT_DIR}/usr/include/rdk/iarmmgrs/mfr \
	-I${PKG_CONFIG_SYSROOT_DIR}/usr/include/rdk/iarmmgrs/power \
	-I${PKG_CONFIG_SYSROOT_DIR}/usr/include/rdk/iarmmgrs-hal \
	-I${PKG_CONFIG_SYSROOT_DIR}/usr/include \
	-I${PKG_CONFIG_SYSROOT_DIR}${includedir}/WPEFramework/powercontroller \
	"

CPPFLAGS += "${INCLUDE_DIRS}"
CPPFLAGS:append = " -DMEDIA_CLIENT "
# C++11 is required
CXXFLAGS += "-std=c++11"
CXXFLAGS += " -DYAJL_V2"

EXTRA_OECONF:append = " --enable-morty --enable-t2api=yes"

inherit autotools systemd pkgconfig

do_configure:prepend() {
        sed -i -e "s%lproc-3.2.8%lprocps%"  ${S}/src/hostif/profiles/DeviceInfo/Makefile.am
        sed -i -e "s%lproc-3.2.8%lprocps%"  ${S}/src/hostif/handlers/Makefile.am
}

do_install:append:client() {
       rm -rf ${D}${sysconfdir}/tr181_snmpOID.conf
}

PACKAGECONFIG += "${@bb.utils.contains('DISTRO_FEATURES', 'emmc_storage', 'emmc', '', d)}"
PACKAGECONFIG:remove = "${@bb.utils.contains('DISTRO_FEATURES','emmc_storage','sdcard','',d)} "


do_install:append() {
	install -d ${D}${includedir}/rdk/tr69hostif ${D}${systemd_unitdir}/system
	install -d ${D}${sysconfdir}
	if ${@bb.utils.contains('DISTRO_FEATURES', 'NEW_HTTP_SERVER_DISABLE', 'true', 'false', d)}; then
		install -m 0644 ${S}/tr69hostif_no_new_http_server.service ${D}${systemd_unitdir}/system/tr69hostif.service
	else
		install -m 0644 ${S}/tr69hostif.service ${D}${systemd_unitdir}/system
	fi
    sed -i 's/@DSMGR_DEPENDENCY@/iarmbusd.service/' ${D}${systemd_unitdir}/system/tr69hostif.service
        install -m 0644 ${S}/partners_defaults.json ${D}${sysconfdir}
	install -m 0644 ${S}/ip-iface-monitor.service ${D}${systemd_unitdir}/system
	install -m 0644 ${S}/src/hostif/include/*.h ${D}${includedir}/rdk/tr69hostif
	install -m 0644 ${S}/conf/mgrlist.conf ${D}${sysconfdir}
        install -d ${D}${base_libdir}/rdk
        install -m 0644 ${S}/src/hostif/parodusClient/parodus.service ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/src/hostif/parodusClient/parodus.path ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/src/hostif/parodusClient/parodus_v4.path ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/src/hostif/parodusClient/parodus_v6.path ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/src/hostif/parodusClient/parodus_bsp.path ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/src/hostif/parodusClient/conf/notify_webpa_cfg.json ${D}${sysconfdir}
        install -m 0644 ${S}/src/hostif/parodusClient/conf/webpa_cfg.json ${D}${sysconfdir}
        install -d ${D}${sysconfdir} ${D}${sysconfdir}/rfcdefaults
        install -m 0644 ${S}/conf/rfcdefaults/tr69hostif.ini ${D}${sysconfdir}/rfcdefaults

        # Below header files are installed by another recipe tr69hostif-headers
        rm -r ${D}${includedir}/hostIf_msgHandler.h
        rm -r ${D}${includedir}/hostIf_NotificationHandler.h

        install -d ${D}${NONROOT_USER_DIR}
        chown ${NONROOT_USER}:non-root -R ${D}${NONROOT_USER_DIR}

       
        install -d ${D}${sysconfdir}
        install -m 0644 ${S}/src/hostif/parodusClient/waldb/data-model/data-model-generic.xml ${D}${sysconfdir}/data-model-generic.xml
        install -m 0644 ${S}/src/hostif/parodusClient/waldb/data-model/data-model-tv.xml ${D}${sysconfdir}/data-model-tv.xml
        install -m 0644 ${S}/src/hostif/parodusClient/waldb/data-model/data-model-stb.xml ${D}${sysconfdir}/data-model-stb.xml

}

addtask do_validate_data_model after do_install before do_package do_packagedata do_populate_sysroot

do_validate_data_model() {
        # if /etc/data-model.xml exists, verify it is well-formed and valid as per CWMP XSD
        if [ -e "${D}${sysconfdir}/data-model.xml" ]; then
            ${STAGING_BINDIR_NATIVE}/python3-native/python3 ${S}/scripts/validateDataModel.py ${D}${sysconfdir}/data-model.xml ${S}/conf/cwmp-datamodel-1-2.xsd
        fi
}

SYSTEMD_SERVICE:${PN} = "tr69hostif.service"
FILES:${PN} += "${systemd_unitdir}/system/tr69hostif.service"

SYSTEMD_SERVICE:${PN} += "ip-iface-monitor.service"
FILES:${PN} += "${systemd_unitdir}/system/ip-iface-monitor.service"
SYSTEMD_SERVICE:${PN} += "parodus.service" 
SYSTEMD_SERVICE:${PN} += "parodus.path" 
SYSTEMD_SERVICE:${PN} += "parodus_v4.path"
SYSTEMD_SERVICE:${PN} += "parodus_v6.path"
SYSTEMD_SERVICE:${PN} += "parodus_bsp.path"
FILES:${PN} += "${systemd_unitdir}/system/parodus.service" 
FILES:${PN} += "${systemd_unitdir}/system/parodus.path" 
FILES:${PN} += "${systemd_unitdir}/system/parodus_v4.path"
FILES:${PN} += "${systemd_unitdir}/system/parodus_v6.path"
FILES:${PN} += "${systemd_unitdir}/system/parodus_bsp.path"
FILES:${PN} += "${base_libdir}/*"
FILES:${PN} += "${sysconfdir}/*"
FILES:${PN} += "${NONROOT_USER_DIR}"

PACKAGE_BEFORE_PN += "${PN}-conf"

FILES:${PN}-conf = "${sysconfdir}/rfcdefaults/tr69hostif.ini"
# Breakpad processname and logfile mapping
BREAKPAD_LOGMAPPER_PROCLIST = "tr69hostif"
BREAKPAD_LOGMAPPER_LOGLIST = "tr69hostif.log"
