FIREBOLT_CPP_CLIENT_PV = "0.5.5"
FIREBOLT_CPP_CLIENT_SRC_URI = "https://github.com/rdkcentral/firebolt-cpp-client/releases/download/v${FIREBOLT_CPP_CLIENT_PV}/firebolt-cpp-client-${FIREBOLT_CPP_CLIENT_PV}.tar.gz"
FIREBOLT_CPP_CLIENT_SHA256SUM = "9ecb43d70cfe9f5737d28c2cae150e8b3258a70a54cb77982fa7da508aecfbbd"

FIREBOLT_CPP_TRANSPORT_PV = "1.1.6"
FIREBOLT_CPP_TRANSPORT_SRC_URI = "https://github.com/rdkcentral/firebolt-cpp-transport/releases/download/v${FIREBOLT_CPP_TRANSPORT_PV}/firebolt-cpp-transport-${FIREBOLT_CPP_TRANSPORT_PV}.tar.gz"
FIREBOLT_CPP_TRANSPORT_SHA256SUM = "fba0193b4fbc69b68b3c9674d102ef3d95aca923af020d6b35d75713aaf35369"

GN_ARGS_EXTRA:append = " rdk_enable_firebolt_api=true use_system_firebolt_libs=false "
GN_ARGS_EXTRA:append = " firebolt_client_version_string=\"${FIREBOLT_CPP_CLIENT_PV}\" "
GN_ARGS_EXTRA:append = " firebolt_transport_version_string=\"${FIREBOLT_CPP_TRANSPORT_PV}\" "

DEPENDS:append = " nlohmann-json websocketpp boost " 

SRC_URI += "${FIREBOLT_CPP_CLIENT_SRC_URI};name=fbclient"
SRC_URI += "${FIREBOLT_CPP_TRANSPORT_SRC_URI};name=fbtransport"
SRC_URI[fbclient.sha256sum] = "${FIREBOLT_CPP_CLIENT_SHA256SUM}"
SRC_URI[fbtransport.sha256sum] = "${FIREBOLT_CPP_TRANSPORT_SHA256SUM}"

SRCREV_FORMAT:append = "_${FIREBOLT_CPP_CLIENT_PV}_${FIREBOLT_CPP_TRANSPORT_PV}"

do_unpack_extra:append() {
   bbnote "symlink firebolt cpp client and transport"
   ( cd "${S}/third_party" && ln -sf ../../firebolt-cpp-client-${FIREBOLT_CPP_CLIENT_PV} firebolt-cpp-client )
   ( cd "${S}/third_party" && ln -sf ../../firebolt-cpp-transport-${FIREBOLT_CPP_TRANSPORT_PV} firebolt-cpp-transport )
}
