#!/bin/sh
# ---------------------------------------------------------------------------
# perf.sh
#
# Capture a DWARF (libdw) perf profile of the WPEFramework ResourceMonitor
# thread across Thunder STARTUP, WITHOUT manually restarting Thunder.
#
# It waits for the WPEFramework process to appear (e.g. after a reboot, or the
# next supervisor respawn), attaches to the whole process as early as possible
# with --call-graph dwarf, then filters the decoded script down to the
# Monitor::IResou thread and builds the collapsed stacks.
#
# Requires the dwarf-enabled perf (dwarf:[on], libunwind:[OFF],
# libdw-dwarf-unwind:[on]) and NOT-stripped WPE binaries (.debug_frame present).
#
# Modes:
#   ./perf.sh watch          # run now; waits for next WPE start
#                                            # (then YOU reboot / let it respawn)
#   ./perf.sh install-boot   # arm at boot via systemd, then reboot
#   ./perf.sh uninstall-boot # remove the boot unit
#
# Tunables (env):
#   DURATION=240   capture window seconds after WPE appears
#   FREQ=99       sampling frequency Hz
#   STACKSZ=65528 dwarf user-stack bytes (max)
# ---------------------------------------------------------------------------
set -u

PERF="${PERF:-/opt/perf/packages-split/lib32-perf/usr/bin/perf}"
PERF_LIB="${PERF_LIB:-/opt/perf/packages-split/lib32-perf/usr/lib}"
COMM="${COMM:-Monitor::IResou}"
WPE_PROC="${WPE_PROC:-WPEFramework}"
DURATION="${DURATION:-240}"
FREQ="${FREQ:-99}"
STACKSZ="${STACKSZ:-65528}"
OUTDIR="${OUTDIR:-/opt/logs}"
SELF_INSTALL="${SELF_INSTALL:-/usr/bin/perf.sh}"
BOOT_UNIT="perf-startup-capture.service"

export LD_LIBRARY_PATH="${PERF_LIB}:${LD_LIBRARY_PATH:-}"
mkdir -p "$OUTDIR"
log() { echo "[perfcap] $*"; }

enable_symbols() {
    echo 0  > /proc/sys/kernel/kptr_restrict       2>/dev/null || true
    echo -1 > /proc/sys/kernel/perf_event_paranoid 2>/dev/null || true
}

check_perf() {
    if ! "$PERF" version --build-options 2>/dev/null | grep -q 'dwarf: \[ on'; then
        log "ERROR: perf lacks DWARF support. Deploy the libdw-enabled perf first."
        "$PERF" version --build-options 2>/dev/null | grep -iE 'dwarf|libunwind' | sed 's/^/       /'
        return 1
    fi
    return 0
}

do_capture() {
    check_perf || exit 1
    enable_symbols
    STAMP=$(date +%Y%m%d_%H%M%S)
    DATA="$OUTDIR/startup_${STAMP}.data"
    FULL="$OUTDIR/startup_${STAMP}.full.txt"
    MON="$OUTDIR/startup_${STAMP}.monitor.txt"

    log "Waiting for the $WPE_PROC process to appear..."
    _pid=""
    _tries=0
    while [ -z "$_pid" ]; do
        for _p in $(pgrep -x "$WPE_PROC" 2>/dev/null | sort -n); do
            _pid="$_p"
            break
        done
        [ -n "$_pid" ] && break
        _tries=$((_tries + 1))
        [ "$_tries" -gt 600000 ] && { log "ERROR: timed out waiting for $WPE_PROC pid"; exit 1; }
        usleep 5000 2>/dev/null || sleep 1
    done
    log "Attached to $WPE_PROC pid=$_pid; recording ${DURATION}s (dwarf,${STACKSZ})."

    # Sample the whole WPEFramework process; the decode step below filters to the
    # Monitor::IResou thread so we still keep the startup path focused.
    "$PERF" record -o "$DATA" -F "$FREQ" --pid "$_pid" \
            --call-graph dwarf,"$STACKSZ" -- sleep "$DURATION"

    log "Decoding on target..."
    "$PERF" script -i "$DATA" > "$FULL"
    awk '/^[A-Za-z]/{keep=($0 ~ /Monitor::IResou/)} {if(keep)print}' "$FULL" > "$MON"

    _n=$(grep -c "^$COMM" "$MON" 2>/dev/null || echo 0)
    log "monitor script: $MON  (${_n} samples)"
    awk '/^[^ \t]/{if(w>m)m=w;w=0;next}/WPEFramework/{w++}END{if(w>m)m=w;print "[perfcap] max_WPE_frames="m}' "$MON"
    echo "[perfcap] setup-path hits: $(grep -cE 'PluginHost::Server::Channel|Web::Request::Deserializer|WebSocketLinkType' "$MON")"
    log "Copy to Mac then: stackcollapse-perf.pl $(basename "$MON") | flamegraph.pl > startup.svg"
}

# Capture-only. Boot arming is handled by the separate unit file
# perf-startup-capture.service (install it with systemctl).
do_capture

