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
    raw="$1"; comm="$2"; valid_tids="${3:-}"
    awk -v want="$comm" -v vtids="$valid_tids" '
        # Build lookup of valid TIDs (WPEFramework main daemon only)
        BEGIN {
            ntids = split(vtids, ta)
            for (i=1;i<=ntids;i++) valid[ta[i]+0] = 1
        }
        function tid_ok(p) {
            if (ntids == 0) return 1      # no filter: accept all (boot mode fallback)
            return (p+0 in valid)
        }
        function ts(field,   n) { gsub(/:/,"",field); return field+0 }
        # trace line time is the 4th whitespace field like "  1234.567890:"
        {
            # locate timestamp token (ends with ":")
            t=0
            for (i=1;i<=NF;i++) if ($i ~ /^[0-9]+\.[0-9]+:$/) { ttok=$i; sub(/:$/,"",ttok); t=ttok+0; break }
            if (t==0) next
        }
        /sched_wakeup(_new)?:/ {
            name=""; pid=""
            if (index($0,"comm=")>0){
                s=substr($0,index($0,"comm=")+5)
                p=index(s," pid=")
                if (p>0) name=substr(s,1,p-1)
            }
            for (i=1;i<=NF;i++) if ($i ~ /^pid=/) pid=substr($i,5)+0
            if (name==want){
                if (tid_ok(pid)) {
                    wake[pid]=t; wcnt[pid]++
                    if (first_wake==0) first_wake=t
                } else {
                    filtered_tids[pid]=1
                }
            }
            next
        }
        /sched_switch:/ {
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
                d=(t-wake[npid])*1000000.0
                delete wake[npid]
                if (d>=0){
                    n++; sum+=d
                    last_switch=t
                    if (n==1){ first=d; first_tid=npid; first_ts=t }
                    if (d>max){max=d; maxpid=npid}
                    if (min==0 || d<min) min=d
                    print "  wakeup->run  tid="npid"  latency="sprintf("%.1f",d)" us"
                }
            } else if (nname==want && tid_ok(npid)==0) {
                filtered_tids[npid]=1
            }
            next
        }
        /sched_stat_wait:/ {
            name=""; dl=0
            if (index($0,"comm=")>0){
                s=substr($0,index($0,"comm=")+5); p=index(s," pid=")
                if(p>0) name=substr(s,1,p-1)
            }
            for (i=1;i<=NF;i++) if ($i ~ /^delay=/) dl=substr($i,7)+0
            if (name==want){ sw_n++; sw_sum+=dl; if(dl>sw_max) sw_max=dl }
            next
        }
        /sched_stat_sleep:/ {
            name=""; dl=0
            if (index($0,"comm=")>0){
                s=substr($0,index($0,"comm=")+5); p=index(s," pid=")
                if(p>0) name=substr(s,1,p-1)
            }
            for (i=1;i<=NF;i++) if ($i ~ /^delay=/) dl=substr($i,7)+0
            if (name==want){ sl_n++; sl_sum+=dl; if(dl>sl_max) sl_max=dl }
            next
        }
        /sched_stat_blocked:/ {
            name=""; dl=0
            if (index($0,"comm=")>0){
                s=substr($0,index($0,"comm=")+5); p=index(s," pid=")
                if(p>0) name=substr(s,1,p-1)
            }
            for (i=1;i<=NF;i++) if ($i ~ /^delay=/) dl=substr($i,7)+0
            if (name==want){ bl_n++; bl_sum+=dl; if(dl>bl_max) bl_max=dl }
            next
        }
        /sched_stat_iowait:/ {
            name=""; dl=0
            if (index($0,"comm=")>0){
                s=substr($0,index($0,"comm=")+5); p=index(s," pid=")
                if(p>0) name=substr(s,1,p-1)
            }
            for (i=1;i<=NF;i++) if ($i ~ /^delay=/) dl=substr($i,7)+0
            if (name==want){ io_n++; io_sum+=dl; if(dl>io_max) io_max=dl }
            next
        }
        /sched_stat_runtime:/ {
            name=""; dl=0
            if (index($0,"comm=")>0){
                s=substr($0,index($0,"comm=")+5); p=index(s," pid=")
                if(p>0) name=substr(s,1,p-1)
            }
            for (i=1;i<=NF;i++) if ($i ~ /^runtime=/) dl=substr($i,9)+0
            if (name==want){ rt_n++; rt_sum+=dl; if(dl>rt_max) rt_max=dl }
            next
        }
        END{
            print ""
            print "======================================================"
            print "  WPEFramework Monitor::IResource thread latency"
            print "======================================================"
            if (n>0){
                window_s = last_switch - first_wake
                total_ms = sum / 1000.0
                print ""
                print ">>> FIRST-TIME STARTUP RUN-QUEUE LATENCY <<<"
                printf "    Thread   : WPEFramework Monitor::IResource  (tid %s)\n", first_tid
                printf "    Latency  : %.1f us\n", first
                printf "    At boot  : t = %.6f s (kernel uptime when first scheduled)\n", first_ts
                print ""
                print "--- All wakeup->run pairs ---"
                printf "    total    : %d  (time window: %.3f s  [%.3f s - %.3f s])\n", \
                       n, window_s, first_wake, last_switch
                printf "    min      : %.1f us\n", min
                printf "    avg      : %.1f us\n", sum/n
                printf "    max      : %.1f us  (tid %s)\n", max, maxpid
                printf "    total accumulated wait : %.3f ms\n", total_ms
                printf "      -> Monitor::IResource spent %.3f ms waiting for a CPU\n", total_ms
                printf "         in %.3f s window  (%.2f%% of window time was CPU starvation)\n", \
                       window_s, (window_s>0) ? (total_ms/1000.0/window_s*100) : 0
            } else {
                print "No wakeup->run pairs captured for "want"."
                print "  * Check the thread name (COMM=...) and that Thunder started"
                print "    inside the trace window."
            }
            if (length(filtered_tids)>0) {
                print ""
                print "--- Filtered out (not in main WPEFramework process) ---"
                for (t in filtered_tids)
                    printf "    tid %s excluded (WPEProcess plugin host or recreated thread)\n", t
            }
            print ""
            print "--- Thread time breakdown (CONFIG_SCHEDSTATS events) ---"
            if (rt_n>0)
                printf "    CPU runtime        : n=%d  total=%.3f ms  avg=%.1f us  max=%.1f us\n", \
                       rt_n, rt_sum/1e6, (rt_sum/rt_n)/1000.0, rt_max/1000.0
            else print "    CPU runtime        : no sched_stat_runtime events (need CONFIG_SCHEDSTATS)"
            if (sw_n>0)
                printf "    Run-queue wait     : n=%d  total=%.3f ms  avg=%.1f us  max=%.1f us\n", \
                       sw_n, sw_sum/1e6, (sw_sum/sw_n)/1000.0, sw_max/1000.0
            else print "    Run-queue wait     : no sched_stat_wait events"
            if (sl_n>0)
                printf "    Voluntary sleep    : n=%d  total=%.3f ms  avg=%.1f us  max=%.1f us\n", \
                       sl_n, sl_sum/1e6, (sl_sum/sl_n)/1000.0, sl_max/1000.0
            else print "    Voluntary sleep    : no sched_stat_sleep events"
            if (bl_n>0)
                printf "    Blocked (lock/I/O) : n=%d  total=%.3f ms  avg=%.1f us  max=%.1f us\n", \
                       bl_n, bl_sum/1e6, (bl_sum/bl_n)/1000.0, bl_max/1000.0
            else print "    Blocked (lock/I/O) : no sched_stat_blocked events"
            if (io_n>0)
                printf "    I/O wait           : n=%d  total=%.3f ms  avg=%.1f us  max=%.1f us\n", \
                       io_n, io_sum/1e6, (io_sum/io_n)/1000.0, io_max/1000.0
            else print "    I/O wait           : no sched_stat_iowait events"
            print "======================================================"
        }
    ' "$raw"

    # EPG UI boot milestone: read from RDK standard milestone file
    log ""
    log "--- Boot-to-EPG UI milestone ---"
    _ms_file="/opt/logs/rdk_milestones.log"
    _epg_ms=$(grep "^EPG_FIRST_FRAME:" "$_ms_file" 2>/dev/null | \
              awk -F: '{print $2+0; exit}')
    if [ -n "$_epg_ms" ] && [ "$_epg_ms" -gt 0 ] 2>/dev/null; then
        _epg_sec=$(awk "BEGIN{printf \"%.3f\", $_epg_ms/1000.0}")
        log "  EPG_FIRST_FRAME : ${_epg_ms} ms  =  ${_epg_sec} s  after boot"
        log "  (source: $_ms_file)"
        # Also print other available milestones for context
        log ""
        log "  Other RDK milestones (ms since boot):"
        grep -E "^(BOOT_|WPE|THUNDER|TUNER|CHANNEL|JSPP|UI_)" "$_ms_file" 2>/dev/null | \
            awk -F: '{printf "    %-35s : %s ms  (%.3f s)\n", $1, $2, $2/1000.0}'
    else
        log "  EPG_FIRST_FRAME not found in $_ms_file"
        log "  Available milestones:"
        cat "$_ms_file" 2>/dev/null | \
            awk -F: '{printf "    %-35s : %s ms  (%.3f s)\n", $1, $2, $2/1000.0}' | head -20
        log "  (if empty, milestone file not yet written — run show-boot after EPG appears)"
    fi
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
SELF_INSTALL="${SELF_INSTALL:-/opt/resource_monitor_runq_latency.sh}"
BOOT_UNIT=runq-boottrace.service
BOOT_UNIT_PATH="/etc/systemd/system/${BOOT_UNIT}"

