PACKAGECONFIG:append = " tr181set"
PACKAGECONFIG[tr181set] = "--enable-tr181set=yes"
DEPENDS+="iarmbus tr69hostif-headers wdmp-c curl "

CXXFLAGS:append:rdktv = " -DUSE_NONSECURE_TR181_LOCALSTORE "

do_install:append() {
      install -m 0755 ${S}/getRFC.sh ${D}${base_libdir}/rdk
      install -m 0755 ${S}/isFeatureEnabled.sh ${D}${base_libdir}/rdk
      ln -sf ${bindir}/tr181 ${D}${bindir}/tr181Set
}

FILES:${PN} += "${bindir}/tr181"
FILES:${PN} += "${bindir}/tr181Set"
FILES:${PN} += "${libdir}/*"
