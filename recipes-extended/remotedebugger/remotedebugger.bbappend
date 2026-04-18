
CFLAGS += " -Wall -Werror"

# Add wpeframework-clientlibraries dependency for Ent-os powermanager
DEPENDS +=  "${@bb.utils.contains('DISTRO_FEATURES', 'pwrmgr-plugin', 'wpeframework-clientlibraries', '', d)}"
RDEPENDS:${PN}:append = "${@bb.utils.contains('DISTRO_FEATURES', 'pwrmgr-plugin', ' wpeframework-clientlibraries ', '', d)}"
CFLAGS += " ${@bb.utils.contains('DISTRO_FEATURES', 'pwrmgr-plugin', " -I${PKG_CONFIG_SYSROOT_DIR}${includedir}/WPEFramework/powercontroller ", "", d)} "
CFLAGS += " ${@bb.utils.contains('DISTRO_FEATURES', 'pwrmgr-plugin', " -DPWRMGR_PLUGIN ", "", d)} "
LDFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'pwrmgr-plugin', " -lWPEFrameworkPowerController", "", d)}"
