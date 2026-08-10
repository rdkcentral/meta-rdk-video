OVERRIDES:append = "${@bb.utils.contains('DISTRO_FEATURES', 'thunder_5', ':thunder_5', '', d)}"

BBMASK += "${@bb.utils.contains('DISTRO_FEATURES', 'thunder_5', '.*/wpeframework(-ui|-tools|-clientlibraries|-extensions)?_.*\.bb', '', d)}"

THUNDER_NAMESPACE = "WPEFramework"
THUNDER_NAMESPACE_LC = "wpeframework"

THUNDER_NAMESPACE:thunder_5 = "Thunder"
THUNDER_NAMESPACE_LC:thunder_5 = "thunder"
