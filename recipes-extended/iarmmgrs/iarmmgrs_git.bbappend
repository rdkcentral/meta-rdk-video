# iarmmgrs_git.bbappend
#
# DS_COMRPC migration: mask dsmgr.service so that dsMgrMain is never
# started by systemd at boot.
#
# entservices-devicesettings (Thunder COM-RPC plugin) replaces dsMgr as
# the DeviceSettings provider for all migrated Thunder clients.
# The dsMgrMain binary and dsmgr.service file remain on the image so
# this change is fully reversible.
#
# Rollback: delete this file. dsmgr.service will start normally again.

do_install:append() {
    # Mask dsmgr.service by symlinking it to /dev/null.
    # A masked unit cannot be started manually or automatically.
    install -d ${D}${sysconfdir}/systemd/system
    ln -sf /dev/null ${D}${sysconfdir}/systemd/system/dsmgr.service
}

FILES:${PN} += "${sysconfdir}/systemd/system/dsmgr.service"