# Enable a single sched event with an ftrace filter, tolerant of old kernels.
enable_filtered() {
    _ev="$1"; _filt="$2"
    [ -d "$TRACEFS/events/sched/$_ev" ] || return 0
    echo "$_filt" > "$TRACEFS/events/sched/$_ev/filter" 2>/dev/null
    echo 1        > "$TRACEFS/events/sched/$_ev/enable" 2>/dev/null
}

# Resolve the TID of Monitor::IResou across ALL WPEFramework processes
# (main daemon + WPEProcess plugin hosts, since Monitor plugin may run OOP).
resolve_target_tid() {
    for _p in $(pgrep -x "$WPE_PROC" 2>/dev/null); do
        for _t in /proc/"$_p"/task/*; do
            if grep -q "$COMM" "$_t/comm" 2>/dev/null; then
                echo "$(basename "$_t")"; return 0
            fi
        done
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
    echo 4096 > "$TRACEFS/buffer_size_kb" 2>/dev/null

    if [ -n "${TARGET_TID:-}" ]; then
        # TID known (live/run mode): filter by exact TID — only this thread
        enable_filtered sched_wakeup       "pid == $TARGET_TID"
        enable_filtered sched_wakeup_new   "pid == $TARGET_TID"
        enable_filtered sched_switch       "next_pid == $TARGET_TID"
        enable_filtered sched_stat_wait    "pid == $TARGET_TID"
        enable_filtered sched_stat_sleep   "pid == $TARGET_TID"
        enable_filtered sched_stat_blocked "pid == $TARGET_TID"
        enable_filtered sched_stat_iowait  "pid == $TARGET_TID"
        enable_filtered sched_stat_runtime "pid == $TARGET_TID"
        echo 1 > "$TRACEFS/tracing_on"
        log "ftrace armed: WPEFramework $WPE_PROC tid=$TARGET_TID ($COMM) — TID-exact filter."
    else
        # Boot mode: thread not created yet, use comm filter until TID is assigned
        enable_filtered sched_wakeup       "comm == \"$COMM\""
        enable_filtered sched_wakeup_new   "comm == \"$COMM\""
        enable_filtered sched_switch       "next_comm == \"$COMM\""
        enable_filtered sched_stat_wait    "comm == \"$COMM\""
        enable_filtered sched_stat_sleep   "comm == \"$COMM\""
        enable_filtered sched_stat_blocked "comm == \"$COMM\""
        enable_filtered sched_stat_iowait  "comm == \"$COMM\""
        enable_filtered sched_stat_runtime "comm == \"$COMM\""
        echo 1 > "$TRACEFS/tracing_on"
        log "ftrace armed: comm='$COMM' filter (boot mode — TID not yet known)."
    fi
}

# Runs at boot from the systemd unit.
arm_boot() {
    arm_filtered_trace || exit 1
    echo "armed $(date)" > "$OUTDIR/runq_boot_armed.txt" 2>/dev/null
}

# Measure ONLY the running WPEFramework Monitor::IResource thread (no restart).
run_live() {
    # TARGET_TID already set by lock_target_tid before this is called
    _mpid=$(pgrep -x "$WPE_PROC" 2>/dev/null | sort -n | head -n1)
    log "Target: $WPE_PROC pid=$_mpid  tid=$TARGET_TID  comm=$COMM"
    arm_filtered_trace || exit 1
    log "Tracing this thread for ${DURATION}s ..."
    sleep "$DURATION"
    echo 0 > "$TRACEFS/tracing_on" 2>/dev/null
    cp "$TRACEFS/trace" "$RAW" 2>/dev/null
    : > "$TRACEFS/set_event" 2>/dev/null
    log "Raw trace saved: $RAW"
    parse_ftrace "$RAW" "$COMM" | tee "$REPORT"
    log "Report: $REPORT"
}

# Pick a systemd unit directory that is actually writable & persistent.
# RDK rootfs is often read-only, so /etc/systemd/system may be RO.
pick_unit_dir() {
    for _d in $(systemctl show -p UnitPath --value 2>/dev/null); do
        case "$_d" in
            /run/*|/dev/*|*/generator*) continue ;;   # skip volatile/generated
        esac
        if mkdir -p "$_d" 2>/dev/null && ( : > "$_d/.w_test" ) 2>/dev/null; then
            rm -f "$_d/.w_test"; echo "$_d"; return 0
        fi
    done
    return 1
}

