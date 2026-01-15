SUMMARY = "RDK AAMP component"
SECTION = "console/utils"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=97dd37dbf35103376811825b038fc32b"

PV = "AAMP"
PR = "r0"

SRCREV_FORMAT = "aamp"
SRCREV_aamp = "b994f1bae145f5799254b6eb37a486ec98e5dc7f"

inherit pkgconfig

DEPENDS += "curl libdash libxml2 cjson iarmmgrs wpeframework readline"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'gstreamer1', 'gstreamer1.0  gstreamer1.0-plugins-base', 'gstreamer gst-plugins-base', d)}"
RDEPENDS_${PN} +=  "${@bb.utils.contains('DISTRO_FEATURES', 'rdk_svp', 'gst-svp-ext', '', d)}"
DEPENDS += " wpe-webkit"
DEPENDS += " wpeframework-clientlibraries"
RDEPENDS:${PN} += "devicesettings"
DEPENDS:append = " virtual/vendor-gst-drm-plugins essos "
NO_RECOMMENDATIONS = "1"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
#To be removed later, the AAMP_RELEASE_TAG_NAME is not using.
AAMP_RELEASE_TAG_NAME ?= "5.9.1.0"

SRC_URI = "${CMF_GITHUB_ROOT}/aamp;${CMF_GITHUB_SRC_URI_SUFFIX};name=aamp"

S = "${WORKDIR}/git"

DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'webkitbrowser-plugin', '${WPEWEBKIT}', '', d)}"

DEPENDS:append = " virtual/vendor-secapi2-adapter "

require aamp-common.inc
require aamp-artifacts-version.inc

PACKAGECONFIG:append = " playready widevine clearkey"

DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec', '-DCMAKE_GST_SUBTEC_ENABLED=1 ', '', d)}"
EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'wpe_security_util_disable', ' -DDISABLE_SECURITY_TOKEN=ON ', '', d)}"

EXTRA_OECMAKE += "  -DCMAKE_WPEFRAMEWORK_REQUIRED=1 "
EXTRA_OECMAKE += "  -DCMAKE_DS_EVENT_SUPPORTED=1 "

EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'rdk_svp', ' -DCMAKE_RDK_SVP=1 ', '', d)}"

EXTRA_OECMAKE += "${@bb.utils.contains('DISTRO_FEATURES', 'sec_manager', ' -DCMAKE_USE_SECMANAGER=1 ', '', d)}"
EXTRA_OECMAKE += " -DCMAKE_WPEWEBKIT_WATERMARK_JSBINDINGS=1 "

RDEPENDS:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec', 'packagegroup-subttxrend-app', '', d)}"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'subtec', 'closedcaption-hal-headers virtual/vendor-dvb virtual/vendor-closedcaption-hal', '', d)}"

#Ethan log is implemented by Dobby hence enabling it.
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_rialto', 'dobby', '', d)}"
PACKAGES = "${PN} ${PN}-dev ${PN}-dbg"

FILES:${PN} += "${libdir}/lib*.so"
FILES:${PN} += "${libdir}/aamp-cli"
FILES:${PN} += "${libdir}/aamp/lib*.so"
FILES:${PN} +="${libdir}/gstreamer-1.0/lib*.so"
FILES:${PN}-dbg +="${libdir}/gstreamer-1.0/.debug/*"

INSANE_SKIP:${PN} = "dev-so"
CXXFLAGS += "-DCMAKE_LIGHTTPD_AUTHSERVICE_DISABLE=1 -I${STAGING_DIR_TARGET}${includedir}/WPEFramework/ "

CXXFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'wpe_security_util_disable', '', ' -lWPEFrameworkSecurityUtil ', d)}"
EXTRA_OECMAKE += " -DCMAKE_LIGHTTPD_AUTHSERVICE_DISABLE=1 "

CXXFLAGS += " -DAAMP_BUILD_INFO=${AAMP_RELEASE_TAG_NAME}" 

#required for specific products but for now distro is available only for UK 
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_UK', ' -DENABLE_USE_SINGLE_PIPELINE=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_IT', ' -DENABLE_USE_SINGLE_PIPELINE=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_DE', ' -DENABLE_USE_SINGLE_PIPELINE=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_AU', ' -DENABLE_USE_SINGLE_PIPELINE=1', '', d)}"

# Enable PTS restamp feature
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_UK', ' -DENABLE_PTS_RESTAMP=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_IT', ' -DENABLE_PTS_RESTAMP=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_DE', ' -DENABLE_PTS_RESTAMP=1', '', d)}"
CXXFLAGS:append = "${@bb.utils.contains('DISTRO_FEATURES', 'RDKE_REGION_AU', ' -DENABLE_PTS_RESTAMP=1', '', d)}"

INCLUDE_DIRS = " \
    -I=${includedir}/rdk/halif/ds-hal \
    "

do_install:append() {
    echo "Installing aamp-cli..."
    install -m755 ${B}/aamp-cli ${D}${libdir}

    # remove the static library if it is installed, 
    # CMakelist in aamp code installing static lib below line should avoid build error 
    rm -f ${D}${libdir}/libtsb.a
}

# Directory for deploying artifacts
DEPLOY_DIR_WGT = "${DEPLOY_DIR}/widgets"
ARTIFACT_FILES_DIR = "${WORKDIR}/artifact-files"
ARTIFACT_DIR = "${WORKDIR}/artifacts"
ARTIFACT_NAME = "AAMP_${AAMP_ARTIFACTS_VERSION}.tgz"

