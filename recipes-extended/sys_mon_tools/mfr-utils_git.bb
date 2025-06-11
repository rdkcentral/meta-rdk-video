SUMMARY = "This recipe compiles utility used for mfr utilities"
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV ?= "${RDK_RELEASE}"
PR ?= "r0"
PACKAGE_ARCH ?= "${MIDDLEWARE_ARCH}"


SRC_URI = "${CMF_GITHUB_ROOT}/sys_mon_tools;${CMF_GITHUB_SRC_URI_SUFFIX};name=mfr-utils"
S = "${WORKDIR}/git"

PROVIDES="mfr-utils"

inherit pkgconfig autotools systemd

DEPENDS ="virtual/mfrlib iarmmgrs-hal-headers iarmbus iarmmgrs wpeframework-clientlibraries"
RDEPENDS:$PN = "virtual/mfrlib wpeframework-clientlibraries"

inherit autotools pkgconfig coverity


CXXFLAGS += " -I${PKG_CONFIG_SYSROOT_DIR}/${includedir}/rdk/iarmmgrs-hal/ "
CFLAGS += " -DYOCTO_BUILD -I${PKG_CONFIG_SYSROOT_DIR}${includedir}/rdk/mfrlib -I${PKG_CONFIG_SYSROOT_DIR}${includedir}/rdk/iarmmgrs-hal -I${PKG_CONFIG_SYSROOT_DIR}${includedir}/rdk/iarmbus/"
CXXFLAGS += "${CFLAGS}"

LDFLAGS += "-lIARMBus -Wl,--no-as-needed -ldl"

do_install() {
        install -d ${D}${bindir}  
        if ${@bb.utils.contains('DISTRO_FEATURES', 'debug-variant', 'true', 'false', d)}; then
          install -m 0755 ${B}/mfr_util ${D}${bindir}
        fi
}

FILES:${PN} += "${bindir}/mfr_util"
INSANE_SKIP:${PN} += "useless-rpaths"
