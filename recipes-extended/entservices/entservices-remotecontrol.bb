SUMMARY = "ENTServices remote control plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7e2eceb64cc374eafafd7e1a4e763f63"

ENTSERVICES_CONTROL_REPO = "entservices-remotecontrol"
SRCREV = "0047e5faa1b3d296c80280244a7057597276a89c"
require include/entservices-control-common.inc

include include/remotecontrol.inc

PACKAGES =+ "${PN}-test"
FILES:${PN}-test += "${bindir}/remoteControlTestClient"
