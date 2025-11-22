DESCRIPTION = "rtCore"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=e7948fb185616891f6b4b35c09cd6ba5"


DEPENDS = "openssl util-linux curl zlib"

EXTRA_OECMAKE += "-DRTCORE_COMPILE_WARNINGS_AS_ERRORS=OFF "
EXTRA_OECMAKE += "-DCMAKE_SKIP_RPATH=ON "

inherit cmake pythonnative


S = "${WORKDIR}/git"

SRC_URI = "git://github.com/rdkcentral/pxCore;branch=rtcore"
SRCREV = "c6ba0955009509fcc48b48d57eb6ce80543440cf"

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

PV = "1.0.1"
PR = "r2"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
