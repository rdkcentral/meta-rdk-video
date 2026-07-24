# Remove wpe-webkit dependency - building without webkit
DEPENDS:remove = "wpe-webkit"
# opencdm headers migrated from wpeframework-clientlibraries to entservices-opencdmi
DEPENDS:append = " entservices-opencdmi"
# Remove aamp dependencies - building without aamp
RDEPENDS:${PN}:remove = "firebolt-native-aamp-sdk"
