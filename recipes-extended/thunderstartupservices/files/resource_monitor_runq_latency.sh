#!/bin/sh
# ---------------------------------------------------------------------------
# resource_monitor_runq_latency.sh
#
# Measure CPU run-queue latency (time from "thread becomes runnable" to
# "thread actually gets the CPU") for the Thunder ResourceMonitor thread
# during Thunder start-up.
#
# The ResourceMonitor worker thread is named "Monitor::IResource" but Linux
# truncates comm to 15 chars, so it shows up as "Monitor::IResou".
#
# Two independent back-ends are provided; pick whichever your image ships:
#
#   METHOD=ftrace  (default) - uses /sys/kernel/debug/tracing, no extra pkg.
#                              Correlates sched_wakeup -> sched_switch and,
#                              if CONFIG_SCHEDSTATS is on, also reports the
#                              kernel's own sched_stat_wait delay.
#   METHOD=perf              - uses `perf sched record` + `perf sched latency`.
#
# Because the thread does not exist yet at boot, tracing is armed FIRST and
# Thunder is (re)started while the tracer runs, so the very first scheduling
# events of the thread are captured.
#
# Usage (Thunder service restart, thread re-created without a reboot):
#   ./resource_monitor_runq_latency.sh                 # ftrace, auto-restart Thunder
#   METHOD=perf ./resource_monitor_runq_latency.sh
#   DURATION=45 ./resource_monitor_runq_latency.sh     # trace window (s)
#   NO_RESTART=1 ./resource_monitor_runq_latency.sh    # you start Thunder yourself
#   COMM='Monitor::IResou' ./resource_monitor_runq_latency.sh
#
# Usage (FULL STB REBOOT - arms a boot-time systemd unit):
#   ./resource_monitor_runq_latency.sh install-boot    # install+enable, then reboot
#   reboot
#   ./resource_monitor_runq_latency.sh show-boot        # after login: dump+compute
#   ./resource_monitor_runq_latency.sh uninstall-boot   # remove the boot unit
# ---------------------------------------------------------------------------
set -u

# ---- tunables --------------------------------------------------------------
COMM="${COMM:-Monitor::IResou}"          # truncated (<=15 char) thread name
METHOD="${METHOD:-ftrace}"               # ftrace | perf
DURATION="${DURATION:-30}"               # seconds to trace during start-up
OUTDIR="${OUTDIR:-/opt/logs}"
NO_RESTART="${NO_RESTART:-0}"            # 1 = do not touch Thunder, trace only
THUNDER_SVC="${THUNDER_SVC:-wpeframework}"  # systemd/service name if present
WPE_PROC="${WPE_PROC:-WPEFramework}"     # process that owns the ResourceMonitor thread
TARGET_TID="${TARGET_TID:-}"             # override: trace exactly this TID

# ---- locate a *mounted* tracefs (dir must actually contain tracing_on) -----
# Some images ship an empty /sys/kernel/tracing stub, so test the file, and
# mount tracefs/debugfs if nothing is available yet.
resolve_tracefs() {
    for _d in /sys/kernel/tracing /sys/kernel/debug/tracing; do
        [ -f "$_d/tracing_on" ] && { echo "$_d"; return 0; }
    done
    # nothing mounted -> try to mount
    mount -t tracefs nodev /sys/kernel/tracing 2>/dev/null
    [ -f /sys/kernel/tracing/tracing_on ] && { echo /sys/kernel/tracing; return 0; }
    mount -t debugfs nodev /sys/kernel/debug 2>/dev/null
    [ -f /sys/kernel/debug/tracing/tracing_on ] && { echo /sys/kernel/debug/tracing; return 0; }
    return 1
}
TRACEFS="$(resolve_tracefs || echo /sys/kernel/debug/tracing)"

mkdir -p "$OUTDIR"
STAMP=$(date +%Y%m%d_%H%M%S)
RAW="$OUTDIR/runq_${METHOD}_${STAMP}.txt"
REPORT="$OUTDIR/runq_latency_${STAMP}.txt"

log() { echo "[runq] $*"; }

# ---- restart Thunder so we capture its start-up ----------------------------
# Candidate systemd/init service names seen across RDK-V / RDKE images.
THUNDER_CANDIDATES="${THUNDER_SVC} wpeframework Thunder wpeframework-2 thunder rdkservices"

