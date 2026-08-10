SUMMARY = "Thunder client libraries"
DESCRIPTION = "Thunder client libraries component"
HOMEPAGE = "https://github.com/rdkcentral/ThunderClientLibraries"
SECTION = "thunder"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=847677038847363222ffb66cfa6406c2"

require recipes-extended/entservices/include/compositor.inc

DEPENDS = " \
    entservices-apis \
    thunder-tools-native \
    ${@bb.utils.contains('DISTRO_FEATURES', 'compositor', '${WPE_COMPOSITOR_DEP}', '', d)} \
    gstreamer1.0 \
"

# Cryptography library
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_icrypto_openssl','openssl', 'virtual/vendor-secapi2-adapter', d)}"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_icrypto_openssl','', 'virtual/vendor-secapi-netflix', d)}"
DEPENDS +=  "${@bb.utils.contains('DISTRO_FEATURES', 'enable_icrypto_openssl',"", bb.utils.contains('DISTRO_FEATURES', 'netflix_cryptanium', 'virtual/vendor-secapi-crypto', "", d), d)}"
CRYPTOGRAPHY_IMPLEMENTATION = "${@bb.utils.contains('DISTRO_FEATURES', 'enable_icrypto_openssl','OpenSSL', 'SecApi', d)}"

SRC_URI = "${CMF_GITHUB_ROOT}/ThunderClientLibraries;protocol=https;nobranch=1"

SRCREV ?= "R${PV}"

S = "${WORKDIR}/git"
TOOLCHAIN = "gcc"

inherit python3native cmake pkgconfig

PACKAGECONFIG ?= " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'provisioning', 'provisionproxy', '', d)} \
    securityagent \
    powercontroller \
    "

PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'compositor', 'compositorclient', '', d)}"

PACKAGECONFIG[compositorclient] = "-DCOMPOSITORCLIENT=ON,-DCOMPOSITORCLIENT=OFF"
PACKAGECONFIG[provisionproxy]   = "-DPROVISIONPROXY=ON,-DPROVISIONPROXY=OFF,libprovision"
PACKAGECONFIG[securityagent]    = "-DSECURITYAGENT=ON, -DSECURITYAGENT=OFF"
PACKAGECONFIG[cryptography]     = "-DCRYPTOGRAPHY=ON, -DCRYPTOGRAPHY=OFF,"
PACKAGECONFIG[powercontroller]     = "-DPOWERCONTROLLER=ON, -DPOWERCONTROLLER=OFF,"

# OCDM
# Note: In RDK OCDM client build built by entservices-opencdmi instead.
#
# def get_cdmi_adapter(d):
#     if bb.utils.contains("DISTRO_FEATURES", "rdk_svp", "true", "false", d) == "true":
#         return "opencdmi_rdk_svp"
#     else:
#         return "opencdm_gst"
#     fi
# 
# WPE_CDMI_ADAPTER_IMPL = "${@get_cdmi_adapter(d)}"
# 
# PACKAGECONFIG += "${@bb.utils.contains('DISTRO_FEATURES', 'opencdm', 'opencdm ${WPE_CDMI_ADAPTER_IMPL}', '', d)} \
# 
# PACKAGECONFIG[opencdm]          = "-DCDMI=ON,-DCDMI=OFF,"
# PACKAGECONFIG[opencdm_gst]      = '-DCDMI_ADAPTER_IMPLEMENTATION="gstreamer",,gstreamer1.0'
# PACKAGECONFIG[opencdmi_rdk_svp] = '-DCDMI_ADAPTER_IMPLEMENTATION="rdk",,gst-svp-ext''


# ----------------------------------------------------------------------------

EXTRA_OECMAKE += " \
    -DBUILD_SHARED_LIBS=ON \
    -DBUILD_REFERENCE=${SRCREV} \
    -DPLUGIN_COMPOSITOR_IMPLEMENTATION=${WPE_COMPOSITOR_IMPL} \
    -DPLUGIN_COMPOSITOR_SUB_IMPLEMENTATION=${WPE_COMPOSITOR_SUB_IMPL} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'netflix_cryptanium', '-DSECAPI_ENGINE_CRYPTANIUM=1', '', d)} \
    -DCRYPTOGRAPHY_IMPLEMENTATION=${CRYPTOGRAPHY_IMPLEMENTATION}\
    ${@bb.utils.contains('DISTRO_FEATURES', 'enable_firebolt_compliance_tdk', '-DBUILD_CRYPTOGRAPHY_TESTS=ON', '', d)} \
    -DCMAKE_SYSROOT=${STAGING_DIR_HOST} \
"

CXXFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'netflix_cryptanium', " -I${STAGING_INCDIR}/secapi-crypto/sec_api/headers", "", d)}"

# Avoid setting --as-needed in LDFLAGS: it can cause libcompositor.so to
# drop linking to libEGL/libGLES on distros that enable it by default.
ASNEEDED = ""

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so ${datadir}/Thunder/* ${PKG_CONFIG_DIR}/*.pc"
FILES:${PN}-dev += "${libdir}/cmake/*"

INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"
