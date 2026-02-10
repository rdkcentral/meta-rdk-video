SUMMARY = "RDK Firmware Upgrader daemon"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=70514b59ff7b36bbbc30d093c6814d8e"

# To have a possibility to override SRC_URI later, we are introducing the following workaround:
PV = "1.4.0"
PR = "r0"

SRCREV_rdkfw = "242382b14b9b1b74b4ec0464b89321f91631643b"
SRC_URI = "${CMF_GITHUB_ROOT}/rdkfwupdater;${CMF_GITHUB_SRC_URI_SUFFIX};name=rdkfw"

DEPENDS +=" cjson curl rdk-logger rbus"
DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec', " ", d)}"
DEPENDS:append = " safec-common-wrapper rfc telemetry iarmbus iarmmgrs dbus glib-2.0 commonutilities libsyswrapper "


CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec',  ' `pkg-config --cflags libsafec`', '-fPIC', d)}"
CXXFLAGS:append:client = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec',  ' `pkg-config --cflags libsafec`', '-fPIC', d)}"
CFLAGS += "-I=${includedir}/wdmp-c -I${STAGING_INCDIR}/rdk/iarmbus -I${STAGING_INCDIR}/rdk/iarmmgrs/sysmgr -I${STAGING_INCDIR}/glib-2.0"
CFLAGS += "-I${STAGING_LIBDIR}/glib-2.0/include"
CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_maintenance_manager', '-DEN_MAINTENANCE_MANAGER -I${STAGING_INCDIR}/rdk/iarmmgrs-hal ', '', d)}"
CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'debug_codebig_cdl', ' -DDEBUG_CODEBIG_CDL', '', d)}"
CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'ctrlm', '-DCTRLM_ENABLED', '', d)}"
CFLAGS += " -Wall -Werror"
CFLAGS:append = " -DRDK_LOGGER"
CXXFLAGS += " -Wall -Werror"

EXTRA_OECONF = "--enable-rfcapi --enable-t2api"
EXTRA_OECONF += "${@bb.utils.contains('DISTRO_FEATURES', 'debug-variant', '--enable-test-fwupgrader', '', d)}"

LDFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --libs libsafec`', '', d)}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRCREV_FORMAT = "rdkfw"

S = "${WORKDIR}/git"

inherit autotools pkgconfig coverity systemd

SYSTEMD_SERVICE:${PN}:remove = " rdkfwupgrader.service rdkfwupgrader.path"

FILES:${PN}:remove = " ${bindir}/rdkfwupgrader \
                 ${base_libdir}/rdk/rdkfwupgrader_start.sh \
                 ${base_libdir}/rdk/rdkfwupgrader_check_now.sh \
                 ${base_libdir}/rdk/rdkfwupgrader_abort_reboot.sh "

INSANE_SKIP:${PN}:append = " installed-vs-shipped"
