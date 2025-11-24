SUMMARY = "Soc-specific implementations for video applications"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
DEPENDS += " gstreamer1.0 gstreamer1.0-plugins-base virtual/vendor-audio-service "
DEPENDS:append = " virtual/vendor-rdk-gstreamer-utils-platform"
RDEPENDS:${PN}:append = " gstreamer1.0 virtual/vendor-rdk-gstreamer-utils-platform"
AUDIOMIXER_NOT_SUPPORTED = "${@bb.utils.contains('DISTRO_FEATURES', 'disable_audio_mixer', "true", "", d)}"
EXTRA_OECMAKE += " \
    -DAUDIOMIXER_NOT_SUPPORTED=${AUDIOMIXER_NOT_SUPPORTED} \
"

PV = "2.0.2"
PR ?= "r0"

SRC_URI = "${CMF_GITHUB_ROOT}/gstreamer-netflix-platform;${CMF_GITHUB_SRC_URI_SUFFIX}"

S = "${WORKDIR}/git"
CXXFLAGS += "-I${STAGING_INCDIR}/glib-2.0 -I${STAGING_INCDIR}/gstreamer-1.0 -I${STAGING_DIR_TARGET}/${libdir}/glib-2.0/include/ "

do_compile () {
    oe_runmake -C ${S} -f Makefile LDFLAGS="${LDFLAGS} -Wl,--hash-style=gnu -lrdkgstreamerutilsplatform"
}

do_install() {
    install -d ${D}/${libdir}
    install -d ${D}/usr/include
    install -m 0755 ${S}/librdkgstreamerutils.so ${D}/${libdir}
    install -m 0644 ${S}/rdk_gstreamer_utils.h ${D}/usr/include
}

INSANE_SKIP:${PN} = "dev-so"
FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so"
INSANE_SKIP:${PN} += "ldflags textrel"
SRCREV = "ea9c7ec1a810053619596123f5bd6fd22b3215f4"
