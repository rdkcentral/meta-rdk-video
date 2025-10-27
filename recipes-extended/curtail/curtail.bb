SUMMARY = "curtail is a program that reads stdin and writes to a fixed size file."
DESCRIPTION = "Use curtail as a standalone program or a library to write to a maximum sized file."
SECTION = "console/utils"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

include curtail.inc

SRC_URI = "git://github.com/Comcast/Infinite-File-Curtailer;protocol=https;nobranch=1;name=curtail"
PV := "${CURTAIL_PV}"
PR := "${CURTAIL_PR}"
SRCREV_pn-curtail := "${CURTAIL_SRCREV}"
SRCREV_FORMAT = "curtail"

S = "${WORKDIR}/git"

inherit autotools pkgconfig
