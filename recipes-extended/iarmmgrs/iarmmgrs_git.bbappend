# iarmmgrs_git.bbappend
#
# DS_COMRPC migration: completely remove dsMgrMain binary and dsmgr.service.
# dsMgr is replaced by entservices-devicesettings (Thunder COM-RPC plugin).
# devicesettings (libds.so, libdshalcli.so) removed from RDEPENDS/DEPENDS/LDFLAGS
# since dsMgrMain (the only iarmmgrs binary that linked against libds) is gone.
# sysMgrMain/mfrMgrMain/libiarmUtils do NOT call any device:: functions —
# -lds/-ldshalcli was only in the global LDFLAGS, not needed by these targets.
#
# Rollback: delete this file.

do_install:append() {
    # Remove dsMgrMain binary completely from image
    rm -f ${D}${bindir}/dsMgrMain
    # Remove dsmgr.service completely from image
    rm -f ${D}${systemd_unitdir}/system/dsmgr.service
    # test_mfr_client is a debug/test tool with no production use; remove it.
    rm -f ${D}${bindir}/test_mfr_client
}

# Recompile mfr/test_mfr/ without -lds/-ldshalcli.
# The base .bb do_compile:append hardcodes:
#   LDFLAGS="-ldshalcli -lds ... ${LDFLAGS}" oe_runmake -B -C ${S}/mfr/test_mfr/
# placing -lds BEFORE --as-needed, forcing unconditional libds linkage on all
# mfr_* utilities. The mfr/test_mfr/Makefile itself only adds -lIARMBus and
# does NOT hardcode -lds, so recompiling with our LDFLAGS (after LDFLAGS:remove
# has stripped -lds/-ldshalcli) produces clean binaries that run without libds.
# This do_compile:append runs AFTER the .bb's do_compile:append, overwriting
# the -lds-linked binaries.
do_compile:append() {
    LDFLAGS="${LDFLAGS}" CFLAGS="${CFLAGS}" oe_runmake -B -C ${S}/mfr/test_mfr/
}

# dsMgrMain linked against -lds; once removed, libds.so no longer needed at runtime
RDEPENDS:${PN}:remove = "devicesettings"
SYSTEMD_SERVICE:${PN}:remove = "dsmgr.service"

# Remove -lds/-ldshalcli from the BitBake LDFLAGS variable.
# This prevents sysMgrMain, mfrMgrMain (main build), and libiarmUtils from
# inheriting -lds via the global "LDFLAGS += -ldshalcli -lds" in the base .bb.
# NOTE: do_compile shell-level LDFLAGS for dsmgr/ and mfr/test_mfr/ are
# hardcoded in the .bb ("LDFLAGS='-lds ...' oe_runmake -B -C ${S}/dsmgr/") and
# are NOT affected by this :remove — those compilations still link libds.so, but
# their resulting binaries are removed from the image by do_install:append above.
LDFLAGS:remove = " -lds -ldshalcli"

# NOTE: devicesettings is intentionally kept in DEPENDS (build-time sysroot).
# iarmmgrs_git.bb do_compile hardcodes -lds/-ldshalsrv for dsmgr/ and
# mfr/test_mfr/ via shell-level LDFLAGS; those compilations need libds.so in
# the recipe-sysroot. Removing devicesettings from DEPENDS would break the
# build. Runtime exclusion is handled by RDEPENDS:remove above.

