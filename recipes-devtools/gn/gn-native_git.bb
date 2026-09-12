#sample
SUMMARY = "GN is a meta-build system that generates build files for Ninja."
HOMEPAGE = "https://gn.googlesource.com/gn"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0fca02217a5d49a14dfe2d11837bb34d"

inherit python3native native

DEPENDS = "ninja-native"

SRC_URI = "git://gn.googlesource.com/gn.git;protocol=https;branch=main"
SRCREV = "39a87c0b36310bdf06b692c098f199a0d97fc810"

S = "${WORKDIR}/git"

do_configure() {
    ${PYTHON} build/gen.py
}

do_compile() {
    ninja -C out
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/out/gn ${D}${bindir}
}

INSANE_SKIP:${PN} += "already-stripped"
