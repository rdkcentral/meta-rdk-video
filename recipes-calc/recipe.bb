DESCRIPTION = "This is the mock implementation for calculator app."

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${MANIFEST_PATH_META_RDK}/licenses/Apache-2.0;md5=3b83ef96387f14655fc854ddc3c6bd57"

DEPENDS = "iarmmgrs-hal-headers" 

SRC_URI = "file://calc_new"

S = "${WORKDIR}/calc_new"

PROVIDES = "virtual/calc_new"
RPROVIDES:${PN} = "virtual/calc_new"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

do_compile()
{
    make
}

do_install()
{
    install -d ${D}${bindir}
    install -m 0755 ${S}/main ${D}${bindir}
}

FILES:${PN} = "${bindir}/main"




