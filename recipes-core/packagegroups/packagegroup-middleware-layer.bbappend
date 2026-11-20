RDEPENDS:${PN} += " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'enable_rialto', 'rialto-reference-player rialto-reference-player-jsbindings', '', d)} \
"
