SUMMARY = "Thunder Plugin deactivation script installed via systemd deInitialiser Service"
LICENSE = "Apache-2.0"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

PR = "r0"
PV = "1.0.0"
# The source file to compile
SRC_URI = "file://deactivate_plugin.sh \
          "

S = "${WORKDIR}"

do_install() {
    install -d ${D}/lib/rdk
    install -m 0755 ${WORKDIR}/deactivate_plugin.sh ${D}/lib/rdk/deactivate_plugin.sh
}

FILES:${PN} += "/lib/rdk/deactivate_plugin.sh"
