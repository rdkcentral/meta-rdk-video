SUMMARY = "PackageManager plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c03d0e6d700b63b51bf8da6b61dac850"

PV = "4.4.1"
PR = "r0"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

S = "${WORKDIR}/git"

inherit cmake pkgconfig python3native

SRC_URI = "git://github.com/rdkcentral/PackageManager.git;protocol=https;branch=main"

SRCREV = "38729b8edfc3ddaba0b3625c19bcd2dd1a05b027"

DEPENDS += "wpeframework entservices-apis wpeframework-tools-native"
RDEPENDS:${PN} += "wpeframework "

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
"

do_install:append() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'true', 'false', d)} == 'true'; then
        if [ -d "${D}/etc/WPEFramework/plugins" ]; then
            find ${D}/etc/WPEFramework/plugins/ -type f | xargs sed -i -r 's/"autostart"[[:space:]]*:[[:space:]]*true/"autostart":false/g'
        fi
    fi
}

FILES_SOLIBSDEV = ""
FILES:${PN} += "${sysconfdir}/WPEFramework/plugins/*"
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
