CXXFLAGS += " -I${PKG_CONFIG_SYSROOT_DIR}/${includedir}/rdk/iarmbus"

EXTRA_OECONF:append = " --enable-iarm --enable-debug"

EXTRA_OECONF += "${@bb.utils.contains('DISTRO_FEATURES', 'tee_enabled', '--enable-tee', '', d)}"
RDEPENDS:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'tee_enabled', 'virtual/tee', '', d)}"
LDFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'tee_enabled', '-lteec', '', d)}"

LDFLAGS += "-lRDKMfrLib -lcjson"
LDFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'dunfell', ' -lsafec-3.5.1',' -lsafec',d)}"
RDEPENDS:${PN} += "iarmbus wpeframework  virtual/mfrlib cjson safec"

