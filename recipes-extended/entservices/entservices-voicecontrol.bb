SUMMARY = "ENTServices voice control plugin"

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-voicecontrol;${CMF_GITHUB_SRC_URI_SUFFIX} \
           file://rdkservices.ini \
          "

PV = "1.0.5"
PR = "r0"
SRCREV = "48e2f5b25e69df158057fd64c700d4284b6bb0a6"

include include/ctrlmplugins.inc
