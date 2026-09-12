SUMMARY = "This recipe installs a game controller database to device"

LICENSE = "Zlib"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Zlib;md5=87f239f408daca8a157858e192597633"

PROVIDES = "libmanette-controllerdb"
RPROVIDES:${PN} = "libmanette-controllerdb"
SRC_URI = "file://gamecontrollerdb"
S = "${WORKDIR}"

do_compile[noexec] = "1"
do_configure[noexec] = "1"

do_install() {
    install -d ${D}${datadir}/libmanette/
    install -m 0644 ${WORKDIR}/gamecontrollerdb ${D}${datadir}/libmanette/
}

FILES:${PN} += "${datadir}/libmanette"

