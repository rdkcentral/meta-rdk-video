SUMMARY = "ENTServices voice control plugin"

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-voicecontrol;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://rdkservices.ini \
          "

PV = "1.0.5"
PR = "r0"
SRCREV = "${PV}"

require include/ctrlmplugins.inc
