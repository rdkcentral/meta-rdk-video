# EntOS Diagnostics Yocto Recipe

This directory contains the Yocto BitBake recipe for building and packaging the **entos-diagnostics** component in RDK-based embedded Linux systems.

## Quick Start

### Prerequisites

- Yocto/OpenEmbedded build environment set up
- Required layer stack (see below)
- Source repository: https://github.com/Yogaraj97/entos-diagnostics

### Build

```bash
# Build the package
bitbake entos-diagnostics

# Or include in your image
IMAGE_INSTALL:append = " entos-diagnostics"
bitbake <your-image-name>

# Or use package manager on target
opkg install entos-diagnostics
```

### Verify

```bash
# On target device
/usr/bin/entos-diagnostics list
/usr/bin/entos-diagnostics plugins
/usr/bin/entos-diagnostics dumpsys --format json
```

## Files

### `entos-diagnostics_1.0.bb`

**Yocto BitBake recipe** for entos-diagnostics component.

**Key Features**:
- ✅ CMake-based build system
- ✅ Cross-compilation support
- ✅ Optional YAML configuration support
- ✅ Comprehensive dependency management
- ✅ Automatic documentation installation
- ✅ Configuration file handling

**Source**: https://github.com/Yogaraj97/entos-diagnostics (commit: 9888db5e56f33cbe0e0682c52a4e3dcf2b47aac4)

### `RECIPE_DOCUMENTATION.md`

**Comprehensive documentation** for the recipe including:
- Repository information
- Complete dependency list (build-time and runtime)
- Build configuration details
- Package structure
- Installation procedures
- Usage examples
- Troubleshooting guide
- Advanced customization
- License compliance
- Performance considerations
- Maintenance procedures

## Dependencies

### Build-Time (DEPENDS)

| Package | Version | Purpose |
|---------|---------|---------|
| cmake-native | 3.16+ | Build system |
| pkg-config-native | Latest | Package configuration |
| curl | 7.80+ | HTTP/HTTPS transport, TLS |
| jsoncpp | 1.8+ | JSON parsing/serialization |
| yaml-cpp | 0.6+ | Optional YAML config support |

### Runtime (RDEPENDS)

| Package | Purpose |
|---------|---------|
| curl | HTTP/HTTPS client library |
| jsoncpp | JSON library |
| yaml-cpp | YAML support (optional) |
| libgcc | GCC runtime (libgcc_s) |
| libstdc++ | C++ standard library |

## Integration Steps

1. **Copy recipe to your layer**
   ```bash
   cp entos-diagnostics_1.0.bb <your-layer>/recipes-extended/entos/
   cp RECIPE_DOCUMENTATION.md <your-layer>/recipes-extended/entos/
   ```

2. **Ensure dependencies available in layer stack**
   - Verify curl recipe in meta-openembedded/meta-oe or similar
   - Verify jsoncpp available (check in your layers)
   - Verify yaml-cpp available (or disable with ENTOS_DISABLE_YAML=1)

3. **Add layer to bblayers.conf**
   ```bitbake
   BBLAYERS += " /path/to/meta-rdk-video"
   ```

4. **Build and test**
   ```bash
   bitbake entos-diagnostics
   ```

## Build Configuration

### Enable/Disable YAML Support

**Default**: Enabled (requires yaml-cpp dependency)

**To disable** (in distro config or local.conf):
```bitbake
ENTOS_DISABLE_YAML = "1"
```

Effect: Reduces dependencies, slightly smaller binary, no YAML config support

### Custom CMake Options

Add in distro config:
```bitbake
EXTRA_OECMAKE:append:entos-diagnostics = " -DCUSTOM_FLAG=value"
```

## Package Output

### `entos-diagnostics` (main)
```
/usr/bin/entos-diagnostics                    # Executable
/etc/entos-diagnostics/providers.json         # Configuration
/usr/share/entos-diagnostics/                 # Profiles/schemas
```

### `entos-diagnostics-dev`
Headers and static libraries (if applicable)

### `entos-diagnostics-doc`
Documentation files (README, design guides, OpenSpec docs)

### `entos-diagnostics-dbg`
Debug symbols (if debug build enabled)

## Usage Examples

### Local Device Snapshot
```bash
/usr/bin/entos-diagnostics list                   # List providers
/usr/bin/entos-diagnostics plugins                # Thunder plugin status
/usr/bin/entos-diagnostics dumpsys               # Full text snapshot
/usr/bin/entos-diagnostics dumpsys --format json # JSON format
```

### Remote Endpoint
```bash
export THUNDER_URL=http://192.168.1.100:9998
export TOKEN='Authorization: Bearer <token>'
/usr/bin/entos-diagnostics dumpsys
```

### With Configuration Files
```bash
/usr/bin/entos-diagnostics dumpsys \
  --config-file /etc/entos-diagnostics/base.json \
  --config-file /etc/entos-diagnostics/local.yaml
```

### Specific Sections
```bash
/usr/bin/entos-diagnostics dumpsys \
  --section platform \
  --section input \
  --section config \
  --format json
```

## Troubleshooting

### "curl not found" during build

Ensure curl recipe is in your layer stack:
```bash
find ${BBLAYERS} -name "curl_*.bb"
```

If not found, add meta-openembedded/meta-oe:
```bitbake
BBLAYERS += " ${TOPDIR}/../meta-openembedded/meta-oe"
```

### "jsoncpp not found" during build

Check if jsoncpp recipe exists:
```bash
find ${BBLAYERS} -name "jsoncpp_*.bb"
```

If not found, you may need to create a recipe or check alternative layers.

### Executable fails at runtime

On target, check dependencies:
```bash
ldd /usr/bin/entos-diagnostics
# Should show libcurl, libjsoncpp, libstdc++, etc.

# If missing, install runtime packages:
opkg install curl jsoncpp
```

## Performance

| Metric | Value |
|--------|-------|
| Build time (first) | 5-15 min |
| Build time (incremental) | <1 min |
| Binary size (stripped) | ~600-750 KB |
| Runtime memory (minimal) | ~5-10 MB |
| Runtime memory (full) | ~20-50 MB |

## References

- **entos-diagnostics Repository**: https://github.com/Yogaraj97/entos-diagnostics
- **Recipe Documentation**: See RECIPE_DOCUMENTATION.md
- **Component README**: Source repo README.md (installed to /usr/share/doc/)
- **Yocto BitBake Manual**: https://docs.yoctoproject.org/bitbake
- **OpenEmbedded Wiki**: https://www.openembedded.org/wiki

## License

- **Recipe**: Apache-2.0 (same as component)
- **Component**: Apache-2.0

---

**Recipe Version**: 1.0  
**Last Updated**: November 2026  
**Maintainer**: RDK/AI2.0 Team
