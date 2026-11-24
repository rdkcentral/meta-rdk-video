PACKAGECONFIG:append = " legacy-rpc-v1"
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://0001-fix-asio-visibility-and-io-thread-order.patch"