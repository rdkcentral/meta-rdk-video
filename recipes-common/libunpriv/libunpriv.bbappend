DEPENDS += "wdmp-c cimplog"
LDFLAGS += "-lwdmp-c -lcimplog"
CPPFLAGS:append = " -I${STAGING_INCDIR}/wdmp-c \
                    -I${STAGING_INCDIR}/cimplog \
                    -D_RDK_VIDEO_PRIV_CAPS_ \
                  "

do_install:append(){
        install -d ${D}${sysconfdir}
        install -d ${D}${sysconfdir}/security/caps/
        install -m 755 ${S}/source/process-capabilities_video.json ${D}${sysconfdir}/security/caps/process-capabilities.json
}

FILES:${PN} += " ${sysconfdir}/security/caps/process-capabilities.json"
