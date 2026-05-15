SUMMARY = "Script to install tdk ipks"
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/../generic/tdk/generic/LICENSE;md5=160f54cb11e918adefb9060de75d725d"

SRC_URI = "${CMF_GIT_ROOT}/rdkv/tools/tdkv;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GIT_BRANCH};name=tdk"
SRCREV_tdk = "b05d3991d362bf03c4d267c0ba7eac4b806f74f3"

SRC_URI += " \
    file://tdk_service.patch \
    file://InstallTDK.sh \
    "

S = "${WORKDIR}"

require recipes-extended/tdk/tdk.inc

inherit autotools systemd

#Override the do_patch() for installtdk to patch tdk.service fiel
do_patch () {
    if [ "${TDK_IPK}" = "TRUE" ]; then
        patch -d ${S}/git/ < tdk_service.patch
    fi
}

# Until the TDK scripts change, we have to manually install in ${TDK_TARGETDIR}
do_install () {
    if [ "${TDK_IPK}" = "TRUE" ]; then
        install -d ${D}${TDK_INSTALLDIR}
        install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${S}/InstallTDK.sh ${D}${TDK_INSTALLDIR}
        install -m 0644 ${S}/git/tdk.service ${D}${systemd_unitdir}/system/tdk.service
    fi
}

SYSTEMD_SERVICE:${PN} = "${@bb.utils.contains('DISTRO_FEATURES','ENABLE_IPK','tdk.service','',d)}"

FILES:${PN} = "${@bb.utils.contains('DISTRO_FEATURES','ENABLE_IPK','${TDK_INSTALLDIR}/*','',d)}"

FILES:${PN} += "${@bb.utils.contains('DISTRO_FEATURES','ENABLE_IPK','${systemd_unitdir}/system/tdk.service','',d)}"
