PLUGIN_MONITOR_INSTANCES_LIST += "${@bb.utils.contains('DISTRO_FEATURES', 'texttrack', 'org.rdk.TextTrack,0,0,1,60,60', '', d)}"
