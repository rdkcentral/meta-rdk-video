DESCRIPTION = "RDK Middleware Binder module"
SECTION = "BinderModule"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI = "${RDKCENTRAL_GITHUB_ROOT}/linux_binder_idl;${RDKCENTRAL_GITHUB_SRC_URI_SUFFIX}"

PV ?= "1.1.1"
PR = "r0"
SRCREV ?= "4278b0c80d098b1853976f3ced5275d77e53c0aa"

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
MW_LIBDIR = "${libdir}/mw"
MW_BINDIR = "${bindir}/mw"
MW_INCDIR = "${includedir}/mw"

EXTRA_OECMAKE += " \
    -DCMAKE_INSTALL_LIBDIR=${MW_LIBDIR} \
    -DCMAKE_INSTALL_BINDIR=${MW_BINDIR} \
    -DCMAKE_INSTALL_INCDIR=${MW_INCDIR} \
"

inherit cmake

CXXFLAGS:append = " -DBINDER_IPC_32BIT"

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
