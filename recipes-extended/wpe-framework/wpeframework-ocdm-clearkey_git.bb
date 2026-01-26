SUMMARY = "WPE Framework OpenCDMi module for clearkey"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ef252d84bf4d7b45388c93f6e5691f3d"

DEPENDS += "entservices-apis openssl"

require include/wpeframework-plugins.inc

SRC_URI = "git://git@github.com/rdkcentral/OCDM-Clearkey.git;protocol=https;branch=R2"

# May 21, 2021
SRCREV = "ddda85542097e630dd5d8d67c8a1a76cee8bf256"

SRC_URI += "file://0001-DTM-4265-ocdm-clearkey-for-fairplay-support.patch \
        "

FILES:${PN} = "${datadir}/WPEFramework/OCDM/*.drm"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"