install_boot() {
    command -v systemctl >/dev/null 2>&1 || { log "systemd required for boot mode."; exit 1; }
    _dir=$(pick_unit_dir) || {
        log "ERROR: no writable+persistent systemd unit dir (rootfs read-only)."
        log "       Available search paths:"
        systemctl show -p UnitPath --value 2>/dev/null | tr ' ' '\n' | sed 's/^/         /'
        log "       -> use MANUAL reboot capture instead (see 'boot-cmdline' below),"
        log "          or tell me a writable dir and set UNIT_DIR=<dir>."
        exit 1
    }
    [ -n "${UNIT_DIR:-}" ] && _dir="$UNIT_DIR"
    BOOT_UNIT_PATH="$_dir/$BOOT_UNIT"
    # stable copy of this script the unit can exec after reboot
    if [ "$0" != "$SELF_INSTALL" ]; then
        cp "$0" "$SELF_INSTALL" 2>/dev/null && chmod +x "$SELF_INSTALL"
    fi
    if ! cat > "$BOOT_UNIT_PATH" <<EOF
[Unit]
Description=Arm ftrace for ResourceMonitor run-queue latency across boot
DefaultDependencies=no
After=sysinit.target local-fs.target sys-kernel-tracing.mount sys-kernel-debug.mount
Before=${THUNDER_SVC}.service
ConditionPathExists=${SELF_INSTALL}

[Service]
Type=oneshot
RemainAfterExit=yes
Environment=COMM=${COMM} OUTDIR=${OUTDIR}
ExecStart=/bin/sh ${SELF_INSTALL} arm-boot

[Install]
WantedBy=sysinit.target
EOF
    then
        log "ERROR: failed to write unit to $BOOT_UNIT_PATH (read-only?)."
        exit 1
    fi
    systemctl daemon-reload
    if ! systemctl enable "$BOOT_UNIT" >/dev/null 2>&1; then
        # enable may fail if [Install] target dir is RO; add an explicit wants symlink
        _wants="$_dir/sysinit.target.wants"
        mkdir -p "$_wants" 2>/dev/null && ln -sf "$BOOT_UNIT_PATH" "$_wants/$BOOT_UNIT" 2>/dev/null
    fi
    systemctl daemon-reload
    if systemctl is-enabled "$BOOT_UNIT" >/dev/null 2>&1; then
        log "Installed & enabled $BOOT_UNIT at $BOOT_UNIT_PATH (arms before ${THUNDER_SVC}.service)."
    else
        log "WARNING: unit written to $BOOT_UNIT_PATH but could not confirm enable."
        log "         Check: systemctl status $BOOT_UNIT"
    fi
    log "Now reboot the STB:   reboot"
    log "After it boots, run:   sh ${SELF_INSTALL} show-boot"
}

