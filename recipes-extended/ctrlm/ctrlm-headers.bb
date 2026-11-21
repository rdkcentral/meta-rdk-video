DESCRIPTION = "Control Manager Headers"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SECTION = "base"
DEPENDS = ""

include ctrlm.inc

PACKAGE_ARCH   = "${MIDDLEWARE_ARCH}"
PV            := "${CTRLM_PV}"
PR            := "${CTRLM_PR}"
SRCREV        := "${CTRLM_SRCREV}"
SRCREV_FORMAT  = "ctrlm-headers"

SRC_URI = "${CMF_GITHUB_ROOT}/control;${CMF_GITHUB_SRC_URI_SUFFIX};name=ctrlm-headers"

S = "${WORKDIR}/git"

inherit cmake

EXTRA_OECMAKE:append = " -DCMAKE_PROJECT_VERSION=${PV} -DINSTALL_HEADERS=ON"

# Header list is maintained in the control CMakeLists.txt; only run the headers install target
OECMAKE_TARGET_INSTALL = "install_headers"

do_compile[noexec] = "1"

ALLOW_EMPTY:${PN} = "1"
