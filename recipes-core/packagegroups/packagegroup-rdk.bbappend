# DS_COMRPC migration: replace real devicesettings runtime with no-op stub.
# devicesettings-stub RPROVIDES "devicesettings", satisfying all remaining RDEPENDS.
# Rollback: delete this file.
RDEPENDS:packagegroup-rdk-generic:remove = "devicesettings"
RDEPENDS:packagegroup-rdk-generic:append = " devicesettings-stub"
