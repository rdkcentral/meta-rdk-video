SUMMARY = "ENTServices remote control plugin"

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-remotecontrol;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://rdkservices.ini \
          "

PV = "1.0.5"
PR = "r0"
SRCREV = "${PV}"

include include/ctrlmplugins.inc

PACKAGES =+ "${PN}-test"
FILES:${PN}-test += "${bindir}/remoteControlTestClient"