# Resolve the real systemd unit that owns WPEFramework, if any.
detect_systemd_unit() {
    command -v systemctl >/dev/null 2>&1 || return 1
    # 1) unit that currently owns the running WPEFramework PID
    _pid=$(pgrep -x WPEFramework 2>/dev/null | head -n1)
    if [ -n "$_pid" ]; then
        _u=$(systemctl status "$_pid" 2>/dev/null | awk 'NR==1{print $2; exit}')
        case "$_u" in *.service) echo "${_u%.service}"; return 0;; esac
    fi
    # 2) fall back to a name match among known candidates
    for _c in $THUNDER_CANDIDATES; do
        if systemctl list-unit-files 2>/dev/null | grep -qi "^${_c}\.service"; then
            echo "$_c"; return 0
        fi
    done
    return 1
}

restart_thunder() {
    if [ "$NO_RESTART" = "1" ]; then
        log "NO_RESTART=1: arm tracer, then (re)start Thunder yourself."
        return
    fi
    _init=$(cat /proc/1/comm 2>/dev/null)
    log "Restarting Thunder (init=${_init:-unknown}) to capture start-up..."

    # 1) systemd
    if _unit=$(detect_systemd_unit); then
        log "systemd unit: ${_unit}.service"
        systemctl restart "$_unit" && return
    fi

    # 2) sysvinit / BusyBox init
    for _c in $THUNDER_CANDIDATES; do
        if [ -x /etc/init.d/"$_c" ]; then
            log "init.d script: /etc/init.d/$_c"
            /etc/init.d/"$_c" restart && return
        fi
    done

    # 3) supervised process (RDK self-heal / minidump respawner):
    #    SIGTERM the daemon and let the supervisor bring it back.
    log "No service manager matched; SIGTERM WPEFramework and rely on supervisor respawn."
    pkill -TERM -x WPEFramework 2>/dev/null || pkill -TERM -f WPEFramework 2>/dev/null
}

# ===========================================================================
# ftrace back-end
# ===========================================================================
run_ftrace() {
    if [ ! -d "$TRACEFS" ]; then
        log "ERROR: tracefs not found at $TRACEFS (need CONFIG_FTRACE)."
        exit 1
    fi

    log "Using ftrace at $TRACEFS"
    # clean slate
    echo 0                 > "$TRACEFS/tracing_on"        2>/dev/null
    echo nop               > "$TRACEFS/current_tracer"    2>/dev/null
    : > "$TRACEFS/trace"                                  2>/dev/null
    : > "$TRACEFS/set_event"                              2>/dev/null

    # enable the scheduler events we need
    echo 1 > "$TRACEFS/events/sched/sched_wakeup/enable"      2>/dev/null
    echo 1 > "$TRACEFS/events/sched/sched_wakeup_new/enable"  2>/dev/null
    echo 1 > "$TRACEFS/events/sched/sched_switch/enable"      2>/dev/null
    # optional: exact kernel-computed run-queue wait (needs CONFIG_SCHEDSTATS)
    if [ -e "$TRACEFS/events/sched/sched_stat_wait/enable" ]; then
        echo 1 > "$TRACEFS/events/sched/sched_stat_wait/enable" 2>/dev/null
        log "sched_stat_wait available (CONFIG_SCHEDSTATS on)."
    fi

    echo 1 > "$TRACEFS/tracing_on"

    restart_thunder

    log "Tracing for ${DURATION}s ..."
    sleep "$DURATION"

    echo 0 > "$TRACEFS/tracing_on"
    cp "$TRACEFS/trace" "$RAW"
    # disable events again
    : > "$TRACEFS/set_event" 2>/dev/null
    log "Raw trace saved: $RAW"

    parse_ftrace "$RAW" "$COMM" | tee "$REPORT"
}