show_boot() {
    if [ ! -d "$TRACEFS" ]; then log "tracefs not found."; exit 1; fi
    echo 0 > "$TRACEFS/tracing_on" 2>/dev/null
    cp "$TRACEFS/trace" "$RAW" 2>/dev/null

    # Determine which TIDs belong to ANY WPEFramework process (daemon + WPEProcess hosts)
    _valid_tids=""
    for _p in $(pgrep -x "$WPE_PROC" 2>/dev/null); do
        for _t in /proc/"$_p"/task/*; do
            if grep -q "$COMM" "$_t/comm" 2>/dev/null; then
                _tid=$(basename "$_t")
                _valid_tids="$_valid_tids $_tid"
                log "  Found: pid=$_p tid=$_tid comm=$COMM"
            fi
        done
    done

    log "Boot trace dumped: $RAW"
    parse_ftrace "$RAW" "$COMM" "$_valid_tids" | tee "$REPORT"
    log "Report: $REPORT"
    log "Re-arm for another reboot is automatic (unit still enabled)."
    log "Remove with:  sh $0 uninstall-boot"
}

uninstall_boot() {
    if command -v systemctl >/dev/null 2>&1; then
        systemctl disable "$BOOT_UNIT" >/dev/null 2>&1
    fi
    rm -f "$BOOT_UNIT_PATH"
    command -v systemctl >/dev/null 2>&1 && systemctl daemon-reload
    echo 0 > "$TRACEFS/tracing_on" 2>/dev/null
    : > "$TRACEFS/set_event"       2>/dev/null
    log "Removed $BOOT_UNIT and disabled tracing."
}

# ===========================================================================
# Resolve and lock onto the exact WPEFramework Monitor::IResource TID.
# Called once at startup for live/run modes. Fails if thread not found.
# Sets TARGET_TID globally so every subsequent operation uses that one TID.
# ===========================================================================
lock_target_tid() {
    _all_pids=$(pgrep -x "$WPE_PROC" 2>/dev/null)
    if [ -z "$_all_pids" ]; then
        log "ERROR: $WPE_PROC is not running. Start Thunder first."
        exit 1
    fi

    # If user supplied TARGET_TID, find which pid owns it and verify comm
    if [ -n "${TARGET_TID:-}" ]; then
        for _mpid in $_all_pids; do
            _comm=$(cat "/proc/$_mpid/task/$TARGET_TID/comm" 2>/dev/null)
            if [ "$_comm" = "$COMM" ]; then
                log "Using user-supplied TARGET_TID=$TARGET_TID  comm=$_comm  pid=$_mpid"
                return 0
            fi
        done
        log "ERROR: TARGET_TID=$TARGET_TID comm '$COMM' not found in any $WPE_PROC process"
        exit 1
    fi

    # Auto-resolve: search ALL WPEFramework processes (daemon + WPEProcess hosts)
    TARGET_TID=""
    _owner_pid=""
    for _p in $(pgrep -x "$WPE_PROC" 2>/dev/null); do
        for _t in /proc/"$_p"/task/*; do
            if grep -q "$COMM" "$_t/comm" 2>/dev/null; then
                TARGET_TID=$(basename "$_t")
                _owner_pid="$_p"
                break 2
            fi
        done
    done

    if [ -z "$TARGET_TID" ]; then
        log "ERROR: '$COMM' thread not found in any $WPE_PROC process."
        log "       Running WPEFramework processes: $_all_pids"
        exit 1
    fi

    # Race guard
    _verify=$(cat "/proc/$_owner_pid/task/$TARGET_TID/comm" 2>/dev/null)
    if [ "$_verify" != "$COMM" ]; then
        log "ERROR: TID $TARGET_TID comm changed to '$_verify' (race). Re-run."
        exit 1
    fi

    log "Locked target:"
    log "  Process : $WPE_PROC  pid=$_owner_pid"
    log "  Thread  : $COMM  (full: Monitor::IResource)"
    log "  TID     : $TARGET_TID"
    log "  Verified: /proc/$_mpid/task/$TARGET_TID/comm = '$_verify'"
    export TARGET_TID
}

# ===========================================================================
main() {
    ACTION="${1:-run}"
    case "$ACTION" in
        install-boot)   install_boot ;;
        arm-boot)       arm_boot ;;
        show-boot)      show_boot ;;
        uninstall-boot) uninstall_boot ;;
        live)
            lock_target_tid
            run_live ;;
        run)
            lock_target_tid
            log "COMM='$COMM'  TID=$TARGET_TID  METHOD=$METHOD  DURATION=${DURATION}s"
            case "$METHOD" in
                ftrace) run_ftrace ;;
                perf)   run_perf ;;
                *) log "Unknown METHOD='$METHOD' (use ftrace|perf)"; exit 2 ;;
            esac
            log "Report: $REPORT"
            ;;
        *) log "Unknown action '$ACTION' (use: run|install-boot|show-boot|uninstall-boot)"; exit 2 ;;
    esac
}
main "$@"
