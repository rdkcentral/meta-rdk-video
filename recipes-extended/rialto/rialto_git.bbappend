EXTRA_OECMAKE:append = ' \
-DRIALTO_ENABLE_CONFIG_FILE=1 \
-DRIALTO_CONFIG_FILE_DIR=/etc/'
 
FILES:${PN}-servermanager-lib += "/etc/rialto-config.json"
