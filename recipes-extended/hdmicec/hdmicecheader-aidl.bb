SUMMARY = "RDK HDMICEC AIDL headers and hal_aidl archive"

DESCRIPTION = "Stages vendor-generated AIDL C++ headers and libhal_aidl.a for middleware consumers"

SECTION = "console/utils"


LICENSE = "Apache-2.0"

LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"



PACKAGE_ARCH = "${MACHINE_ARCH}"


FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI = " \
    git://github.com/rdkcentral/rdk-halif-aidl-vcomponent-hdmicec.git;protocol=https;nobranch=1;name=hdmicec;destsuffix=git \
    git://github.com/rdkcentral/rdk-halif-aidl.git;protocol=https;nobranch=1;name=halifaidl;destsuffix=rdk-halif-aidl \
"

SRCREV_hdmicec = "0.2.0-rc1"
SRCREV_halifaidl = "0.13.1"
SRCREV_FORMAT = "hdmicec_halifaidl"

S = "${WORKDIR}/git"


COMPATIBLE_MACHINE = "(vdevice_x86-64-mw)"

# Prevent work directory cleanup so hdmicec can access C++ sources
RM_WORK_EXCLUDE += "hdmicecheader-aidl"



# Build the AIDL support library directly in this helper recipe.

DEPENDS = "ut-core libbinder ut-control"

HALIFAIDL_COMPONENT_NAME = "hdmicec"
HAL_AIDL_MODULES = "hdmicec"

inherit halif-aidl



# Header/artifact staging only.

do_patch[noexec] = "1"

do_configure[noexec] = "1"

# Allow halif-aidl class to build C++ sources
do_compile[noexec] = "1"

do_install() {

    AIDL_HEADER_ROOT="${HALIFAIDL_BUILD}/${AIDL_SRC_VERSION}/h/com"
    AIDL_LIB_FILE="${HALIFAIDL_BUILD}/lib/libhal_aidl.a"


    if [ ! -f "${AIDL_HEADER_ROOT}/rdk/hal/hdmicec/IHdmiCec.h" ]; then
        bbfatal "Missing IHdmiCec.h at ${AIDL_HEADER_ROOT}/rdk/hal/hdmicec"
    fi

    if [ ! -f "${AIDL_LIB_FILE}" ]; then
        bbfatal "Missing libhal_aidl.a at ${AIDL_LIB_FILE}"
    fi

    bbnote "Staging AIDL headers from ${AIDL_HEADER_ROOT}"
    bbnote "Staging AIDL lib from ${AIDL_LIB_FILE}"

    # Stage headers
    install -d ${D}${includedir}/com

    find "${AIDL_HEADER_ROOT}" -type f -name '*.h' | while read -r f; do

        relpath=${f#${AIDL_HEADER_ROOT}/}

        install -d "${D}${includedir}/com/$(dirname "${relpath}")"

        install -m 0644 "${f}" "${D}${includedir}/com/${relpath}"

    done

    # Stage library
    install -d ${D}${libdir}

    install -m 0644 "${AIDL_LIB_FILE}" "${D}${libdir}/libhal_aidl.a"

}

FILES:${PN} = "${includedir}/com/rdk/hal/ ${libdir}/libhal_aidl.a"



ALLOW_EMPTY:${PN} = "0"