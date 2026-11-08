SUMMARY = "EntOS Diagnostics - Read-only AI2.0 device diagnostic utility"
DESCRIPTION = "A C++17 utility for unified snapshots of AI2.0 device state. \
Collects platform metrics, process information, and application/service state \
from Thunder plugins with deterministic output, automatic credential redaction, \
and deadline-aware collection. Supports text and JSON output, optional YAML \
configuration, and remote Thunder endpoints."

HOMEPAGE = "https://github.com/Yogaraj97/entos-diagnostics"
BUGTRACKER = "https://github.com/Yogaraj97/entos-diagnostics/issues"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

SECTION = "rdk/diagnostics"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

# Version and revision
PV = "1.0.0"
PR = "r0"

# Source repository - GitHub public repository
SRC_URI = "git://github.com/Yogaraj97/entos-diagnostics.git;protocol=https;branch=main"
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

# Build inheritance for CMake
inherit cmake pkgconfig

# ============================================================================
# BUILD DEPENDENCIES (DEPENDS)
# ============================================================================
# These packages are required during build time and will be available in 
# the build sysroot via the OpenEmbedded/Yocto build system.

DEPENDS = " \
    cmake-native \
"

# Core C++ and system libraries (required)
DEPENDS += " \
    curl \
    jsoncpp \
"

# Optional YAML configuration support
# Comment out to disable YAML support and reduce dependencies
DEPENDS += " \
    yaml-cpp \
"

# ============================================================================
# RUNTIME DEPENDENCIES (RDEPENDS)
# ============================================================================
# Packages required when the binary is executed on the target device.
# The build system will ensure these are available in the target rootfs.

RDEPENDS:${PN} = " \
    curl \
    jsoncpp \
    yaml-cpp \
    libgcc \
    libstdc++ \
"

# Reduce unnecessary dependencies for dev/debug packages
RDEPENDS:${PN}-dev = ""
RDEPENDS:${PN}-dbg = ""

# ============================================================================
# CMAKE CONFIGURATION
# ============================================================================
# Flags passed to CMake during configuration.

EXTRA_OECMAKE = " \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_INSTALL_PREFIX=/usr \
    -DBUILD_TESTING=OFF \
    -DENTOS_ENABLE_YAML=ON \
    -DCMAKE_EXPORT_COMPILE_COMMANDS=OFF \
"

# ============================================================================
# COMPILER FLAGS
# ============================================================================
# Optimization and warning flags for security and quality.

TARGET_CFLAGS:append = " -Wall -Wextra -Wpedantic"
TARGET_CXXFLAGS:append = " -Wall -Wextra -Wpedantic"

# ============================================================================
# INSTALLATION
# ============================================================================
# Install the executable, configuration files, and documentation.

