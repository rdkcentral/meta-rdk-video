SUMMARY = "Common DRM platform abstraction framework"
DESCRIPTION = "Common C++ API for DRM platform hooks and SoC callbacks"

LICENSE = "Apache-2.0"

LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

inherit features_check
REQUIRED_DISTRO_FEATURES = "use_ocdm_drm_ext"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRCREV_FORMAT = "drm-ext"
SRC_URI = "git://github.com/rdkcentral/drm-ext.git;${CMF_GITHUB_SRC_URI_SUFFIX};name=drm-ext"

SRCREV = "10e478034c775b369136a1aff7427605e21ba76b"

S = "${WORKDIR}/git"

inherit cmake

DEPENDS = ""

FILES:${PN} += "${libdir}/*.so ${libdir}/*.so.*" 
SOLIBS = ".so"
FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} = "dev-so installed-vs-shipped"

RDEPENDS:${PN} += " drm-ext-soc"

do_install:append() {
        install -d ${D}/${libdir}
        install -m 0755 ${WORKDIR}/build/*.so        ${D}/${libdir}
}

DEBIAN_NOAUTONAME:${PN} = "1"
DEBIAN_NOAUTONAME:${PN}-dev = "1"
DEBIAN_NOAUTONAME:${PN}-dbg = "1"
DEBIAN_NOAUTONAME:${PN}-src = "1"
DEBIAN_NOAUTONAME:${PN}-staticdev = "1"
DEBIAN_NOAUTONAME:${PN}-doc = "1"
DEBIAN_NOAUTONAME:${PN}-locale = "1"
