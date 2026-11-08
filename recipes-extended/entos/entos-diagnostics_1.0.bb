SUMMARY = "EntOS Diagnostics - Read-only AI2.0 device diagnostic utility"
DESCRIPTION = "A C++17 utility for unified snapshots of AI2.0 device state. \
Collects platform metrics, process information, and application/service state \
from Thunder plugins with deterministic output, automatic credential redaction, \
and deadline-aware collection. Supports text and JSON output, optional YAML \
configuration, and remote Thunder endpoints."

HOMEPAGE = "https://github.com/Yogaraj97/entos-diagnostics"
BUGTRACKER = "https://github.com/Yogaraj97/entos-diagnostics/issues"

LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

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
# DEPLOYMENT NOTES - COMPREHENSIVE USAGE GUIDE
# ============================================================================
#
# YOCTO BUILD INTEGRATION:
# ──────────────────────────────────────────────────────────────────────────
#
#   1. Add this recipe to your Yocto layer:
#      $ cp entos-diagnostics_1.0.bb <layer>/recipes-extended/entos/
#
#   2. Include in your image via packagegroup:
#      $ echo "IMAGE_INSTALL:append = \" entos-diagnostics\"" >> local.conf
#
#   3. Or add to meta-rdk image recipe:
#      EXTRA_IMAGE_INSTALL:append = " entos-diagnostics"
#
#   4. Build the package:
#      $ bitbake entos-diagnostics
#
#   5. Verify build output:
#      $ find tmp/deploy -name "*entos-diagnostics*"
#
#   6. Build complete image:
#      $ bitbake rdk-image-weston
#
# BUILD DEPENDENCIES (Required in sysroot):
# ──────────────────────────────────────────────────────────────────────────
#
#   - curl (libcurl development package)
#   - jsoncpp (JsonCpp development package, C++ JSON parsing)
#   - yaml-cpp (optional, enables YAML configuration support)
#   - cmake (build system)
#   - gcc/g++ (C++17 compiler)
#
# To disable optional YAML support and reduce dependencies:
#   $ ENTOS_DISABLE_YAML=1 bitbake entos-diagnostics
#
# INSTALLATION & VERIFICATION:
# ──────────────────────────────────────────────────────────────────────────
#
#   On target device, verify installation:
#   $ root@device:~# entos-diagnostics --help
#   $ root@device:~# ls -la /usr/bin/entos-diagnostics
#   $ root@device:~# ls -la /etc/entos-diagnostics/
#   $ root@device:~# ls -la /usr/share/entos-diagnostics/
#
# ============================================================================
# RUNTIME USAGE - COMMAND LINE REFERENCE
# ============================================================================
#
# BASIC COMMANDS:
# ──────────────────────────────────────────────────────────────────────────
#
#   List Available Sections:
#   $ entos-diagnostics list
#   $ entos-diagnostics list --format json
#
#   Show Thunder Plugins Status:
#   $ entos-diagnostics plugins
#   $ entos-diagnostics plugins --format json
#
#   Collect System Diagnostics (default all sections):
#   $ entos-diagnostics dumpsys
#   $ entos-diagnostics dumpsys --format text
#   $ entos-diagnostics dumpsys --format json
#
# SPECIFIC DIAGNOSTIC SECTIONS:
# ──────────────────────────────────────────────────────────────────────────
#
#   Platform Information:
#   $ entos-diagnostics dumpsys --section platform
#
#   Configuration State:
#   $ entos-diagnostics dumpsys --section config
#
#   Input Handler State:
#   $ entos-diagnostics dumpsys --section input
#
#   Application Runtime:
#   $ entos-diagnostics dumpsys --section runtime
#   $ entos-diagnostics dumpsys --section runtime --format json
#
#   Package Manager:
#   $ entos-diagnostics dumpsys --section package
#
#   Window Manager:
#   $ entos-diagnostics dumpsys --section window
#
#   Power State Manager:
#   $ entos-diagnostics dumpsys --section power
#
#   Thunder Service Manager:
#   $ entos-diagnostics dumpsys --section thunder
#
#   Multiple Sections:
#   $ entos-diagnostics dumpsys --section platform --section runtime --section window
#
# REMOTE THUNDER CONNECTION:
# ──────────────────────────────────────────────────────────────────────────
#
#   Default (localhost):
#   $ entos-diagnostics dumpsys
#
#   Remote Thunder endpoint:
#   $ entos-diagnostics dumpsys --url http://192.168.1.100:9998
#
#   With environment variable:
#   $ export THUNDER_URL=http://192.168.1.100:9998
#   $ entos-diagnostics dumpsys
#
# TIMEOUTS AND PERFORMANCE:
# ──────────────────────────────────────────────────────────────────────────
#
#   Default timeouts (all in milliseconds):
#   - Connection timeout: 1000 ms
#   - Per-call timeout: 3000 ms
#   - Total snapshot timeout: 15000 ms (15 seconds)
#
#   Custom timeouts:
#   $ entos-diagnostics dumpsys --connect-ms 2000 --request-ms 5000 --timeout-ms 30000
#
#   Quick lightweight snapshot (5 second budget):
#   $ entos-diagnostics dumpsys --timeout-ms 5000
#
# AUTHENTICATION & SECURITY:
# ──────────────────────────────────────────────────────────────────────────
#
#   Authorization token file:
#   $ entos-diagnostics dumpsys --token-file /etc/thunder/authtoken
#
#   Environment variable token:
#   $ export TOKEN="Bearer <your-token-here>"
#   $ entos-diagnostics dumpsys
#
#   Note: Sensitive data (tokens) are automatically redacted from output
#
# CONFIGURATION FILES:
# ──────────────────────────────────────────────────────────────────────────
#
#   Default provider configuration:
#   $ entos-diagnostics dumpsys --profile /etc/entos-diagnostics/providers.json
#
#   Custom provider profile:
#   $ entos-diagnostics dumpsys --profile /opt/custom-providers.json
#
#   Custom configuration sources:
#   $ entos-diagnostics dumpsys --config-file /etc/sky/aisettings.json
#   $ entos-diagnostics dumpsys --config-file /etc/custom/settings.json
#
# FIXTURE/TESTING MODE:
# ──────────────────────────────────────────────────────────────────────────
#
#   Use custom /proc root for testing (simulated environment):
#   $ entos-diagnostics dumpsys --proc-root /opt/test/procfs
#
# OUTPUT EXAMPLES:
# ──────────────────────────────────────────────────────────────────────────
#
#   1. Text format (human-readable):
#      $ entos-diagnostics dumpsys --section platform --format text
#      Output: Formatted table with platform metrics
#
#   2. JSON format (machine-readable):
#      $ entos-diagnostics dumpsys --section runtime --format json | jq .
#      Output: JSON object with nested section data
#
#   3. Save to file:
#      $ entos-diagnostics dumpsys --format json > /tmp/snapshot.json
#      $ entos-diagnostics dumpsys > /tmp/snapshot.txt
#
#   4. Pipe to other tools:
#      $ entos-diagnostics dumpsys --format json | jq '.sections[] | select(.id=="runtime")'
#      $ entos-diagnostics dumpsys --section platform | grep -i "version"
#
#   5. Monitor loop (5 snapshots):
#      $ for i in {1..5}; do entos-diagnostics dumpsys --section runtime; sleep 2; done
#
# TROUBLESHOOTING:
# ──────────────────────────────────────────────────────────────────────────
#
#   Connection Issues:
#   $ entos-diagnostics dumpsys --url http://127.0.0.1:9998 2>&1
#   Error "Connection refused": Check Thunder is running
#
#   Timeout Issues:
#   $ entos-diagnostics dumpsys --timeout-ms 30000
#   Increase timeout if system is slow or many plugins are querying
#
#   Provider Disabled:
#   $ entos-diagnostics dumpsys --section runtime 2>&1
#   Error "Provider disabled": Verify providers.json has enabled=true
#
#   Missing Sections:
#   $ entos-diagnostics list
#   Run list to see all available sections and their IDs
#
#   Verify Installation:
#   $ which entos-diagnostics
#   $ entos-diagnostics --help
#   $ entos-diagnostics list
#
# INTEGRATION WITH DIAGNOSTICS TOOLS:
# ──────────────────────────────────────────────────────────────────────────
#
#   Collect and compress diagnostics:
#   $ entos-diagnostics dumpsys --format json > snapshot.json
#   $ tar czf diagnostic_bundle.tar.gz snapshot.json /var/log/*
#
#   Compare before/after:
#   $ entos-diagnostics dumpsys > before.txt
#   $ # perform test/operation
#   $ entos-diagnostics dumpsys > after.txt
#   $ diff before.txt after.txt
#
#   Integration with monitoring systems:
#   $ while true; do
#       entos-diagnostics dumpsys --section runtime --format json | \
#       jq '.sections[0].data.apps[] | {id, state, pid}' >> runtime_monitor.log
#       sleep 30
#     done
#
# PERFORMANCE CONSIDERATIONS:
# ──────────────────────────────────────────────────────────────────────────
#
#   Quick snapshot (critical sections only):
#   $ entos-diagnostics dumpsys --section platform --section runtime
#
#   Full diagnostic (all sections, ~15 seconds):
#   $ entos-diagnostics dumpsys --timeout-ms 20000
#
#   Minimal snapshot (testing):
#   $ entos-diagnostics dumpsys --section platform --timeout-ms 5000
#
# ENVIRONMENT VARIABLES:
# ──────────────────────────────────────────────────────────────────────────
#
#   THUNDER_URL        - Override default Thunder endpoint
#   TOKEN               - Authorization token for Thunder
#   
#   Example:
#   $ export THUNDER_URL=http://192.168.1.100:9998
#   $ export TOKEN="Bearer abc123"
#   $ entos-diagnostics dumpsys
#
# APPMANAGER UNIFIED DIAGNOSTICS (v2.0):
# ──────────────────────────────────────────────────────────────────────────
#
#   New in v2.0: Unified diagnostic output for AppManager and similar managers
#   Output format: Human-readable tables (NOT JSON)
#
#   Example AppManager diagnostics (when implemented):
#   $ entos-diagnostics appmanager --loaded
#   $ entos-diagnostics appmanager --installed
#   $ entos-diagnostics appmanager --packages
#   $ entos-diagnostics appmanager --resources
#   $ entos-diagnostics appmanager --window
#
#   Complete dump:
#   $ entos-diagnostics appmanager --all
#
#   Expected output: Human-readable tables with summaries
#   Example:
#   App ID             | Instance ID        | State        | PID
#   ───────────────────┼────────────────────┼──────────────┼─────────
#   com.netflix.nrdp   | instance-001       | running      |    1234
#   Summary: 2 app(s) loaded
#
# ============================================================================
