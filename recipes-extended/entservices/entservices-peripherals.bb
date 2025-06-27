SUMMARY = "ENTServices peripherals plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7e2eceb64cc374eafafd7e1a4e763f63"

PV ?= "1.0.5"
PR ?= "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-peripherals;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://0001-RDKTV-20749-Revert-Merge-pull-request-3336-from-npol.patch \
          "

# Release version - 1.0.5
SRCREV = "d1599dd3eacfc54a24e8580573dfdb654856ff31"

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
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"

# More complicated plugins are moved seperate includes
include include/remotecontrol.inc

PACKAGECONFIG ?= " breakpadsupport \
    telemetrysupport \
    frontpanel \
    ${@bb.utils.contains('DISTRO_FEATURES', 'ctrlm', 'voicecontrol remotecontrol', '', d)} \
"

PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'ENABLE_MOTION_DETECTION', ' motiondetection','',d)}"

PACKAGECONFIG[breakpadsupport]      = ",,breakpad-wrapper,breakpad-wrapper"
PACKAGECONFIG[telemetrysupport]     = "-DBUILD_ENABLE_TELEMETRY_LOGGING=ON,,telemetry,telemetry"
PACKAGECONFIG[motiondetection]      = "-DPLUGIN_MOTION_DETECTION=ON,,virtual/vendor-motiondetector-hal virtual/vendor-fpdriverlib,virtual/vendor-motiondetector-hal virtual/vendor-fpdriverlib"
PACKAGECONFIG[voicecontrol]         = "-DPLUGIN_VOICECONTROL=ON,-DPLUGIN_VOICECONTROL=OFF,iarmbus iarmmgrs ctrlm-headers,iarmbus ctrlm"
PACKAGECONFIG[remotecontrol]        = "-DPLUGIN_REMOTECONTROL=ON,-DPLUGIN_REMOTECONTROL=OFF,iarmbus iarmmgrs ctrlm-headers,iarmbus ctrlm"
PACKAGECONFIG[frontpanel]           = "-DPLUGIN_FRONTPANEL=ON,,iarmbus iarmmgrs devicesettings virtual/vendor-devicesettings-hal,iarmbus devicesettings"
PACKAGECONFIG[ledcontrol]           = "-DPLUGIN_LEDCONTROL=ON,,iarmbus iarmmgrs devicesettings entservices-apis virtual/vendor-devicesettings-hal,iarmbus devicesettings entservices-apis"

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DSECAPI_LIB=sec_api \
"

# Check if DRI_DEVICE_NAME is defined. If yes- use that as DEFAULT_DEVICE. If not, use DEFAULT_DEVICE configured from rdkservices.
python () {
    dri_device_name = d.getVar('DRI_DEVICE_NAME')
    if dri_device_name:
        d.appendVar('OECMAKE_CXX_FLAGS', ' -DDEFAULT_DEVICE=\'\\"{}\\"\' '.format(dri_device_name))
}

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

PACKAGES =+ "${PN}-test"
FILES:${PN}-test += "${bindir}/remoteControlTestClient"

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/*.so ${datadir}/WPEFramework/*"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
