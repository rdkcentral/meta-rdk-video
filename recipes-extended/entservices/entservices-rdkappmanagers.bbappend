EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_rdkappmanagers_microservice_plugins', '-DENABLE_PLUGIN_RDKAPPMANAGERS=ON', '-DENABLE_PLUGIN_RDKAPPMANAGERS=OFF', d)}"
