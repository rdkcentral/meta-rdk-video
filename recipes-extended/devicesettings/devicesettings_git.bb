SUMMARY = "Cross-platform library for controlling STB platform hardware configuration"
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

PV = "1.0.29"
PR = "r0"

SRCREV_devicesettings = "9c65c2d30479aaf1633d829c811e098375a8f316"
SRC_URI = "${CMF_GITHUB_ROOT}/devicesettings;${CMF_GITHUB_SRC_URI_SUFFIX};name=devicesettings"

# devicesettings is not a 'generic' component, as some of its source
# files include .h files that come from the HAL implementation until
# this is fixed (see https://cards.linaro.org/browse/RDK-108).  Each
# BSP needs to implement virtual/vendor-devicesettings-hal when
# devicesettings become 'generic' we will remove the dependency on the
# hal, Note: we make this package machine specific since it uses a
# machine HAL
#MADAN
DEPENDS="json-c iarmbus rdk-logger virtual/vendor-devicesettings-hal devicesettings-hal-headers safec-common-wrapper rfc wdmp-c"
#RDEPENDS:${PN} += "directfb"
DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec ', " ", d)}"

S = "${WORKDIR}/git"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit coverity

# Thunder dependencies
DEPENDS += "wpeframework"
DEPENDS += "entservices-apis"

CFLAGS += "-DSAFEC_DUMMY_API"
CXXFLAGS += "-DSAFEC_DUMMY_API "

# Enable Thunder COM-RPC preprocessor define
CFLAGS += "-DUSE_WPE_THUNDER_PLUGIN"
CXXFLAGS += "-DUSE_WPE_THUNDER_PLUGIN"

#
# ds-hal header should preceed ds/include 
# to achieve desired HAL override
#
INCLUDE_DIRS = " \
    -I${S}/config \
    -I${S}/config/include \
    -I${STAGING_DIR_TARGET}${includedir}/rdk/ds-hal \
    -I${STAGING_DIR_TARGET}${includedir}/rdk/halif/ds-hal \
    -I./include \
    -I${S}/rpc/include \
    -I${S}/ds/include \
    -I${S}/hal/include \
    -I${STAGING_DIR_TARGET}${includedir} \
    -I${STAGING_DIR_TARGET}${includedir}/rdk/iarmbus \
    -I${STAGING_DIR_TARGET}${includedir}/rdk/logger \
    -I${STAGING_DIR_TARGET}${includedir}/glib-2.0 \
    -I${STAGING_DIR_TARGET}${libdir}/glib-2.0/include \
    -I${STAGING_DIR_TARGET}${includedir}/logger \
    -I${STAGING_INCDIR}/WPEFramework \
    "
#-I${STAGING_DIR_TARGET}${includedir}/directfb

