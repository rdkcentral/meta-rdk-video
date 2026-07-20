# DS_COMRPC migration: replace real devicesettings runtime with no-op stub.
# devicesettings-stub RPROVIDES "devicesettings", satisfying all remaining RDEPENDS.
# Rollback: delete this file.
RDEPENDS:${PN}:remove = "devicesettings"
RDEPENDS:${PN}:append = " devicesettings-stub"
