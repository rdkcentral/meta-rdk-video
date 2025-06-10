SUMMARY = "EntOSMigrationTestApp plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=894043553c584ff36ca284238b9ad9a1"

PV ?= "1.0.0"
PR ?= "r0"

#S = "${WORKDIR}/git"
S = "${WORKDIR}/entservices-entosmigrationtestapp"
inherit cmake pkgconfig


PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

EXTRA_OECMAKE += " -DENABLE_RFC_MANAGER=ON"
EXTRA_OECMAKE += " -DBUILD_ENABLE_THERMAL_PROTECTION=ON "
EXTRA_OECMAKE += "-DDISABLE_GEOGRAPHY_TIMEZONE=ON"
EXTRA_OECMAKE += " -DENABLE_SYSTEM_GET_STORE_DEMO_LINK=ON "
EXTRA_OECMAKE += " -DBUILD_ENABLE_DEVICE_MANUFACTURER_INFO=ON "
EXTRA_OECMAKE += " -DBUILD_ENABLE_APP_CONTROL_AUDIOPORT_INIT=ON "
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'link_localtime', ' -DBUILD_ENABLE_LINK_LOCALTIME=ON', '',d)}"

DEPENDS += "cmake-native"
DEPENDS += "power-manager-headers wpeframework wpeframework-tools-native"
RDEPENDS:${PN} += "wpeframework"

TARGET_LDFLAGS += " -Wl,--no-as-needed -ltelemetry_msgsender -Wl,--as-needed "

#CXXFLAGS += "-I${STAGING_INCDIR}/rdk/iarmbus/ "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/rdk/iarmbus/ "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/wdmp-c/ "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/trower-base64/ "
CXXFLAGS += " -DRFC_ENABLED "
# enable filtering for undefined interfaces and link local ip address notifications
CXXFLAGS += " -DNET_DEFINED_INTERFACES_ONLY -DNET_NO_LINK_LOCAL_ANNOUNCE "
CXXFLAGS += " -Wall -Werror "
CXXFLAGS:remove_morty = " -Wall -Werror "
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"

# ----------------------------------------------------------------------------

PACKAGECONFIG ?= " entosmigrationtestapp\
    ${@bb.utils.contains('DISTRO_FEATURES', 'systimemgr', 'systimemgrsupport', '', d)} \
"

DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"

PACKAGECONFIG[entosmigrationtestapp]      = "-DPLUGIN_ENTOSMIGRATIONTESTAPP=ON,-DPLUGIN_ENTOSMIGRATIONTESTAPP=OFF,iarmbus iarmmgrs rfc devicesettings virtual/vendor-devicesettings-hal,iarmbus rfc devicesettings"
# ----------------------------------------------------------------------------


# Check if DisplayInfo backend is defined.
python () {
    machine_name = d.getVar('MACHINE')
    if 'raspberrypi4' in machine_name:
        d.appendVar('EXTRA_OECMAKE', ' -DBUILD_RPI=ON')
}

do_fetch[noexec] = "1"
do_configure:prepend () {
   cp -ar "${THISDIR}/entservices-entosmigrationtestapp" "${WORKDIR}/"
}
do_populate_lic[noexec] = "1"


# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/*.so ${datadir}/WPEFramework/*"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
