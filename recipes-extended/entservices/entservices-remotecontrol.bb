SUMMARY = "ENTServices remote control plugin"

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-remotecontrol;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://rdkservices.ini \
          "

PV = "1.0.6"
PR = "r0"
SRCREV = "eb08dcfaa703f9afdd6337245fcf0bc8c53d4b6c"

include include/ctrlmplugins.inc

PACKAGES =+ "${PN}-test"
FILES:${PN}-test += "${bindir}/remoteControlTestClient"
