# DS_COMRPC migration: remove devicesettings runtime dependency.
# tenablehdcp has no direct device:: calls — libds.so not needed.
# Rollback: delete this file.
RDEPENDS:${PN}:remove = "devicesettings"
