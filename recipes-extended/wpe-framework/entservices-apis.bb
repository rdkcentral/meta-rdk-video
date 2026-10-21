SUMMARY = "entservices-apis"

require include/entservices-apis-common.inc

# Version and revision: ENTSERVICES_APIS_VERSION in include/entservices-apis-common.inc
PR = "r0"

DEPENDS = "wpeframework wpeframework-tools-native"

SRC_URI += "file://RDKEMW-1007.patch"

# ----------------------------------------------------------------------------

# APIs built by their own recipe (include/entservices-apis-standalone.inc), which can pin its own revision:
#   OpenCDMi - entservices-apis-ocdm (incl. cdmi.h)
STANDALONE_APIS = "OpenCDMi"

EXTRA_OECMAKE += " -DSTANDALONE_APIS=${STANDALONE_APIS}"
