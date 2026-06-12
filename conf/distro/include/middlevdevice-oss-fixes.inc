# vdevice_x86.inc
# ============================================================================
# Fixes for vdevice incompatibilities with DISTRO=poky.
# included via machine.conf
# ============================================================================

# ============================================================================
# PREFERRED_VERSION overrides
# ============================================================================

PREFERRED_VERSION_dnsmasq = "2.90"
PREFERRED_VERSION_libarchive = "3.6.2%"
PREFERRED_VERSION_gmp = "6.2.1%"
PREFERRED_VERSION_gmp-native = "6.2.1%"
PREFERRED_VERSION_mpfr = "4.1.1%"
PREFERRED_VERSION_mpfr-native = "4.1.1%"
PREFERRED_VERSION_gettext = "0.21%"
PREFERRED_VERSION_gettext-native = "0.21%"

# ============================================================================
# BBMASK - Mask incompatible recipes/bbappends
# ============================================================================

# --- meta-rdk-video ---
BBMASK .= "|meta-rdk-video/recipes-extended/cobalt/libloader-app_"
BBMASK .= "|meta-rdk-video/recipes-extended/ctrlm/ctrlm-main.bb"
BBMASK .= "|meta-rdk-video/recipes-extended/xr-voice-sdk/xr-voice-sdk_1.0.bb"
BBMASK .= "|meta-rdk-video/recipes-extended/xupnp/xupnp_git.bbappend"

# --- meta-rdk-oss-reference: connectivity ---
BBMASK .= "|meta-rdk-oss-reference/recipes-connectivity/openssl/"
BBMASK .= "|meta-rdk-oss-reference/recipes-connectivity/bluez5/bluez5_5.66"

# --- meta-rdk-oss-reference: core ---
BBMASK .= "|meta-rdk-oss-reference/recipes-core/libxml2/libxml2_2.9.14.bbappend"
BBMASK .= "|meta-rdk-oss-reference/recipes-core/systemd/systemd_230.bb"

# --- meta-rdk-oss-reference: extended ---
BBMASK .= "|meta-rdk-oss-reference/recipes-extended/pam/libpam_1.5.2.bbappend"

# --- meta-rdk-oss-reference: graphics ---
BBMASK .= "|meta-rdk-oss-reference/recipes-graphics/drm/libdrm_2.4.100.bb"
BBMASK .= "|meta-rdk-oss-reference/recipes-graphics/cairo/cairo_1.16.0.bbappend"
BBMASK .= "|meta-rdk-oss-reference/recipes-graphics/vulkan/"
BBMASK .= "|meta-rdk-oss-reference/recipes-graphics/wayland/wayland_%.bbappend"
BBMASK .= "|meta-rdk-oss-reference/recipes-graphics/pango/"

# --- meta-rdk-oss-reference: orphaned bbappends ---
BBMASK .= "|meta-rdk-oss-reference/recipes-devtools/python/python3_3.10.15.bbappend"
BBMASK .= "|meta-rdk-oss-reference/recipes-support/fcgi/fcgi_%.bbappend"

# --- meta-rdk ---
BBMASK .= "|meta-rdk/recipes-core/eglibc/eglibc-locale_%.bbappend"

# --- meta-rdk-restricted ---
BBMASK .= "|meta-rdk-restricted/recipes-raspberrypi/mediaframework/rmfhalheaders_%.bbappend"

# --- meta-middleware ---
BBMASK .= "|meta-middleware-restricted-support/recipes-core/packagegroups/packagegroup-middleware-layer.bbappend"
BBMASK .= "|meta-middleware-generic-support/recipes-core/images/middleware-test-image.bb"

BBMASK .= "|meta-vendor-vdevice/recipes-multimedia/.*"
BBMASK .= "|meta-vendor-vdevice/recipes-media/.*"
# ============================================================================
# DISTRO_FEATURES adjustments
# ============================================================================

DISTRO_FEATURES:remove = "wifi"
DISTRO_FEATURES:append = " thunder_security_disable wpe_r4_4 enable_icrypto_openssl"

# ============================================================================
# Provider / HAL configuration
# ============================================================================

PREFERRED_PROVIDER:virtual/vendor-hdmicec-hal = "rdk-halif-aidl"

# ============================================================================
# RDK includes (versions and security flags)
# ============================================================================

require ${MANIFEST_PATH_META_RDK}/conf/distro/include/rdk-versions.inc
require ${MANIFEST_PATH_META_RDK}/conf/distro/include/rdk_security_flags.inc
require ${MANIFEST_PATH_RDK_HALIF_HEADERS}/conf/include/rdk-headers-versions.inc

PROVIDES:pn-rdk-halif-aidl += "virtual/vendor-hdmicec-hal" 

CFLAGS:append:pn-telemetry = " -Wno-unused-result -Wno-format-security"
CXXFLAGS:append:pn-telemetry = " -Wno-unused-result -Wno-format-security"

CFLAGS:remove:pn-telemetry = "-Werror -Werror=format-security"
CXXFLAGS:remove:pn-telemetry = "-Werror -Werror=format-security"

CFLAGS:append:pn-commonutilities = " -Wno-error"

CFLAGS:append:pn-webconfig-framework = " -Wno-stringop-truncation -Wno-error=stringop-truncation"
CXXFLAGS:append:pn-webconfig-framework = " -Wno-stringop-truncation -Wno-error=stringop-truncation"

CFLAGS:remove:pn-webconfig-framework = " -Werror"
CXXFLAGS:remove:pn-webconfig-framework = " -Werror"

# added this because in aidl provides was not added and for hdmicec aidl is the hal Provider

LOG_BACKTRACE = "${@bb.utils.contains('DISTRO_FEATURES', 'logbacktrace', 'y', 'n', d)}"

BACKTRACE_LDFLAGS ?= ""
BACKTRACE_DEPENDS ?= ""

BACKTRACE_LDFLAGS:pn-wpeframework = "-Wl,--no-as-needed -lbreakpadwrapper -Wl,--as-needed"
BACKTRACE_DEPENDS:pn-wpeframework = " breakpad-wrapper"

DEPENDS:append = " ${@LOG_BACKTRACE == "y" and BACKTRACE_DEPENDS or ""}"
LDFLAGS:append = " ${@LOG_BACKTRACE == "y" and BACKTRACE_LDFLAGS or ""}"

# ============================================================================
# Build fixes
# ============================================================================

RDKROOT = "${TOPDIR}/.."

INHERIT += "fix-host-contamination"
