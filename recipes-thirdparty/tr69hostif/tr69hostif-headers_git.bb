SUMMARY = "TR69 hostif Headers"
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=99e7c83e5e6f31c2cbb811e186972945"

PV = "1.3.0"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

SRCREV = "4165c452affc7d33865f5572b8a909f7af56c531"
SRC_URI = "${CMF_GITHUB_ROOT}/tr69hostif;${CMF_GITHUB_SRC_URI_SUFFIX};name=tr69hostif"

DEPENDS += "safec-common-wrapper"
DEPENDS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' safec', " ", d)}"

inherit pkgconfig

CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --cflags libsafec`', ' -fPIC', d)}"
CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --cflags libsafec`', ' -fPIC', d)}"

LDFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --libs libsafec`', '', d)}"
CFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"

CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --cflags libsafec`', ' -fPIC', d)}"
CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', ' `pkg-config --cflags libsafec`', ' -fPIC', d)}"
CXXFLAGS:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'safec', '', ' -DSAFEC_DUMMY_API', d)}"


S = "${WORKDIR}/git"

do_compile[noexec] = "1"

do_install() {
	install -d ${D}${includedir}
	install -m 0644 ${S}/src/hostif/include/*.h ${D}${includedir}
	install -m 0644 ${S}/src/hostif/handlers/include/*.h ${D}${includedir}
}
ALLOW_EMPTY:${PN} = "1"