# Parse wakeup->switch pairs and (if present) sched_stat_wait for the thread.
parse_ftrace() {
    raw="$1"; comm="$2"
    awk -v want="$comm" '
        function ts(field,   n) { gsub(/:/,"",field); return field+0 }
        # trace line time is the 4th whitespace field like "  1234.567890:"
        {
            # locate timestamp token (ends with ":")
            t=0
            for (i=1;i<=NF;i++) if ($i ~ /^[0-9]+\.[0-9]+:$/) { ttok=$i; sub(/:$/,"",ttok); t=ttok+0; break }
            if (t==0) next
        }
        /sched_wakeup(_new)?:/ {
            # ... sched_wakeup: comm=NAME pid=PID prio=.. target_cpu=..
            name=""; pid=""
            for (i=1;i<=NF;i++){
                if ($i ~ /^comm=/){ name=substr($i,6) }
                if ($i ~ /^pid=/){ pid=substr($i,5)+0 }
            }
            # comm may contain the ":" so rebuild between comm= and pid=
            if (index($0,"comm=")>0){
                s=substr($0,index($0,"comm=")+5)
                p=index(s," pid=")
                if (p>0) name=substr(s,1,p-1)
            }
            if (name==want){ wake[pid]=t; wcnt[pid]++ }
            next
        }
        /sched_switch:/ {
            # prev.. ==> next_comm=NAME next_pid=PID next_prio=..
            npid=""; nname=""
            k=index($0,"==>")
            if (k>0){
                s=substr($0,k)
                if (index(s,"next_comm=")>0){
                    r=substr(s,index(s,"next_comm=")+10)
                    p=index(r," next_pid=")
                    if (p>0) nname=substr(r,1,p-1)
                }
                if (index(s,"next_pid=")>0){
                    r=substr(s,index(s,"next_pid=")+9)
                    npid=r+0
                }
            }
            if (nname==want && (npid in wake)){
                d=(t-wake[npid])*1000000.0   # s -> us
                delete wake[npid]
                if (d>=0){
                    n++; sum+=d
                    if (n==1){ first=d; first_tid=npid; first_ts=t }
                    if (d>max){max=d; maxpid=npid}
                    if (min==0 || d<min) min=d
                    print "  wakeup->run  tid="npid"  latency="sprintf("%.1f",d)" us"
                }
            }
            next
        }
        /sched_stat_wait:/ {
            # comm=NAME pid=PID delay=NS [ns]
            name=""; dl=0
            if (index($0,"comm=")>0){
                s=substr($0,index($0,"comm=")+5)
                p=index(s," pid=")
                if (p>0) name=substr(s,1,p-1)
            }
            for (i=1;i<=NF;i++) if ($i ~ /^delay=/){ dl=substr($i,7)+0 }
            if (name==want){
                sw_n++; sw_sum+=dl
                if (dl>sw_max) sw_max=dl
            }
            next
        }
        END{
            print ""
            print "=== ResourceMonitor run-queue latency ("want") ==="
            if (n>0){
                printf "wakeup->run pairs : %d\n", n
                printf "  FIRST: %.1f us  (tid %s, first scheduled at t=%.6f)\n", first, first_tid, first_ts
                printf "  min  : %.1f us\n", min
                printf "  avg  : %.1f us\n", sum/n
                printf "  max  : %.1f us  (tid %s)\n", max, maxpid
            } else {
                print "No wakeup->run pairs captured for "want"."
                print "  * Check the thread name (COMM=...) and that Thunder started"
                print "    inside the trace window."
            }
            if (sw_n>0){
                printf "sched_stat_wait   : n=%d  avg=%.1f us  max=%.1f us (kernel runq wait)\n", \
                       sw_n, (sw_sum/sw_n)/1000.0, sw_max/1000.0
            }
        }
    ' "$raw"
}

# ===========================================================================
# perf back-end
# ===========================================================================
run_perf() {
    if ! command -v perf >/dev/null 2>&1; then
        log "ERROR: perf not found. Use METHOD=ftrace instead."
        exit 1
    fi
    PERFDATA="$OUTDIR/sched_${STAMP}.data"
    log "Recording scheduler events with perf for ${DURATION}s ..."

    # arm perf in the background, then restart Thunder so its start-up lands
    # inside the recording window.
    perf sched record -o "$PERFDATA" -- sleep "$DURATION" &
    PERF_PID=$!
    sleep 1
    restart_thunder
    wait "$PERF_PID"

    log "perf data: $PERFDATA"
    {
        echo "=== perf sched latency (filtered: $COMM) ==="
        perf sched latency -i "$PERFDATA" -s max 2>/dev/null | \
            awk 'NR<=2 || $0 ~ /'"$COMM"'/'
        echo ""
        echo "Columns: Task | Runtime ms | Switches | Avg delay ms | Max delay ms | Max delay at"
        echo "'Avg/Max delay' == run-queue (wakeup-to-run) latency."
    } | tee "$REPORT"
}

