PACKAGECONFIG:append = " tr181set"
PACKAGECONFIG[tr181set] = "--enable-tr181set=yes"
DEPENDS+="iarmbus tr69hostif-headers wdmp-c curl "

CXXFLAGS:append:rdktv = " -DUSE_NONSECURE_TR181_LOCALSTORE "

# generating minidumps symbols
inherit breakpad-wrapper
DEPENDS += "breakpad breakpad-wrapper"
BREAKPAD_BIN:append = "rfc"
PACKAGECONFIG:append = " breakpad"
PACKAGECONFIG[breakpad] = "--enable-breakpad,,breakpad,"

LDFLAGS += "-lbreakpadwrapper -lpthread -lstdc++"
CXXFLAGS += "-DINCLUDE_BREAKPAD"

do_install:append() {
      install -m 0755 ${S}/getRFC.sh ${D}${base_libdir}/rdk
      install -m 0755 ${S}/isFeatureEnabled.sh ${D}${base_libdir}/rdk
      ln -sf ${bindir}/tr181 ${D}${bindir}/tr181Set
}

FILES:${PN} += "${bindir}/tr181"
FILES:${PN} += "${bindir}/tr181Set"
FILES:${PN} += "${libdir}/*"
