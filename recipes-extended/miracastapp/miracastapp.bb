SUMMARY = "Miracast Application"

LICENSE = "CLOSED"

#DEPENDS += " mbedtls"
DEPENDS += " openssl"
DEPENDS += " mdns"
DEPENDS += " fdk-aac"
DEPENDS += " sqlite3"
DEPENDS += " alsa-lib"
DEPENDS += " libopus"
DEPENDS += " curl"
DEPENDS += " entservices-apis"
DEPENDS += " wpeframework wpeframework-clientlibraries"
DEPENDS += " essos"
DEPENDS += " qtbase"
DEPENDS += " rdk-gstreamer-utils"
DEPENDS += " rdkperf"
DEPENDS:append = " gstreamer1.0 gstreamer1.0-plugins-base secapi2-adapter virtual/vendor-rdk-gstreamer-utils-platform virtual/vendor-gst-drm-plugins"
DEPENDS += "${@bb.utils.contains('IMAGE_FEATURES', 'prodlog', 'asappsserviced-trials', '', d)}"
DEPENDS += "${@bb.utils.contains('IMAGE_FEATURES', 'prod', 'asappsserviced-native asappsserviced-release', 'asappsserviced-native asappsserviced-debug', d)}"
DEPENDS += " libsoup-2.4"
DEPENDS += " virtual/vendor-miracast-soc"
DEPENDS += " wpa-supplicant"

RDEPENDS:${PN} += " rdk-gstreamer-utils rdkperf virtual/vendor-miracast-soc wpa-supplicant"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-casting;${CMF_GITHUB_SRC_URI_SUFFIX};name=miracast-app"
SRC_URI += "file://MiracastAppPackage.json"

SRC_URI += "file://config.xml"
SRC_URI += "file://run-miracastapp.sh"
SRC_URI += "file://icon.png"
SRC_URI += "file://spinner.png"
SRC_URI += "file://bg.jpg"
CURR_DIR := "${THISDIR}"

MIRACASTAPP_WIDGET_VERSION ?= "1.0"

WIDGET_FILES_DIR = "${WORKDIR}/widget-files"
WIDGET_DIR = "${WORKDIR}/widget"

do_create_widget[cleandirs] = "${WIDGET_FILES_DIR} ${WIDGET_DIR}"

do_create_widget() {
    echo "Entered do_create widget"
    mkdir -p ${WIDGET_FILES_DIR}/${datadir}
    mkdir -p ${WIDGET_FILES_DIR}/${libdir}
    install -m 755 ${S}/build/skyMiracastApplication ${WIDGET_FILES_DIR}
    ${TARGET_PREFIX}strip --strip-unneeded --remove-section=.pdr --remove-section=.comment ${WIDGET_FILES_DIR}/skyMiracastApplication || true
    echo "Widget files format information:"
    file ${WIDGET_FILES_DIR}/${libdir}/*
    file ${WIDGET_FILES_DIR}/skyMiracastApplication
    install -m 755 ${WORKDIR}/run-miracastapp.sh ${WIDGET_FILES_DIR}/
    install -m 755 ${WORKDIR}/config.xml ${WIDGET_FILES_DIR}/
    install -m 755 ${WORKDIR}/icon.png ${WIDGET_FILES_DIR}/
    install -m 755 ${WORKDIR}/spinner.png ${WIDGET_FILES_DIR}/
    install -m 755 ${WORKDIR}/bg.jpg ${WIDGET_FILES_DIR}/
    file ${STAGING_DIR_NATIVE}${bindir}/create-sign-sky-app
    ${STAGING_DIR_NATIVE}${bindir}/create-sign-sky-app --skipvalid --indir ${WIDGET_FILES_DIR} --outwgt ${WIDGET_DIR}/package.wgt --pkcs12 ${STAGING_DIR_NATIVE}${datadir}/sky-debug-widget-cert.p12
    mkdir -p ${DEPLOY_DIR_IMAGE}/MiracastApp_widget
    cp ${WIDGET_DIR}/package.wgt ${DEPLOY_DIR_IMAGE}/MiracastApp_widget
}

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
SRCREV ?= "6fbcc7e97917d767887012de2943ad454385c4c7"

PV ?= "1.0"
PR ?= "r0"

S = "${WORKDIR}/git/Miracast/skyMiracastApplication"

# Set the crypto implementation for the build, choices are MBedTLS and OpenSSL
CRYPTO = "OpenSSL"

do_configure:append(){
    echo "configuring... to remove the build directory"
    rm -rf ${S}/build
}


addtask do_create_widget after do_install before do_package

EXTRA_OEMAKE += "STAGING_DIR=${STAGING_DIR_TARGET} \
                -C ${S} \
               "

DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OEMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', "USE_THUNDER_R4=1", "", d)}"

CXXFLAGS += "-I${STAGING_INCDIR}/WPEFramework"
CXXFLAGS += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', "-DUSE_THUNDER_R4=1", "", d)}"

EXTRA_OEMAKE += " SKY_BUILD=1"
CXXFLAGS += " -DSKY_BUILD=1"
CXXFLAGS += " -I${STAGING_INCDIR}/rdk/iarmbus -I${STAGING_INCDIR}/rdk/iarmmgrs-hal -I${STAGING_INCDIR}/rdk/ds -I${STAGING_INCDIR}/rdk/ds-hal -I${STAGING_INCDIR}/rdk/halif/ds-hal -I${STAGING_INCDIR}/rdk/ds-rpc -I${STAGING_INCDIR}/systemd"
CFLAGS += " -DSKY_BUILD=1"

#EXTRA_OEMAKE += "'CXX=${CXX}' 'CXXFLAGS=${CXXFLAGS} $(shell pkg-config --cflags Qt5Gui Qt5Widgets Qt5Core)' 'LDFLAGS=${LDFLAGS} $(shell pkg-config --libs Qt5Gui Qt5Widgets Qt5Core)'"


inherit autotools pkgconfig
FILES_SOLIBSDEV = ""
SOLIBS = ".so"
INSANE_SKIP:${PN} += "installed-vs-shipped"
