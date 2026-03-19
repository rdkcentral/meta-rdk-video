SUMMARY = "Sys mon tool Devicesettings utility"

DESCRIPTION = "Devicesettings utility to retrieve device settings"

SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

SYSMONTOOL_NAME = "sys-utils"


PV = "${RDK_RELEASE}+gitr${SRCPV}"
SRC_URI = "${CMF_GIT_ROOT}/rdk/components/generic/sys_mon_tools/sys_utils;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GIT_BRANCH}"
SRCREV = "f53212b9ad6feae80cea17a1a756b2b7be295934"

DEPENDS = "iarmbus iarmmgrs dbus glib-2.0 devicesettings directfb devicesettings-hal-headers "
RDEPENDS:${PN} = " devicesettings"

S = "${WORKDIR}/git"

inherit coverity autotools

INCLUDE_DIRS = " \
    -I${STAGING_INCDIR} \
    -I${STAGING_INCDIR}/rdk/iarmbus \
    -I${STAGING_INCDIR}/rdk/iarmmgrs/power \
    -I${STAGING_INCDIR}/rdk/iarmmgrs-hal \
    -I${STAGING_INCDIR}/rdk/ds \
    -I${STAGING_INCDIR}/rdk/ds-hal \
    -I${STAGING_INCDIR}/rdk/halif/ds-hal \
    -I${STAGING_INCDIR}/rdk/ds-rpc \
    -I${STAGING_INCDIR}/directfb \
    -I${STAGING_INCDIR}/glib-2.0 \
    -I${STAGING_LIBDIR}/glib-2.0/include \
    "
CFLAGS += "${INCLUDE_DIRS}"
CPPFLAGS += "${INCLUDE_DIRS}"

LDFLAGS += "-lpthread -lglib-2.0 -L. -lIARMBus -ldbus-1 -ldshalcli -ldl -lds -ljansson -luuid"

do_configure:prepend() {
        rm -rf ${S}/Makefile
}