# ===========================================================================
# BOOT back-end : measure run-queue latency across a full STB reboot.
#
# The Monitor::IResource thread is created very early in boot, so we cannot
# arm the tracer after login. Instead we install a systemd oneshot unit that
# runs BEFORE wpeframework.service and arms *filtered* ftrace (only events
# for COMM). Because the filter drops everything else, the ring buffer holds
# every matching event for the entire boot with no overflow. After the box is
# up you run `show-boot` to stop tracing, dump and compute the latency.
# ===========================================================================
SELF_INSTALL="${SELF_INSTALL:-/usr/bin/resource_monitor_runq_latency.sh}"
BOOT_UNIT=runq-boottrace.service
BOOT_UNIT_PATH="/etc/systemd/system/${BOOT_UNIT}"

# Enable a single sched event with an ftrace filter, tolerant of old kernels.
enable_filtered() {
    _ev="$1"; _filt="$2"
    [ -d "$TRACEFS/events/sched/$_ev" ] || return 0
    echo "$_filt" > "$TRACEFS/events/sched/$_ev/filter" 2>/dev/null
    echo 1        > "$TRACEFS/events/sched/$_ev/enable" 2>/dev/null
}

# Resolve the TID of the ResourceMonitor thread (comm==$COMM) that lives inside
# the WPEFramework daemon. Picks the main daemon (lowest WPEFramework pid) so
# WPEProcess plugin hosts are ignored.
resolve_target_tid() {
    _mpid=$(pgrep -x "$WPE_PROC" 2>/dev/null | sort -n | head -n1)
    [ -n "$_mpid" ] || return 1
    for _c in /proc/"$_mpid"/task/*/comm; do
        [ -r "$_c" ] || continue
        if [ "$(cat "$_c" 2>/dev/null)" = "$COMM" ]; then
            _p=${_c%/comm}; echo "${_p##*/}"; return 0
        fi
    done
    return 1
}

arm_filtered_trace() {
    if [ ! -d "$TRACEFS" ]; then
        log "ERROR: tracefs not found at $TRACEFS (need CONFIG_FTRACE)."
        return 1
    fi
    echo 0   > "$TRACEFS/tracing_on"     2>/dev/null
    echo nop > "$TRACEFS/current_tracer" 2>/dev/null
    : > "$TRACEFS/trace"                 2>/dev/null
    : > "$TRACEFS/set_event"             2>/dev/null
    # bigger-than-needed buffer; filtered volume is tiny anyway
    echo 4096 > "$TRACEFS/buffer_size_kb" 2>/dev/null

    # Prefer exact-thread scoping: the ResourceMonitor thread that lives inside
    # the WPEFramework process. Falls back to comm when the TID is not known
    # yet (e.g. boot, before the thread is created).
    _tid="${TARGET_TID:-$(resolve_target_tid || true)}"
    if [ -n "$_tid" ]; then
        enable_filtered sched_wakeup      "pid == $_tid"
        enable_filtered sched_wakeup_new  "pid == $_tid"
        enable_filtered sched_switch      "next_pid == $_tid"
        enable_filtered sched_stat_wait   "pid == $_tid"
        echo 1 > "$TRACEFS/tracing_on"
        log "Filtered ftrace armed for $WPE_PROC TID=$_tid (comm=$COMM)."
    else
        enable_filtered sched_wakeup      "comm == \"$COMM\""
        enable_filtered sched_wakeup_new  "comm == \"$COMM\""
        enable_filtered sched_switch      "next_comm == \"$COMM\""
        enable_filtered sched_stat_wait   "comm == \"$COMM\""
        echo 1 > "$TRACEFS/tracing_on"
        log "Filtered ftrace armed for COMM='$COMM' (TID not yet known; will match $WPE_PROC's thread on creation)."
    fi
}

# Runs at boot from the systemd unit.
arm_boot() {
    arm_filtered_trace || exit 1
    echo "armed $(date)" > "$OUTDIR/runq_boot_armed.txt" 2>/dev/null
}