do_install() {
    # Install main executable
    install -d ${D}${bindir}
    install -m 0755 ${B}/entos-diagnostics ${D}${bindir}/
    
    # Install configuration templates to sysconfdir
    install -d ${D}${sysconfdir}/entos-diagnostics
    if [ -f ${S}/config/providers.json ]; then
        install -m 0644 ${S}/config/providers.json ${D}${sysconfdir}/entos-diagnostics/
    fi
    
    # Install YAML configuration templates (if present)
    if [ -d ${S}/config ]; then
        for yaml_file in ${S}/config/*.yaml; do
            if [ -f "$yaml_file" ]; then
                install -m 0644 "$yaml_file" ${D}${sysconfdir}/entos-diagnostics/
            fi
        done
    fi
    
    # Install schemas and profiles to data directory
    install -d ${D}${datadir}/entos-diagnostics
    if [ -d ${S}/config ]; then
        for json_file in ${S}/config/*.json; do
            if [ -f "$json_file" ]; then
                install -m 0644 "$json_file" ${D}${datadir}/entos-diagnostics/
            fi
        done
        
        for schema_file in ${S}/config/*.schema; do
            if [ -f "$schema_file" ]; then
                install -m 0644 "$schema_file" ${D}${datadir}/entos-diagnostics/
            fi
        done
    fi
    
    # Install documentation
    install -d ${D}${docdir}/entos-diagnostics
    
    # Main README
    if [ -f ${S}/README.md ]; then
        install -m 0644 ${S}/README.md ${D}${docdir}/entos-diagnostics/
    fi
    
    # OpenSpec design and workflow documentation
    if [ -d ${S}/openspec/changes/add-unified-diagnostics ]; then
        for doc_file in ${S}/openspec/changes/add-unified-diagnostics/*.md; do
            if [ -f "$doc_file" ]; then
                install -m 0644 "$doc_file" ${D}${docdir}/entos-diagnostics/
            fi
        done
    fi
    
    # LICENSE file
    if [ -f ${S}/LICENSE ]; then
        install -m 0644 ${S}/LICENSE ${D}${docdir}/entos-diagnostics/
    fi
}

# ============================================================================
# PACKAGE CONFIGURATION
# ============================================================================
# Define what files go into which package and metadata.

PACKAGES = "${PN} ${PN}-dev ${PN}-doc ${PN}-dbg"

# Main binary package
FILES:${PN} = " \
    ${bindir}/entos-diagnostics \
    ${sysconfdir}/entos-diagnostics/* \
    ${datadir}/entos-diagnostics/* \
"

# Development package (if library files are installed)
FILES:${PN}-dev = " \
    ${includedir} \
    ${libdir}/*.a \
    ${libdir}/*.la \
    ${libdir}/pkgconfig \
"

# Documentation package
FILES:${PN}-doc = " \
    ${docdir}/entos-diagnostics/* \
"

# ============================================================================
# CONFIGURATION FILE HANDLING
# ============================================================================
# Mark configuration files so they are preserved on package updates.

CONFFILES:${PN} = " \
    ${sysconfdir}/entos-diagnostics/providers.json \
"

# ============================================================================
# PACKAGE SUMMARIES AND DESCRIPTIONS
# ============================================================================

SUMMARY:${PN} = "EntOS Diagnostics - Device operational snapshot utility"

SUMMARY:${PN}-dev = "EntOS Diagnostics - Development files (headers, static libs)"

SUMMARY:${PN}-doc = "EntOS Diagnostics - Documentation and design guides"

DESCRIPTION:${PN}-doc = "Comprehensive documentation for EntOS Diagnostics \
including architecture guides, deployment procedures, OpenSpec workflow, \
implementation tasks, and API contracts."

# ============================================================================
# QA AND SECURITY SETTINGS
# ============================================================================

# Skip certain QA checks if needed
INSANE_SKIP:${PN} = "already-stripped"

# Security and reproducibility
SECURITY_STRICT_PERMISSIONS = "1"

# Compatibility
COMPATIBLE_HOST = ".*-linux"
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

# ============================================================================
# BUILD VARIANTS (Optional)
# ============================================================================
# Uncomment to add build variants for different configurations.

# Support for conditional YAML compilation
# To disable YAML: set ENTOS_DISABLE_YAML = "1" in distro config
EXTRA_OECMAKE:append = " \
    ${@'-DENTOS_ENABLE_YAML=OFF' if d.getVar('ENTOS_DISABLE_YAML') == '1' else '-DENTOS_ENABLE_YAML=ON'} \
"

# ============================================================================
# POST-INSTALL HOOKS (Optional)
# ============================================================================
# Uncomment if runtime configuration is needed after installation.

# pkg_postinst:${PN}() {
#     # Verify executable is accessible
#     if [ -x /usr/bin/entos-diagnostics ]; then
#         /usr/bin/entos-diagnostics list > /dev/null 2>&1 || echo "Warning: entos-diagnostics initial check failed"
#     fi
# }

# ============================================================================
# TEST EXECUTION (Optional)
# ============================================================================
# Enable host-side testing during build if needed.

# do_compile:append() {
#     if ${@bb.utils.contains('DISTRO_FEATURES', 'tests', 'true', 'false', d)}; then
#         cmake --build ${B} -- test
#     fi
# }

# ============================================================================
# VERSION INFORMATION
# ============================================================================
# Keep track of component version for package management.

PV = "1.0.0"
PR = "r0"

# For development/rolling builds, use:
# PV = "1.0+git${SRCPV}"

# ============================================================================
# DEPLOYMENT NOTES
# ============================================================================
# 
# Usage in Yocto build:
#
#   1. Add this recipe to your Yocto layer (recipes-extended/entos/)
#   2. Include in your image via packagegroup or direct dependency
#   3. Build: bitbake entos-diagnostics
#   4. Optional: Add to rootfs image via IMAGE_INSTALL variable
#
# Dependencies required in sysroot:
#   - curl (libcurl development package)
#   - jsoncpp (JsonCpp development package)
#   - yaml-cpp (optional, for YAML configuration support)
#
# On target device:
#   $ /usr/bin/entos-diagnostics list
#   $ /usr/bin/entos-diagnostics plugins
#   $ /usr/bin/entos-diagnostics dumpsys --format json
#   $ /usr/bin/entos-diagnostics dumpsys --section platform --url http://127.0.0.1:9998
#
# ============================================================================
