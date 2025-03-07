SUMMARY = "rt headers"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=e7948fb185616891f6b4b35c09cd6ba5"

PV ?= "2.0.0"
PR ?= "r1"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
S = "${WORKDIR}/git"

def get_spark_rev(d):
    spark_rev_s = bb.utils.contains('DISTRO_FEATURES', 'enable_spark_tip', '${AUTOREV}', '51333037650ecee44191492b541106efa573cc35', d)
    return spark_rev_s

def get_spark_git_details(d):
    spark_details_s = bb.utils.contains('DISTRO_FEATURES', 'enable_spark_tip', 'git://github.com/rdkcentral/pxCore;branch=rdk', 'git://github.com/rdkcentral/pxCore;branch=rdk', d)
    return spark_details_s


SRC_URI = "${@get_spark_git_details(d)}"
SRCREV = "baa62f69c332d82496518eb5251a32b3521b71f5"

SRC_URI += "file://0001-unregister-wayland-when-destroyed.patch \
            file://DELIA-50375.patch"

# this is a Header package only, nothing to build
do_compile[noexec] = "1"
do_configure[noexec] = "1"

# also get rid of the default dependency added in bitbake.conf
# since there is no 'main' package generated (empty)
RDEPENDS:${PN}-dev = ""
# to include the headers in the SDK
ALLOW_EMPTY:${PN} = "1"

do_install() {
    install -d ${D}${includedir}
    mkdir -p ${D}${includedir}/pxcore
    mkdir -p ${D}${includedir}/pxcore/unix
    mkdir -p ${D}${includedir}/pxcore/gles
    install -m 0644 ${S}/src/*.h ${D}${includedir}/pxcore
    install -m 0644 ${S}/src/unix/*.h ${D}${includedir}/pxcore/unix
    install -m 0644 ${S}/src/gles/*h ${D}${includedir}/pxcore/gles
    # utf8.h is causing xre build failure
    rm -f ${D}${includedir}/pxcore/utf*.h
}
