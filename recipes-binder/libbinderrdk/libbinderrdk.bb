DESCRIPTION = "RDK Middleware Binder module"
SECTION = "BinderModule"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI = "${CMF_GITHUB_ROOT}/linux_binder_idl;${CMF_GITHUB_SRC_URI_SUFFIX}"

#PR = "r0"
#SRCREV_TAG = 1.0.0"
PV ?= "2.7.0"
SRCREV ?= "2.7.0"
S = "${WORKDIR}/git"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

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

inherit cmake siteinfo

FILES:${PN} += " \
    ${MW_LIBDIR} \
    ${MW_LIBDIR}/* \
    ${MW_BINDIR} \
    ${MW_BINDIR}/* \
"
