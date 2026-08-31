DESCRIPTION = "RDK Middleware Binder module"
SECTION = "BinderModule"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI = "${RDKCENTRAL_GITHUB_ROOT}/linux_binder_idl;${RDKCENTRAL_GITHUB_SRC_URI_SUFFIX}"

PV ?= "1.1.1"
PR = "r0"
SRCREV ?= "1.0.0"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

S = "${WORKDIR}/git"

#
# Middleware package provides
#
PROVIDES += "libbinderrdk liblogrdk"
RPROVIDES:${PN} += "libbinderrdk liblogrdk"

#
# Middleware installation locations
#
# libdir matches BINDER_SDK_DIR/lib/binder expected by consumer CMakeLists.txt
MW_LIBDIR = "${prefix}/mw/lib/binder"
MW_BINDIR = "${bindir}/mw"
MW_INCDIR = "${includedir}/mw/include"

EXTRA_OECMAKE += " \
    -DCMAKE_INSTALL_LIBDIR=${MW_LIBDIR} \
    -DCMAKE_INSTALL_BINDIR=${MW_BINDIR} \
    -DCMAKE_INSTALL_INCDIR=${MW_INCDIR} \
"

EXTRA_OECMAKE:append = " \
	-DBUILD_ENV_YOCTO=ON \
	-DBUILD_ENV_HOST=OFF \
	-DTARGET_LIB64_VERSION=ON \
"


# MW_LIBDIR is outside the default staged ${libdir}, so it must be staged explicitly
SYSROOT_DIRS += "${prefix}/mw"

inherit cmake

#
# Configure Android Binder sources
#
do_configure:prepend() {
    cd ${S}

    # Initialise Android Binder build environment
    . ./setup-env.sh

    # Clone Android Binder sources
    clone_android_binder_repo

    cd ${B}
}

FILES:${PN} += " \
    ${MW_LIBDIR} \
    ${MW_LIBDIR}/* \
    ${MW_BINDIR} \
    ${MW_BINDIR}/* \
"

#
# QA
#
INSANE_SKIP:${PN}-dev += "dev-elf"
INSANE_SKIP:${PN} += "dev-deps"

FILES_SOLIBSDEV = ""
