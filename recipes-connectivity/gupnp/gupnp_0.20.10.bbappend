FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
## The below patch is required to use gssdp-1.6 with gupnp-1.0
SRC_URI:remove = " file://0001-Use-gssdp-1.2.patch"

SRC_URI:append = " file://0001-Use-gssdp-1.6.patch"
