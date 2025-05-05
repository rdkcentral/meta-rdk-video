# Maintenance Manager Plugin, provides an interface to control and monitor maintenance activities on Embedded devices

# Task Timeout for Maintenance tasks
MAINTENANCE_TASK_TIMEOUT = "3600"

# Syslog-ng configuration for Maintenance Manager
SYSLOG-NG_FILTER += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_maintenance_manager maintenance_journal_logging', 'maintenancemanager', '', d)}"
SYSLOG-NG_PROGRAM_maintenancemanager = "MaintenanceManager"
SYSLOG-NG_DESTINATION_maintenancemanager = "maintenance.log"
SYSLOG-NG_LOGRATE_maintenancemanager = "very-high"

# Log Rotation for Maintenance Manager Logs
LOGROTATE_NAME = "${@bb.utils.contains('DISTRO_FEATURES', 'enable_maintenance_manager maintenance_journal_logging', 'maintenancemanager', '', d)}"
LOGROTATE_LOGNAME_maintenancemanager = "maintenance.log"
LOGROTATE_SIZE_maintenancemanager = "786432"
LOGROTATE_ROTATION_maintenancemanager = "3"
LOGROTATE_SIZE_MEM_maintenancemanager = "786432"
LOGROTATE_ROTATION_MEM_maintenancemanager = "3"

PACKAGECONFIG:append = "${@bb.utils.contains('DISTRO_FEATURES' , 'enable_maintenance_manager', ' maintenancemanager', '', d)}"

# Enable suppress maintenance
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'suppress_maintenance', '-DSUPPRESS_MAINTENANCE=ON', '', d)}"

# Enable WhoAmI
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'whoami_enabled', ' -DENABLE_WHOAMI=ON', '',d)}"

# Add Task Timeout
EXTRA_OECMAKE += "-DTASK_TIMEOUT=${MAINTENANCE_TASK_TIMEOUT}"

# Enable Journal Logging
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_maintenance_manager maintenance_journal_logging', ' -DENABLE_JOURNAL_LOGGING=ON', '',d)}"

PACKAGECONFIG[maintenancemanager]   = "-DPLUGIN_MAINTENANCEMANAGER=ON -DDISABLE_DCM_TASK=ON,-DPLUGIN_MAINTENANCEMANAGER=OFF,iarmbus iarmmgrs rfc sysint rdkfwupgrader dcmd entservices-apis,iarmbus rfc sysint rdkfwupgrader dcmd entservices-apis"
