SUMMARY = "Generic RDK MoCA HAL"
SECTION = "console/utils"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"
PV = "${RDK_RELEASE}+git${SRCPV}"
PROVIDES = "virtual/moca-hal moca-hal-tools moca-hal-headers"
RPROVIDES:${PN} = "virtual/moca-hal moca-hal-tools moca-hal-headers"

SRC_URI = "${CMF_GIT_ROOT}/rdk/components/generic/mocahal;protocol=${CMF_GIT_PROTOCOL};branch=${CMF_GIT_BRANCH}"
SRCREV = "${AUTOREV}"
S = "${WORKDIR}/git"
DEPENDS="systemd rfc "
inherit autotools systemd pkgconfig coverity syslog-ng-config-gen logrotate_config
SYSLOG-NG_FILTER = "moca-status"
SYSLOG-NG_SERVICE_moca-status = "moca-status.service"
SYSLOG-NG_DESTINATION_moca-status = "mocaStatus.log"
SYSLOG-NG_LOGRATE_moca-status = "medium"

LOGROTATE_NAME="mocastatus"
LOGROTATE_LOGNAME_mocastatus="mocaStatus.log"
#HDD_DISABLE
LOGROTATE_SIZE_MEM_mocastatus="512000"
LOGROTATE_ROTATION_MEM_mocastatus="3"
#HDD_ENABLE
LOGROTATE_SIZE_mocastatus="1048576"
LOGROTATE_ROTATION_mocastatus="4"

SYSTEMD_SERVICE:${PN} += "moca-status.service"
FILES:${PN} += "${systemd_unitdir}/system/moca-status.service"

RMH_START_DEFAULT_SINGLE_CHANEL ?= "1150"
RMH_START_DEFAULT_POWER_REDUCTION ?= "9"
RMH_START_DEFAULT_TABOO_START_CHANNEL ?= "41"
RMH_START_DEFAULT_TABOO_MASK ?= "0x00FBFFFF"
RMH_START_SET_MAC_FROM_PROC ?= "false"

# Set preferred NC in two steps to allow RMH_START_DEFAULT_PREFERRED_NC to be already set when we get here. Otherwise _client and _hybrid will override previous value.
local_RMH_START_DEFAULT_PREFERRED_NC_client ?= "false"
local_RMH_START_DEFAULT_PREFERRED_NC_hybrid ?= "true"
local_RMH_START_DEFAULT_PREFERRED_NC ?= "false"
RMH_START_DEFAULT_PREFERRED_NC ?= "${local_RMH_START_DEFAULT_PREFERRED_NC}"

CFLAGS:append = " -Wall -Wno-unused-function\
 -DMACHINE_NAME='"${MACHINE}"'\
 -DRMH_START_DEFAULT_SINGLE_CHANEL=${RMH_START_DEFAULT_SINGLE_CHANEL}\
 -DRMH_START_DEFAULT_POWER_REDUCTION=${RMH_START_DEFAULT_POWER_REDUCTION}\
 -DRMH_START_DEFAULT_TABOO_START_CHANNEL=${RMH_START_DEFAULT_TABOO_START_CHANNEL}\
 -DRMH_START_DEFAULT_TABOO_MASK=${RMH_START_DEFAULT_TABOO_MASK}\
 -DRMH_START_DEFAULT_PREFERRED_NC=${RMH_START_DEFAULT_PREFERRED_NC}\
 -DRMH_START_SET_MAC_FROM_PROC=${RMH_START_SET_MAC_FROM_PROC}\
"

do_generate_rmh_soc_header() {
    cp ${S}/rmh_interface/soc_header/rmh_soc_header.h ${S}/rmh_interface/rmh_soc.h
    (
      echo " "
      echo "/**********************************************"
      echo "* ====== THIS HEADER WAS AUTO GENERATED ======"
      echo "* Date: $(date)"
      echo "* Branch: ${RDK_GIT_BRANCH}"
      echo "* Revision: ${PV}"
      echo "**********************************************/"
      echo '#include "rmh_type.h"'
      echo " "
      echo '#ifdef __cplusplus'
      echo 'extern "C" {'
      echo '#endif'
      echo " "
      gcc -E -P ${S}/rmh_interface/soc_header/rmh_generate_soc_header.c -I${S}/rmh_interface -I${S}/rmh_lib
      echo " "
      echo '#ifdef __cplusplus'
      echo '}'
      echo '#endif'
      echo " "
    ) >> ${S}/rmh_interface/rmh_soc.h

    # Cleanup empty lines from gcc. We could do this directly on the output of gcc but then we have to add a pipefail to catch potential
    # failures. Also if we use pipefail bitbake will show the error in sed rather than gcc. So for now we will just delete them all at
    # the end and if you want a blank line, add a space.
    sed -i '/^$/d' ${S}/rmh_interface/rmh_soc.h
}
addtask generate_rmh_soc_header after do_patch before do_compile


do_generate_rmh_documentation() {
    ${S}/rmh_interface/html_documentation/rmh_generate_html_doc.sh "${S}" "${PV}" "${RDK_GIT_BRANCH}" > ${S}/rmh_interface/html_documentation/rmh_api.html
}
addtask do_generate_rmh_documentation after do_patch before do_compile

do_install:append () {
    install -d ${D}/usr/share/doc
    install -m 0644 ${S}/rmh_interface/html_documentation/rmh_api.html ${D}/usr/share/doc
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${S}/rmh_scripts/moca-status.service ${D}${systemd_unitdir}/system
}
