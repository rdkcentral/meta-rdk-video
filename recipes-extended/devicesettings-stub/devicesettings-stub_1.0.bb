SUMMARY = "No-op stub for libds (devicesettings client library)"
DESCRIPTION = "Provides libds.so with all device:: C++ symbols as no-op stubs. \
Replaces the real devicesettings package for non-Thunder clients when dsMgr is \
disabled. No IARM bus calls, no dsMgr daemon, no HAL dependency at runtime. \
RPROVIDES devicesettings so runtime RDEPENDS are satisfied; PROVIDES is omitted \
to avoid a build-time conflict with devicesettings_git.bb."

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

PV = "1.0"
PR = "r0"

# -------------------------------------------------------------------------
# RPROVIDES so that the installed package satisfies any RDEPENDS on
# 'devicesettings' at the package-manager level (runtime only).
#
# PROVIDES is intentionally NOT set: adding it would make BitBake see two
# build-time providers for 'devicesettings' (this stub + devicesettings_git.bb)
# and fail with "Multiple .bb files are due to be built which each provide".
# Client recipes that DEPENDS="devicesettings" still pull in the real
# devicesettings headers for compilation — which is correct.
#
# RREPLACES/RCONFLICTS prevent the package manager installing both at once.
#
# To rollback: replace 'devicesettings-stub' with 'devicesettings' in
# IMAGE_INSTALL or any override that selects this package.
# -------------------------------------------------------------------------
RPROVIDES:${PN} += "devicesettings"
RREPLACES:${PN} += "devicesettings"
RCONFLICTS:${PN} += "devicesettings"

# Stub sources live in the recipe's files/ directory
S = "${WORKDIR}"

SRC_URI = "file://CMakeLists.txt \
           file://devicesettings_stub.cpp \
          "

inherit cmake

PACKAGE_ARCH = "${MACHINE_ARCH}"

# -------------------------------------------------------------------------
# Build-time: headers only
#   devicesettings-hal-headers-dev  → dsTypes.h, dsError.h, dsAVDTypes.h, etc.
#   devicesettings                  → device:: C++ headers (manager.hpp, host.hpp …)
#
# Runtime: nothing from devicesettings or dsMgr
# -------------------------------------------------------------------------
DEPENDS = "devicesettings devicesettings-hal-headers"
# NOTE: no RDEPENDS on 'devicesettings' — the stub IS the runtime libds.so

CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/rdk/ds "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/rdk/halif/ds-hal "
CXXFLAGS += " -I${STAGING_DIR_TARGET}${includedir}/rdk/ds-hal "
CXXFLAGS += " -Wno-error "

EXTRA_OECMAKE += " \
    -DCMAKE_SYSROOT=${STAGING_DIR_TARGET} \
    -DBUILD_SHARED_LIBS=ON \
"

# Install the stub libds.so into the same location as the real one
FILES:${PN} += "${libdir}/libds.so*"

INSANE_SKIP:${PN} += "dev-so"