# Measure ONLY the running WPEFramework Monitor::IResource thread (no restart).
run_live() {
    _tid="${TARGET_TID:-$(resolve_target_tid || true)}"
    if [ -z "$_tid" ]; then
        log "ERROR: could not find '$COMM' thread inside $WPE_PROC."
        log "       Is Thunder running?  pgrep -x $WPE_PROC"
        exit 1
    fi
    _mpid=$(pgrep -x "$WPE_PROC" 2>/dev/null | sort -n | head -n1)
    log "Target: $WPE_PROC pid=$_mpid  tid=$_tid  comm=$COMM"
    TARGET_TID="$_tid" arm_filtered_trace || exit 1
    log "Tracing this thread for ${DURATION}s ..."
    sleep "$DURATION"
    echo 0 > "$TRACEFS/tracing_on" 2>/dev/null
    cp "$TRACEFS/trace" "$RAW" 2>/dev/null
    : > "$TRACEFS/set_event" 2>/dev/null
    log "Raw trace saved: $RAW"
    parse_ftrace "$RAW" "$COMM" | tee "$REPORT"
    log "Report: $REPORT"
}

UNIT_DIR_ETC=/etc/systemd/system
UNIT_DIR_LOCAL=/usr/local/lib/systemd/system

# Make /etc/systemd/system writable via a tmpfs overlay (squashfs RO rootfs workaround).
# Upper layer is backed by /opt/systemd-etc-overlay which is persistent across reboots.
mount_etc_overlay() {
    _upper=/opt/systemd-etc-overlay/upper
    _work=/opt/systemd-etc-overlay/work
    mkdir -p "$_upper" "$_work"
    # Copy existing unit files into upper so overlay sees them
    cp -a /etc/systemd/system/. "$_upper/" 2>/dev/null || true
    if mount -t overlay overlay \
        -o lowerdir=$UNIT_DIR_ETC,upperdir=$_upper,workdir=$_work \
        $UNIT_DIR_ETC 2>/dev/null; then
        log "Overlay mounted on $UNIT_DIR_ETC (upper=$_upper — persistent in /opt/)"
        return 0
    fi
    return 1
}

# Write the runq-boottrace.service unit file and enable it.
# After=opt.mount    — /opt/ is mounted before we run (confirmed on xione-bcm-flex2).
# Before=wpeframework.service — arms ftrace before WPEFramework binary starts.
# WantedBy=wpeframework.service — systemd auto-starts this unit on every
#   wpeframework start (cold boot OR service restart).
write_unit() {
    _path="$1"
    cat > "$_path" <<EOF
[Unit]
Description=Arm ftrace for WPEFramework ResourceMonitor run-queue latency
DefaultDependencies=no
After=opt.mount
Before=${THUNDER_SVC}.service
ConditionPathExists=${SELF_INSTALL}

[Service]
Type=oneshot
RemainAfterExit=yes
Environment=COMM=${COMM} OUTDIR=${OUTDIR}
ExecStart=/bin/sh ${SELF_INSTALL} arm-boot

[Install]
WantedBy=${THUNDER_SVC}.service
EOF
}

enable_unit() {
    _path="$1"; _dir="$2"
    systemctl daemon-reload
    # Create wpeframework.service.wants symlink directly — works even if
    # 'systemctl enable' cannot write to /etc/systemd/system/.
    _wants="$_dir/${THUNDER_SVC}.service.wants"
    mkdir -p "$_wants" 2>/dev/null
    ln -sf "$_path" "$_wants/$BOOT_UNIT" 2>/dev/null
    systemctl enable "$BOOT_UNIT" >/dev/null 2>&1 || true
    systemctl daemon-reload
    log "Enabled: $BOOT_UNIT  ->  $THUNDER_SVC.service.wants/"
}

