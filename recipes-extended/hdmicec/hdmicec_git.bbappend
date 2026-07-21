# DS_COMRPC migration: remove devicesettings runtime dependency.
# hdmicec uses Thunder COM-RPC for DS queries — libds.so not needed.
# Rollback: delete this file.
RDEPENDS:${PN}:remove = "devicesettings"