do_create_artifacts[cleandirs] = "${ARTIFACT_FILES_DIR} ${ARTIFACT_DIR}"
do_create_artifacts[vardepsexclude] += "DATETIME"

do_create_artifacts() {
    if [ "${PLATFORM_PATH}" = "unknown" ]; then
        echo "Skipping artifact creation for unknown platform [MACHINE=${MACHINE}]"
        return 0
    fi

    # Create all required directories
    mkdir -p ${ARTIFACT_FILES_DIR}/${libdir}
    mkdir -p ${ARTIFACT_FILES_DIR}/${libdir}/gstreamer-1.0

    # List what's in the install directory to help with debugging
    echo "Listing files installed by this recipe:"
    ls -la ${D}${libdir}/

    # Check if gstreamer-1.0 directory exists and list its contents
    if [ -d "${D}${libdir}/gstreamer-1.0" ]; then
        echo "Listing gstreamer-1.0 files:"
        ls -la ${D}${libdir}/gstreamer-1.0/
    fi

    # Create artifacts.info file with build information
    ARTIFACT_INFO_FILE="${ARTIFACT_FILES_DIR}/artifacts.info"
    echo "Generating ${ARTIFACT_INFO_FILE}"
    touch "${ARTIFACT_INFO_FILE}"
    echo "DATE=${DATETIME}" > ${ARTIFACT_INFO_FILE}
    echo "OS_TYPE=${OS_TYPE}" >> ${ARTIFACT_INFO_FILE}
    echo "PLATFORM=${PLATFORM_PATH}" >> ${ARTIFACT_INFO_FILE}
    echo "RDK_BRANCH=${PROJECT_BRANCH}" >> ${ARTIFACT_INFO_FILE}
    echo "WIDGET_VERSION_PREFIX=${WIDGET_VERSION_PREFIX}" >> ${ARTIFACT_INFO_FILE}
    echo "YOCTO_VERSION=${@get_yocto_code(d)}" >> ${ARTIFACT_INFO_FILE}
    echo "AAMP_BRANCH=${AAMP_RELEASE_TAG_NAME}" >> ${ARTIFACT_INFO_FILE}

    # Get the actual Git commit hash instead of AUTOREV
    if [ -d "${S}/.git" ]; then
        ACTUAL_REVISION=$(cd ${S} && git rev-parse HEAD)
        echo "AAMP_SRC_REV=$ACTUAL_REVISION" >> ${ARTIFACT_INFO_FILE}
    else
        echo "AAMP_SRC_REV=${SRCREV_aamp} (from recipe)" >> ${ARTIFACT_INFO_FILE}
    fi

    # Copy binaries from the recipe's install directory with verbose output and error handling
    echo "Copying .so files from ${D}${libdir}/ to ${ARTIFACT_FILES_DIR}/${libdir}/"
    cp -Lv ${D}${libdir}/*.so ${ARTIFACT_FILES_DIR}/${libdir}/ 2>/dev/null || echo "No .so files in ${D}${libdir}/"

    if [ -d "${D}${libdir}/gstreamer-1.0" ]; then
        echo "Copying .so files from ${D}${libdir}/gstreamer-1.0/ to ${ARTIFACT_FILES_DIR}/${libdir}/gstreamer-1.0/"
        cp -Lv ${D}${libdir}/gstreamer-1.0/*.so ${ARTIFACT_FILES_DIR}/${libdir}/gstreamer-1.0/ 2>/dev/null || echo "No .so files in ${D}${libdir}/gstreamer-1.0/"
    fi

    # Strip all binaries in the artifact-files directory
    echo "Stripping binaries in artifact-files directory..."
    find ${ARTIFACT_FILES_DIR} -type f -name "*.so" | xargs ${STRIP} --strip-all 2>/dev/null || true

    echo "Artifact files structure:"
    find ${ARTIFACT_FILES_DIR} -type f | sort

    # Package into ARTIFACT_NAME
    echo "Packaging artifacts into ${ARTIFACT_DIR}/${ARTIFACT_NAME}"
    tar -cvzf ${ARTIFACT_DIR}/${ARTIFACT_NAME} -C ${ARTIFACT_FILES_DIR} .

    # Deploy ARTIFACT_NAME
    mkdir -p ${DEPLOY_DIR_IMAGE}/AAMP_artifacts
    cp ${ARTIFACT_DIR}/${ARTIFACT_NAME} ${DEPLOY_DIR_IMAGE}/AAMP_artifacts/
}

do_deploy_artifacts() {
    if [ -f ${ARTIFACT_DIR}/${ARTIFACT_NAME} ]; then
        mkdir -p ${DEPLOY_DIR_WGT}/AAMP_artifacts
        cp -v ${ARTIFACT_DIR}/${ARTIFACT_NAME} ${DEPLOY_DIR_WGT}/AAMP_artifacts/
        echo "Copied ${ARTIFACT_DIR}/${ARTIFACT_NAME} to ${DEPLOY_DIR_WGT}/AAMP_artifacts"
    else
        echo "Artifact not present! Skipping this operation [MACHINE=${MACHINE}]."
    fi
}

addtask do_create_artifacts after do_install before do_package
addtask do_deploy_artifacts after do_create_artifacts before do_package
