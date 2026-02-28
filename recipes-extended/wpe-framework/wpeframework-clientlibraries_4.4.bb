SUMMARY = "WPEFramework client libraries"
LICENSE = "Apache-2.0"
HOMEPAGE = "https://github.com/rdkcentral/ThunderClientlibraries"
LIC_FILES_CHKSUM = "file://LICENSE;md5=847677038847363222ffb66cfa6406c2"

PR = "r20"
PV = "4.4.2"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

inherit python3native cmake pkgconfig

SRC_URI = "git://github.com/rdkcentral/ThunderClientLibraries.git;protocol=https;branch=R4_4;name=wpeframework-clientlibraries \
           file://r4.4/Library-version-Matched-With-Release-ClientLibs.patch \
           file://r4.4/0004-R4-Security-Agent-Library-NameChange.patch \
           file://r4.4/0001-Add-functionality-to-construct-Session-private-data.patch \
           file://r4.4/0001-RDK-OCDM-adapter.patch \
           file://r4.4/0003_MediaType_name_changed.patch \
           file://r4.4/0001-OCDM-enhancement-for-ocdm-adapter.patch \
           file://r4.4/0004-Cipher-CipherNetflix-methods-return-type-changes.patch \
           file://r4.4/R4.2_compilation_error_assert.patch \
           file://r4.4/0001-Cryptography-CipherNetflix.r4.4.patch \
           file://r4.4/0003-trace_l2-BuildError-fix.patch \
           file://r4.4/0001-reconnect-if-connection-establishment-is-failed.patch \
           file://r4.4/0001-check-_session-has-a-valid-pointer.patch \
           file://r4.4/0001-add-svp-header-to-data-before-decryption.patch \
           file://r4.4/RDK-55149.patch \
           file://r4.4/0001-Add-vault-platform-case.patch \
           file://0001-error-handling-if-invalid-external-input.patch \
           file://r4.4/0001-Implement-IPersistent-interface-for-RPC-Vault.patch \
           file://r4.4/0001-SecAPI-Re-acquire-sec-handle-after-flush.patch \
           file://r4.4/0001-DELIA-64727-Prealloc-secure-memory-before-decrypt.patch \
           file://r4.4/0001-RDKEMW-7064-Dont-decrypt-fake-buffer-is-revoke-has-b.patch \
           file://r4.4/0001-PowerManagerClient-library-implementation.patch \
           file://r4.4/0001-RDKEMW-13372-Support-for-additional-clear-data-after.patch \
           "

# Oct 17, 2023
SRCREV_wpeframework-clientlibraries = "09a75a85e1263e0520f182dea6dc19c673e070a1"

# ----------------------------------------------------------------------------

S = "${WORKDIR}/git"
TOOLCHAIN = "gcc"

require recipes-extended/entservices/include/compositor.inc
#include include/compositor.inc

DEPENDS = " \
    entservices-apis \
    wpeframework-tools-native \
    ${@bb.utils.contains('DISTRO_FEATURES', 'compositor', '${WPE_COMPOSITOR_DEP}', '', d)} \
    gstreamer1.0 \
"

RDEPENDS:${PN}:append:dunfell = "${@bb.utils.contains('DISTRO_FEATURES', 'sage_svp', ' gst-svp-ext', '', d)}"
RDEPENDS:${PN}:append:dunfell = "${@bb.utils.contains('DISTRO_FEATURES', 'rdk_svp', ' gst-svp-ext', '', d)}"
RDEPENDS:${PN}:append:dunfell = " wpeframework rdkperf"

#Cryptography library
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_icrypto_openssl','openssl', 'virtual/vendor-secapi2-adapter', d)}"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'enable_icrypto_openssl','', 'virtual/vendor-secapi-netflix', d)}"
DEPENDS +=  "${@bb.utils.contains('DISTRO_FEATURES', 'enable_icrypto_openssl',"", bb.utils.contains('DISTRO_FEATURES', 'netflix_cryptanium', 'virtual/vendor-secapi-crypto', "", d), d)}"
CRYPTOGRAPHY_IMPLEMENTATION = "${@bb.utils.contains('DISTRO_FEATURES', 'enable_icrypto_openssl','OpenSSL', 'SecApi', d)}"


def get_cdmi_adapter(d):
    if bb.utils.contains("DISTRO_FEATURES", "rdk_svp", "true", "false", d) == "true":
        return "opencdmi_rdk_svp"
    else:
        return "opencdm_gst"
    fi

WPE_CDMI_ADAPTER_IMPL = "${@get_cdmi_adapter(d)}"

PACKAGECONFIG ?= " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'opencdm', 'opencdm ${WPE_CDMI_ADAPTER_IMPL}', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'provisioning', 'provisionproxy', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_security_disable', '', 'securityagent', d)} \
    cryptography \
    powercontroller \
    "

PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'compositor', 'compositorclient', '', d)}"

PACKAGECONFIG[compositorclient] = "-DCOMPOSITORCLIENT=ON,-DCOMPOSITORCLIENT=OFF"
PACKAGECONFIG[provisionproxy]   = "-DPROVISIONPROXY=ON,-DPROVISIONPROXY=OFF,libprovision"
PACKAGECONFIG[securityagent]    = "-DSECURITYAGENT=ON, -DSECURITYAGENT=OFF"
PACKAGECONFIG[cryptography]     = "-DCRYPTOGRAPHY=ON, -DCRYPTOGRAPHY=OFF,"
PACKAGECONFIG[powercontroller]     = "-DPOWERCONTROLLER=ON, -DPOWERCONTROLLER=OFF,"

# OCDM
PACKAGECONFIG[opencdm]          = "-DCDMI=ON,-DCDMI=OFF,"
PACKAGECONFIG[opencdm_gst]      = '-DCDMI_ADAPTER_IMPLEMENTATION="gstreamer",,gstreamer1.0'
PACKAGECONFIG[opencdmi_rdk_svp]= '-DCDMI_ADAPTER_IMPLEMENTATION="rdk",,'

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

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/*.so ${datadir}/WPEFramework/* ${PKG_CONFIG_DIR}/*.pc"
FILES:${PN}-dev += "${libdir}/cmake/*"

INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN}-dbg += "dev-so"

# ----------------------------------------------------------------------------

RDEPENDS:${PN}:append:rpi = " ${@bb.utils.contains('MACHINE_FEATURES', 'vc4graphics', '', 'userland', d)}"

CXXFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'netflix_cryptanium', " -I${STAGING_INCDIR}/secapi-crypto/sec_api/headers", "", d)}"

# Avoid settings ADNEEDED in LDFLAGS as this can cause the libcompositor.so to drop linking to libEGL/libGLES
# which might not be needed at first glance but will cause problems higher up in the change, there for lets drop -Wl,--as-needed
# some distros, like POKY (morty) enable --as-needed by default (e.g. https://git.yoctoproject.org/cgit/cgit.cgi/poky/tree/meta/conf/distro/include/as-needed.inc?h=morty)
ASNEEDED = ""
