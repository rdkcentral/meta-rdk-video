
MFR:qemuall = ""

# DS_COMRPC migration: replace real devicesettings runtime with no-op stub.
# devicesettings-stub RPROVIDES "devicesettings", satisfying all remaining RDEPENDS.
# Rollback: remove the two lines below.
RDEPENDS:packagegroup-rdk-media-common:remove = "devicesettings"
RDEPENDS:packagegroup-rdk-media-common:append = " devicesettings-stub"

RDEPENDS:packagegroup-rdk-media-common += "\
   essos-examples \
   ${@bb.utils.contains("DISTRO_FEATURES", "rdkshell", "rdkshell", "", d)} \
   ${@bb.utils.contains("DISTRO_FEATURES", "offline_apps", "lxapp-bt-audio  residentui", "", d)} \
   ${@bb.utils.contains("DISTRO_FEATURES", "build_rne", "wpeframework-ui", "", d)} \
   ${@bb.utils.contains('DISTRO_FEATURES', 'fwupgrader', 'rdkfwupgrader', '', d)} \
   ${@bb.utils.contains('DISTRO_FEATURES', 'enable_rialto','rialto-client rialto-server rialto-servermanager rialto-gstreamer rialto-ocdm', '', d) } \
   ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'thunderstartupservices', '', d)} \
   "
