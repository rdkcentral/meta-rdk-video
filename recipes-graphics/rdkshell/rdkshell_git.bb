DESCRIPTION = "RDKShell"
HOMEPAGE = "https://github.com/rdkcentral/RDKShell"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=25e1268bd2a2291ebad380ef00c2f842"

DEPENDS = "westeros essos libjpeg libpng virtual/libgles2"

inherit cmake

PV ?= "1.0.0"
PR ?= "r0"
PACKAGE_ARCH ?= "${MIDDLEWARE_ARCH}"

S = "${WORKDIR}/git"

SRC_URI = "git://github.com/rdkcentral/RDKShell;branch=master"

# Jun 5, 2024
SRCREV ?= "a0a88b812d39ee57b15b48f00488c4d9ba737f14"

# Adding this as patch for development phase. This can be upstreamed once kirkstone builds are verified
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI:append = " file://shared_ptr_fix_kirkstone.patch"

EXTRA_OECMAKE += "-DRDKSHELL_BUILD_FORCE_1080=ON"

do_install() {
    install -d ${D}/home/root
    install -m 0755 ${B}/rdkshell ${D}/home/root/

    install -d ${D}/${libdir}
    install -m 0755 ${B}/librdkshell.so ${D}/${libdir}/

    install -d ${D}/${libdir}/plugins/westeros
    install -m 0755 ${B}/extensions/RdkShellExtendedInput/libwesteros_plugin_rdkshell_extended_input.so.1.0.0 ${D}/${libdir}/plugins/westeros/libwesteros_plugin_rdkshell_extended_input.so

    install -d ${D}${includedir}/rdkshell
    install -m 0644 ${S}/*.h ${D}${includedir}/rdkshell/
}

FILES:${PN} += " \
                ${libdir}/*.so \
                /home/root/rdkshell \
                ${libdir}/librdkshell.so \
                ${libdir}/plugins/westeros/libwesteros_plugin_rdkshell_extended_input.so \
                "

# Clearing FILES_SOLIBSDEV if not packaging development symlink libraries
FILES_SOLIBSDEV = ""

# Skipping certain QA checks
INSANE_SKIP:${PN} += "dev-so staticdev already-stripped"
INSANE_SKIP:${PN}:append:morty = " ldflags"

# Preventing Debian packaging tools from automatically renaming the package
DEBIAN_NOAUTONAME:${PN} = "1"

# Extend to native builds if this is intended for the build system
BBCLASSEXTEND = "native"
