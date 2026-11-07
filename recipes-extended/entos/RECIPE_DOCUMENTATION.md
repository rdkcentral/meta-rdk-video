# entos-diagnostics Yocto Recipe Documentation

## Overview

This Yocto recipe (`entos-diagnostics_1.0.bb`) integrates the **EntOS Diagnostics** component into RDK-based embedded Linux builds. It enables automated compilation, packaging, and deployment of the diagnostic utility across Yocto-based BSPs.

**Recipe Location**: `meta-rdk-video/recipes-extended/entos/entos-diagnostics_1.0.bb`

---

## Repository Information

| Property | Value |
|----------|-------|
| **Repository** | https://github.com/Yogaraj97/entos-diagnostics |
| **Branch** | main |
| **Commit** | 9888db5e56f33cbe0e0682c52a4e3dcf2b47aac4 |
| **Protocol** | HTTPS (git://) |
| **License** | Apache-2.0 |

---

## Dependencies

### Build-Time Dependencies (DEPENDS)

These packages are required during compilation and must be available in the build sysroot:

#### Core Build Tools
```bitbake
cmake-native          # CMake build system (native/host version)
pkg-config-native     # Package configuration tool (native version)
```

#### Required Libraries
```bitbake
curl                  # libcurl - HTTP/HTTPS client library with TLS support
                      # Version: typically 7.80+
                      # Features: HTTP/TLS authentication, custom headers, timeouts

jsoncpp               # JsonCpp - JSON parsing and generation library
                      # Version: typically 1.8+
                      # Features: Parsing, serialization, deterministic output
```

#### Optional Libraries
```bitbake
yaml-cpp              # YAML configuration file parsing
                      # Version: typically 0.6+
                      # Optional: Can be disabled by setting ENTOS_DISABLE_YAML=1
                      # Feature: Realtek YAML configuration support
```

### Runtime Dependencies (RDEPENDS)

These packages must be present on the target device for the utility to function:

```bitbake
curl                  # Runtime curl library (libcurl.so.*)
jsoncpp               # Runtime JsonCpp library
yaml-cpp              # Runtime YAML library (if YAML support enabled)
gcc-runtime           # GCC runtime library (libgcc_s.so.*)
libstdc++             # C++ standard library (libstdc++.so.*)
```

### Implicit Dependencies

The Yocto build system automatically satisfies these:

```bitbake
libc                  # C standard library (glibc)
libpthread            # POSIX threads library
libm                  # Math library
libdl                 # Dynamic linker
```

### Optional Build-Time Dependencies

Can be enabled/disabled via configuration:

```bitbake
# For YAML configuration support (enabled by default)
yaml-cpp              # Enable: -DENTOS_ENABLE_YAML=ON
                      # Disable: -DENTOS_ENABLE_YAML=OFF
                      # Set in distro config: ENTOS_DISABLE_YAML = "1"
```

---

## Build Configuration

### CMake Options

The recipe configures CMake with these flags:

```cmake
-DCMAKE_BUILD_TYPE=Release              # Optimized build, no debug symbols
-DCMAKE_INSTALL_PREFIX=/usr             # Install to /usr (standard Linux FHS)
-DBUILD_TESTING=OFF                     # Don't build tests in production
-DENTOS_ENABLE_YAML=ON                  # Enable YAML configuration support
-DCMAKE_EXPORT_COMPILE_COMMANDS=OFF     # Don't generate compile_commands.json
```

### Compiler Flags

Applied to all compilation units:

```bash
# C flags
-Wall -Wextra -Wpedantic               # Strict warnings

# C++ flags
-Wall -Wextra -Wpedantic                # Strict warnings

# Build type: Release
# Implied flags: -O2 (optimization), no debug symbols
```

---

## Package Structure

### Output Package: `entos-diagnostics`

**Main binary package** containing the executable and configuration:

```
/usr/bin/entos-diagnostics                    # Executable (755)
/etc/entos-diagnostics/providers.json         # Provider configuration (644)
/etc/entos-diagnostics/*.yaml                 # YAML configs if present (644)
/usr/share/entos-diagnostics/                 # Schemas and profiles
```

### Output Package: `entos-diagnostics-dev`

**Development package** (usually minimal for this utility):

```
/usr/include/                                 # C++ headers
/usr/lib/                                     # Static libraries (.a)
/usr/lib/pkgconfig/                          # pkg-config files
```

### Output Package: `entos-diagnostics-doc`

**Documentation package**:

```
/usr/share/doc/entos-diagnostics/README.md
/usr/share/doc/entos-diagnostics/design.md
/usr/share/doc/entos-diagnostics/workflow.md
/usr/share/doc/entos-diagnostics/tasks.md
/usr/share/doc/entos-diagnostics/proposal.md
/usr/share/doc/entos-diagnostics/analysis.md
/usr/share/doc/entos-diagnostics/LICENSE
```

### Output Package: `entos-diagnostics-dbg`

**Debug symbols package** (if debug build enabled):

```
/usr/lib/debug/usr/bin/entos-diagnostics.debug
```

---

## Installation & Usage

### Build the Package

```bash
# Add to your distro config or build command
bitbake entos-diagnostics

# Or as part of an image
bitbake <your-image-name>
```

### Install to Image

**Option 1: Explicit IMAGE_INSTALL**

```bitbake
# In your distro/image .bb file
IMAGE_INSTALL:append = " entos-diagnostics"
```

**Option 2: Via Packagegroup**

```bitbake
# In a packagegroup recipe
RDEPENDS:packagegroup-core-diagnostics:append = " entos-diagnostics"
```

**Option 3: Via Package Manager (Runtime)**

On device with package manager:
```bash
opkg install entos-diagnostics
apt install entos-diagnostics
```

### Verify Installation

On target device:
```bash
# Check if executable is present and working
/usr/bin/entos-diagnostics list

# Expected output:
# config          CONFIG
# dial            DIAL
# ...
# (list of available data sources)
```

### Run Examples

```bash
# List all providers
/usr/bin/entos-diagnostics list

# Check Thunder plugin status
/usr/bin/entos-diagnostics plugins

# Full device snapshot (text format)
/usr/bin/entos-diagnostics dumpsys

# Specific sections in JSON format
/usr/bin/entos-diagnostics dumpsys --section platform --section config --format json

# Remote endpoint with authentication
export THUNDER_URL=http://192.168.1.100:9998
export TOKEN='Authorization: Bearer <token>'
/usr/bin/entos-diagnostics dumpsys

# With local configuration
/usr/bin/entos-diagnostics dumpsys \
  --config-file /etc/entos-diagnostics/base.json \
  --config-file /etc/entos-diagnostics/local.yaml
```

---

## Configuration

### Enable/Disable YAML Support

**Default**: YAML support enabled (`-DENTOS_ENABLE_YAML=ON`)

**To disable YAML** (reduce dependencies):

Option 1 - In distro config:
```bitbake
# In your distro .conf file
ENTOS_DISABLE_YAML = "1"
```

Option 2 - In local.conf:
```bitbake
ENTOS_DISABLE_YAML = "1"
```

Effect:
- Disables yaml-cpp dependency
- Removes YAML configuration file support
- Reduces package size

### Custom CMake Options

To add custom CMake flags:

```bitbake
# In your distro config
EXTRA_OECMAKE:append:entos-diagnostics = " -DCUSTOM_OPTION=VALUE"
```

### Architecture-Specific Builds

The recipe respects the Yocto architecture settings:

```bitbake
PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
COMPATIBLE_HOST = ".*-linux"
```

This ensures proper builds for:
- ARM (armv7l, armv8)
- x86_64
- MIPS
- Other Yocto-supported architectures

---

## Dependency Resolution

### Yocto Build System Resolution

The Yocto build system automatically:

1. **Fetches** build dependencies from configured layers
2. **Stagings** them into `${SYSROOT_DESTDIR}`
3. **Passes** include/lib paths to CMake via `OE_CMAKE_TOOLCHAIN_FILE`
4. **Cross-compiles** with the target architecture toolchain
5. **Packages** runtime dependencies into the rootfs

### Common Dependency Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| `curl not found` | curl recipe not in layers | Add meta-openembedded or similar |
| `jsoncpp not found` | jsoncpp recipe missing | Add meta-jsoncpp layer or provide recipe |
| `yaml-cpp not found` | yaml-cpp recipe missing | Set `ENTOS_DISABLE_YAML=1` or add recipe |
| `gcc-runtime missing` | Not in image | Add to IMAGE_INSTALL or depend recipe |
| `libstdc++ version error` | Toolchain mismatch | Verify matching GCC versions |

---

## Required Layers

For a complete build, ensure your Yocto environment includes:

### Mandatory Layers
```bash
# Core Yocto layers
meta                              # Core recipes (cmake, gcc, etc.)
meta-openembedded/meta-oe         # OpenEmbedded common recipes
meta-rdk-video                    # This layer (entos-diagnostics recipe)
```

### Recommended Layers
```bash
# For development/debug builds
meta-openembedded/meta-perl       # Perl tools (if needed)
meta-openembedded/meta-python     # Python tools (if needed)
```

### Layer Configuration Example

```bash
# In bblayers.conf
BBLAYERS = " \
    ${TOPDIR}/../meta \
    ${TOPDIR}/../meta-openembedded/meta-oe \
    ${TOPDIR}/../meta-rdk-video \
"
```

---

## Build Verification

### Check Recipe Syntax

```bash
bitbake -v entos-diagnostics 2>&1 | head -30
```

Expected output: No parse errors, shows recipe path

### Do a Test Build

```bash
bitbake entos-diagnostics -c cleanall
bitbake entos-diagnostics
```

Expected output: Build completes successfully, exits with code 0

### Verify Package Contents

```bash
# After successful build
find ${TMPDIR}/work/*/entos-diagnostics-*/package -type f
# or
bitbake entos-diagnostics -c package && \
  find ${TMPDIR}/work/*/entos-diagnostics-*/package -type f
```

### Check Dependencies Were Resolved

```bash
bitbake -e entos-diagnostics | grep "^DEPENDS"
# Should show curl, jsoncpp, yaml-cpp, etc.
```

---

## Cross-Compilation Notes

The recipe is designed for cross-compilation:

### Target Architecture Handling

```bash
# Automatically handled by Yocto
OE_CMAKE_TOOLCHAIN_FILE          # Provides cross-compile config
CMAKE_TOOLCHAIN_FILE              # Points to correct toolchain
COMPATIBLE_HOST = ".*-linux"      # Ensures Linux-only builds
```

### Build Machine vs. Target Machine

```bitbake
cmake-native                      # Runs on build machine
curl, jsoncpp, yaml-cpp           # Built for target architecture
gcc, g++                          # Target architecture compilers
```

### Sysroot Management

Build dependencies are automatically staged:

```bash
${SYSROOT_DESTDIR}/usr/include/   # curl, jsoncpp headers
${SYSROOT_DESTDIR}/usr/lib/       # curl, jsoncpp libraries
${SYSROOT_DESTDIR}/usr/lib/pkgconfig/  # pkg-config files
```

---

## Troubleshooting

### Build Fails: "curl not found"

```
Check if curl recipe exists in any layer:
  find ${BBLAYERS} -name "curl_*.bb"

If not found, add the layer that provides it:
  meta-openembedded/meta-oe usually has curl
```

### Build Fails: "jsoncpp version mismatch"

```
entos-diagnostics may be incompatible with very old jsoncpp versions.
Ensure jsoncpp >= 1.8:
  bitbake -e jsoncpp | grep "^PV"
```

### Link Error: "undefined reference to curl functions"

```
Check that libcurl.so is in sysroot:
  find ${SYSROOT_DESTDIR} -name "libcurl*"

If missing, rebuild curl:
  bitbake curl -c cleanall && bitbake curl
```

### Executable Fails at Runtime: "cannot find shared object"

On target device:
```bash
# Check dependencies
ldd /usr/bin/entos-diagnostics
# Should show:
#   libcurl.so.4 => /usr/lib/libcurl.so.4
#   libjsoncpp.so.XX => /usr/lib/libjsoncpp.so.XX
#   libyaml-cpp.so.X => /usr/lib/libyaml-cpp.so.X
#   libstdc++.so.6 => /usr/lib/libstdc++.so.6

# If any are missing, install runtime dependencies:
opkg install curl jsoncpp yaml-cpp libstdc++
```

---

## Advanced Customization

### Custom Build Flags

In distro config:

```bitbake
# Add optimization flags
TARGET_CFLAGS:append = " -march=native"
TARGET_CXXFLAGS:append = " -march=native"

# Or disable specific features
EXTRA_OECMAKE:append = " -DCUSTOM_FEATURE=OFF"
```

### Static Linking (Advanced)

To statically link dependencies (not recommended):

```bitbake
# This requires all dependencies to have static library builds
EXTRA_OECMAKE:append = " -DBUILD_SHARED_LIBS=OFF"
# Then rebuild all dependencies as static

# Not recommended: increases binary size, breaks updates
```

### Debug Build

To build with debug symbols:

```bash
bitbake entos-diagnostics -c cleanall
VARIANT=debug bitbake entos-diagnostics
```

Or in local.conf:

```bitbake
BUILD_OPTIMIZATION = "-O0 -g"
INHIBIT_STRIP = "1"
```

---

## License Compliance

### License Declaration

```bitbake
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"
```

### License Verification

Yocto will verify the LICENSE file matches the checksum:

```bash
# During build, Yocto checks:
# 1. LICENSE file exists in source tree
# 2. MD5 matches declared checksum
# 3. Declared license is valid (checked against SPDX list)
```

### Dependency Licenses

All runtime dependencies' licenses are tracked:

```bash
bitbake entos-diagnostics -c check_licenses
# Generates license summary including:
#   - Apache-2.0 (entos-diagnostics)
#   - Any license for curl (typically Apache or similar)
#   - Any license for jsoncpp, yaml-cpp
```

---

## Performance Considerations

### Build Time

- **First build**: 5-15 minutes (depends on layer, dependencies)
- **Rebuild**: <1 minute (incremental)
- **Clean build**: 10-20 minutes

### Binary Size

- **entos-diagnostics (stripped)**: ~600-750 KB
- **With debug symbols**: ~2-3 MB
- **With dependencies installed**: Additional 5-15 MB

### Runtime Memory

- **Minimal**: ~5-10 MB (base)
- **Full snapshot**: ~20-50 MB (depending on device state)

---

## Maintenance & Updates

### Updating to New Version

1. Update recipe PV:
```bitbake
PV = "1.1.0"
PR = "r0"
```

2. Update SRCREV:
```bitbake
SRCREV = "<new-commit-hash>"
```

3. Update LIC_FILES_CHKSUM if license changed:
```bash
# Get new MD5
md5sum LICENSE
```

4. Test build:
```bash
bitbake entos-diagnostics -c cleanall && bitbake entos-diagnostics
```

### Version Scheme

Current: `1.0.0-r0`
- `1.0.0`: Project semantic versioning
- `r0`: Yocto recipe revision (increment if recipe changes, not source)

---

## Integration Checklist

- [ ] Recipe file placed in `meta-rdk-video/recipes-extended/entos/`
- [ ] All dependencies (curl, jsoncpp, yaml-cpp) available in layer stack
- [ ] Layer added to `bblayers.conf`
- [ ] License file verified (`LICENSE` in source repo)
- [ ] SRCREV commit hash verified
- [ ] Test build successful: `bitbake entos-diagnostics`
- [ ] Executable works on target: `/usr/bin/entos-diagnostics list`
- [ ] Documentation (if needed) included in image
- [ ] Runtime dependencies auto-installed with main package

---

## References

- **Yocto Project Documentation**: https://www.yoctoproject.org/docs
- **BitBake User Manual**: https://docs.yoctoproject.org/bitbake
- **CMake in Yocto**: https://docs.yoctoproject.org/ref-manual/classes.html#ref-classes-cmake
- **entos-diagnostics Repository**: https://github.com/Yogaraj97/entos-diagnostics
- **RDK Documentation**: https://wiki.rdkcentral.com

---

**Recipe Created**: November 2026  
**Last Updated**: November 2026  
**Maintainer**: RDK/AI2.0 Team
