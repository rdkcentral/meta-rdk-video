DESCRIPTION = "uWebSockets"
HOMEPAGE = ""

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${THISDIR}/files/Apache-2.0;md5=3b83ef96387f14655fc854ddc3c6bd57"

S = "${WORKDIR}/git"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
#Apr 22 2018
SRC_URI = "git://github.com/uNetworking/uWebSockets.git;branch=v0.14 \
           file://0001-libuv-makefile.patch
          "

SRCREV = "c7aa984726e41aa37a7dd75b76e45113759199ee"
#SRC_URI[sha256sum] = "f1981bedabb71995641529986570c69be873d3f148b34e865876e4fd34b389b9"

DEPENDS = "openssl libuv zlib"

do_compile() {
  cd  ${S}
  make -f Makefile
}

do_install() {
   install -d ${D}/${libdir}
   cp -a ${B}/libuWS.so ${D}/${libdir}

   install -d ${D}${includedir}
   mkdir -p ${D}${includedir}/uwebsockets

   install -m 0644 ${S}/src/*.h ${D}${includedir}/uwebsockets
}

FILES:${PN} += "${libdir}/*.so"
FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "dev-so staticdev"
INSANE_SKIP:${PN}:append:morty = " ldflags"
INSANE_SKIP:${PN} += "already-stripped"
INSANE_SKIP:${PN}:append:morty = " ldflags"
DEBIAN_NOAUTONAME:${PN} = "1"
BBCLASSEXTEND = "native"

FILES:${PN} += "${libdir}/libuWS.so"

TARGET_CC_ARCH += "${LDFLAGS}"