# note: we really on 'make -e' to control LDFLAGS and CFLAGS from here. This is
# far from ideal, but this is to workaround the current component Makefile
LDFLAGS += "-lrdkloggers -lpthread -lglib-2.0 -L. -lIARMBus -ldl "
LDFLAGS += "-lWPEFrameworkCore -lWPEFrameworkCOM"
CFLAGS += "-fPIC -D_REENTRANT -Wall ${INCLUDE_DIRS}"
CFLAGS += "-DRDK_DSHAL_NAME="\""libds-hal.so.0\""""
CFLAGS += " -DYOCTO_BUILD"
CFLAGS += " -DDS_AUDIO_SETTINGS_PERSISTENCE"
CFLAGS += " -DDSMGR_LOGGER_ENABLED"

# added support for rfc
CFLAGS += "-I${STAGING_INCDIR}/wdmp-c"
CXXFLAGS += "-I${STAGING_INCDIR}/wdmp-c"
LDFLAGS +="-lrfcapi"

CFLAGS += "-fPIC -D_REENTRANT -Wall ${INCLUDE_DIRS}"
CXXFLAGS += "-fPIC -D_REENTRANT -Wall ${INCLUDE_DIRS}"

# Shared libs created by the RDK build aren't versioned, so we need
# to force the .so files into the runtime package (and keep them
# out of -dev package).
FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so"
TARGET_CC_ARCH += "${LDFLAGS}"
do_configure:prepend() {
	rm -rf ${S}/Makefile
	# If this file is not needed, remove from repo. Deleting from source directory causes rebuild when module
  	# builds from externalsrc. For now removing from git ls-files
	cd ${S}
	git update-index --assume-unchanged ${S}/Makefile
	cd -
}

do_compile() {

    # remove local version of hal since we use devicesettings-hal-headers,
    # and want to make sure we don't use this copy by error.
    rm -rf hal

    # Export Thunder flag for Makefile's ifdef check
    export USE_WPE_THUNDER_PLUGIN=y

    # and now the generic components
    oe_runmake -B -C ${S}/rpc/cli
    oe_runmake -B -C ${S}/rpc/srv
    export CFLAGS="$CFLAGS -std=c++11"
    oe_runmake -B -C ${S}/ds
#To Build Test Samples under "sample/"
    export LDFLAGS="$LDFLAGS -L${S}/ds -lds -L${S}/rpc/cli -ldshalcli"
    oe_runmake -B -C ${S}/sample dsapp
}

do_install() {

    install -d ${D}${includedir}/rdk/ds
    install -m 0644 ${S}/ds/include/*.h* ${D}${includedir}/rdk/ds
    install -m 0644 ${S}/ds/*.h* ${D}${includedir}/rdk/ds

    install -d ${D}${includedir}/rdk/ds-rpc
    install -m 0644 ${S}/rpc/include/*.h* ${D}${includedir}/rdk/ds-rpc

    install -d ${D}${libdir}
    for i in ${S}/rpc/cli/*.so ${S}/rpc/srv/*.so ${S}/ds/*.so; do
        install -m 0755 $i ${D}${libdir}
    done
}
INSANE_SKIP:${PN} = "ldflags"


FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"
CFLAGS += "-DHAS_FLASH_PERSISTENT -DHAS_THERMAL_API -DdsFPD_BRIGHTNESS_DEFAULT=10 "
#enabling HDCP callback in rpc server
CFLAGS += " -DHAS_HDCP_CALLBACK"
CFLAGS += "${@bb.utils.contains("DISTRO_FEATURES", "uhd_enabled", "-DHAS_4K_SUPPORT ", "", d)}"
CFLAGS += " -DENABLE_DEEP_SLEEP"
CFLAGS += "${@bb.utils.contains("DISTRO_FEATURES", "RDKE_PLATFORM_TV", "", " -DIGNORE_EDID_LOGIC ", d)}"
CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_UK', '-DENABLE_EU_RESOLUTION', \
             bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_IT', '-DENABLE_EU_RESOLUTION', \
             bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_DE', '-DENABLE_EU_RESOLUTION', \
             bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_AU', '-DENABLE_EU_RESOLUTION', \
             bb.utils.contains('DISTRO_FEATURES', 'RDKE_PLATFORM_STB', '-DENABLE_FLEX2_RESOLUTION', \
             '', d), d), d), d), d)}"
CFLAGS += "${@bb.utils.contains("DISTRO_FEATURES", "enable_default_resolution_720p", "-DENABLE_US_RESOLUTION ", "", d)}"
CFLAGS += "${@bb.utils.contains("DISTRO_FEATURES", "enable_all_white_led_config", "-DENABLE_US_LED_CONFIG ", "", d)}"
CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_hdmiin_support', ' -DHAS_HDMI_IN_SUPPORT ', '', d)}"
CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_compositein_support', ' -DHAS_COMPOSITE_IN_SUPPORT ', '', d)}"
CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_spdif_support', ' -DHAS_SPDIF_SUPPORT ', '', d)}"
CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_headphone_support', ' -DHAS_HEADPHONE_SUPPORT ', '', d)}"

do_install:append() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/DSApp ${D}${bindir}/DSApp
    
#    if ${@bb.utils.contains('DISTRO_FEATURES' , 'RDKE_PLATFORM_TV', 'true', 'false', d)}; then
#        install -m 0755 ${S}/sample/hdmiIn ${D}${bindir}
#    fi

}

