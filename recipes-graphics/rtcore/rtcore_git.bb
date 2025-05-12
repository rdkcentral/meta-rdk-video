DESCRIPTION = "rtCore"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=e7948fb185616891f6b4b35c09cd6ba5"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

DEPENDS = "openssl util-linux curl zlib"

EXTRA_OECMAKE += "-DRTCORE_COMPILE_WARNINGS_AS_ERRORS=OFF "
EXTRA_OECMAKE += "-DCMAKE_SKIP_RPATH=ON "

inherit cmake pythonnative

PV ?= "1.0.0"
PR ?= "r0"

S = "${WORKDIR}/git"

SRC_URI = "git://github.com/rdkcentral/pxCore;branch=rtcore"
SRCREV = "b4f004a8c2347e0ba8c0bf76cfbdc064cb8d6fd8"

SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations -Wno-maybe-uninitialized -Wno-address"

TARGET_CFLAGS += " -fno-delete-null-pointer-checks -fpermissive -Os "
TARGET_CXXFLAGS += " -fno-delete-null-pointer-checks -fpermissive -Os "

do_install () {
   install -d ${D}/${libdir}
   cp -dr ${S}/build/rtcore/librtCoreExt.so ${D}/${libdir}/librtCoreExt.so

   mkdir -p ${D}${includedir}/rtcore
   mkdir -p ${D}${includedir}/rtcore/unix
   install -m 0644 ${S}/src/*.h ${D}${includedir}/rtcore
   install -m 0644 ${S}/src/unix/*.h ${D}${includedir}/rtcore/unix
}

FILES:${PN} += "${libdir}/*.so"
FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "dev-so staticdev"
INSANE_SKIP:${PN}:append:morty = " ldflags"
DEBIAN_NOAUTONAME:${PN} = "1"
BBCLASSEXTEND = "native"