install_boot() {
    command -v systemctl >/dev/null 2>&1 || { log "systemd required."; exit 1; }
    [ "$0" != "$SELF_INSTALL" ] && cp "$0" "$SELF_INSTALL" 2>/dev/null && chmod +x "$SELF_INSTALL"

    # --- Try /etc/systemd/system/ first (standard, persistent after overlay) ---
    if ( : > "$UNIT_DIR_ETC/.w_test" ) 2>/dev/null; then
        rm -f "$UNIT_DIR_ETC/.w_test"
        log "/etc/systemd/system/ is directly writable."
        _dir="$UNIT_DIR_ETC"
    else
        log "/etc/systemd/system/ is read-only — mounting tmpfs overlay backed by /opt/ ..."
        if mount_etc_overlay; then
            _dir="$UNIT_DIR_ETC"
        else
            log "Overlay failed. Trying /usr/local/lib/systemd/system/ ..."
            mkdir -p "$UNIT_DIR_LOCAL" 2>/dev/null
            if ( : > "$UNIT_DIR_LOCAL/.w_test" ) 2>/dev/null; then
                rm -f "$UNIT_DIR_LOCAL/.w_test"
                _dir="$UNIT_DIR_LOCAL"
                log "Using $UNIT_DIR_LOCAL (persistent on BCM/Sky)."
            else
                log "WARNING: no persistent dir. Falling back to /run/systemd/system/ (volatile)."
                _dir="/run/systemd/system"
            fi
        fi
    fi

    BOOT_UNIT_PATH="$_dir/$BOOT_UNIT"
    write_unit "$BOOT_UNIT_PATH" || { log "ERROR: write failed to $BOOT_UNIT_PATH"; exit 1; }
    enable_unit "$BOOT_UNIT_PATH" "$_dir"
    log "Service file : $BOOT_UNIT_PATH"
    log "Ordering     : After=opt.mount  Before=${THUNDER_SVC}.service"
    log "Trigger      : WantedBy=${THUNDER_SVC}.service (auto on every wpeframework start)"
    log ""
    log "Now: reboot  ->  sh ${SELF_INSTALL} show-boot"
}

# Re-arm into /run/systemd/system/ — call from any early /opt/ boot hook.
reinstall_boot() {
    log "reinstall: writing $BOOT_UNIT to /run/systemd/system/ ..."
    BOOT_UNIT_PATH="/run/systemd/system/$BOOT_UNIT"
    write_unit "$BOOT_UNIT_PATH"
    enable_unit "$BOOT_UNIT_PATH" "/run/systemd/system"
    systemctl start "$BOOT_UNIT" 2>/dev/null
    log "reinstall done. ftrace armed for COMM=$COMM."
}

show_boot() {
    [ -f "$TRACEFS/tracing_on" ] || { log "tracefs not found."; exit 1; }
    echo 0 > "$TRACEFS/tracing_on" 2>/dev/null
    cp "$TRACEFS/trace" "$RAW" 2>/dev/null
    log "Boot trace dumped: $RAW"
    parse_ftrace "$RAW" "$COMM" | tee "$REPORT"
    log "Report: $REPORT"
    log "Remove hook with:  sh $0 uninstall-boot"
}

uninstall_boot() {
    if command -v systemctl >/dev/null 2>&1; then
        systemctl disable "$BOOT_UNIT" >/dev/null 2>&1
        systemctl stop "$BOOT_UNIT" 2>/dev/null
    fi
    rm -f "/run/systemd/system/$BOOT_UNIT" \
          "$UNIT_DIR_ETC/$BOOT_UNIT" \
          "$UNIT_DIR_LOCAL/$BOOT_UNIT" \
          "$UNIT_DIR_ETC/${THUNDER_SVC}.service.wants/$BOOT_UNIT" \
          "$UNIT_DIR_LOCAL/${THUNDER_SVC}.service.wants/$BOOT_UNIT" \
          "/run/systemd/system/${THUNDER_SVC}.service.wants/$BOOT_UNIT"
    command -v systemctl >/dev/null 2>&1 && systemctl daemon-reload
    # Remove overlay if we mounted one
    umount "$UNIT_DIR_ETC" 2>/dev/null || true
    echo 0 > "$TRACEFS/tracing_on" 2>/dev/null
    : > "$TRACEFS/set_event" 2>/dev/null
    log "Removed $BOOT_UNIT and disabled tracing."
}

# ===========================================================================
main() {
    ACTION="${1:-run}"
    case "$ACTION" in
        install-boot)   install_boot ;;
        arm-boot)       arm_boot ;;
        reinstall)      reinstall_boot ;;
        show-boot)      show_boot ;;
        uninstall-boot) uninstall_boot ;;
        live)           run_live ;;
        run)
            log "COMM='$COMM'  METHOD=$METHOD  DURATION=${DURATION}s  OUTDIR=$OUTDIR"
            case "$METHOD" in
                ftrace) run_ftrace ;;
                perf)   run_perf ;;
                *) log "Unknown METHOD='$METHOD' (use ftrace|perf)"; exit 2 ;;
            esac
            log "Report: $REPORT"
            ;;
        *) log "Unknown action '$ACTION' (use: run|live|install-boot|reinstall|show-boot|uninstall-boot)"; exit 2 ;;
    esac
}
main "$@"
