SUMMARY = "entservices-apis: OpenCDMi interfaces"
DESCRIPTION = "apis/OpenCDMi of entservices-apis, built as a standalone API"

STANDALONE_API = "OpenCDMi"

require include/entservices-apis-standalone.inc

PR = "r0"

# Same entservices-apis release as entservices-apis.bb (ENTSERVICES_APIS_VERSION).
# To move the OCDM interfaces on their own - together with entservices-opencdmi
# and the DRM implementations - pin a commit here, e.g.:
#   PV = "4.4.1+git${SRCPV}"
#   SRCREV_entservices-apis = "<commit>"

do_install:append() {
    install -m 0644 ${D}${includedir}/WPEFramework/interfaces/IDRM.h ${D}${includedir}/cdmi.h
}

FILES:${PN} += "${includedir}/cdmi.h"
