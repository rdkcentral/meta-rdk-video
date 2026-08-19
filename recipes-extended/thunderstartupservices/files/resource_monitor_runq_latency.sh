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
DEBUG_LOG="${DEBUG_LOG:-/tmp/resource_monitor_runq_latency.debug.log}"
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

debug() {
    printf '[runq-debug] %s\n' "$*" >> "$DEBUG_LOG" 2>/dev/null || \
        printf '[runq-debug] %s\n' "$*" >&2
}

debug "Script started: $0"
debug "Arguments: $*"
debug "UID=$(id -u 2>/dev/null || echo unknown) PID=$$"
debug "OUTDIR=$OUTDIR DEBUG_LOG=$DEBUG_LOG METHOD=$METHOD DURATION=$DURATION"
debug "TRACEFS=$TRACEFS COMM=$COMM WPE_PROC=$WPE_PROC TARGET_TID=${TARGET_TID:-unset}"
debug "PATH=$PATH"

if ! mkdir -p "$OUTDIR" 2>/dev/null; then
    debug "ERROR: cannot create OUTDIR=$OUTDIR"
    exit 1
fi
if [ ! -d "$OUTDIR" ] || [ ! -w "$OUTDIR" ]; then
    debug "ERROR: OUTDIR is missing or not writable: $OUTDIR"
    exit 1
fi
STAMP=$(date +%Y%m%d_%H%M%S)
RAW="$OUTDIR/runq_${METHOD}_${STAMP}.txt"
REPORT="$OUTDIR/runq_latency_${STAMP}.txt"
BOOT_RAW="$OUTDIR/runq_boot_ftrace.txt"
BOOT_REPORT="$OUTDIR/runq_boot_latency.txt"
debug "RAW=$RAW REPORT=$REPORT"
debug "BOOT_RAW=$BOOT_RAW BOOT_REPORT=$BOOT_REPORT"
PRESSURE_PID=""
PRESSURE_LOG=""

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


# ---------------------------------------------------------------------------
# Thread-pressure sampler: records runnable+total thread counts every ~100ms
# using /proc/uptime (kernel-monotonic clock, same reference as ftrace).
# Each line: uptime_s  runnable  total  percpu_rq_sum
# ---------------------------------------------------------------------------
start_thread_pressure_sampler() {
    PRESSURE_LOG="$OUTDIR/thread_pressure_${STAMP}.log"
    (
        while true; do
            read -r _up _ < /proc/uptime 2>/dev/null || _up="0"
            read -r _ _ _ _lf _ < /proc/loadavg 2>/dev/null || _lf="0/0"
            _run=${_lf%/*}
            _tot=${_lf#*/}
            # Sum per-CPU run-queue depths; more precise than loadavg running field
            _rq=$(awk -F: '/nr_running/{s+=$NF+0}END{printf "%d",s}' \
                  /proc/sched_debug 2>/dev/null || echo "?")
            printf '%s %s %s %s\n' "$_up" "$_run" "$_tot" "$_rq"
            sleep 0.1 2>/dev/null || usleep 100000 2>/dev/null || sleep 1
        done
    ) >> "$PRESSURE_LOG" &
    PRESSURE_PID=$!
    debug "Pressure sampler started: pid=$PRESSURE_PID log=$PRESSURE_LOG"
    log "Thread pressure sampler: $PRESSURE_LOG"
}

stop_thread_pressure_sampler() {
    [ -n "$PRESSURE_PID" ] && kill "$PRESSURE_PID" 2>/dev/null
    wait "$PRESSURE_PID" 2>/dev/null
    PRESSURE_PID=""
    debug "Pressure sampler stopped ($(wc -l < "$PRESSURE_LOG" 2>/dev/null || echo 0) samples)"
}

# Append thread-pressure summary and spike-correlation table to the report.
# Correlates each spike (>5ms) with the nearest pressure sample by uptime timestamp.
report_thread_pressure() {
    _plog="$1"; _rfile="$2"
    echo ""
    echo "======================================================"
    echo "  Thread pressure during trace window"
    echo "======================================================"
    if [ ! -s "$_plog" ]; then
        echo "  No pressure samples captured (log: $_plog)"
        echo "======================================================"
        return
    fi
    awk '
    BEGIN { min_r=999999; max_r=0; min_t=999999; max_t=0; n=0; sum_r=0; sum_t=0; first=-1 }
    NF>=3 {
        ts=$1+0; r=$2+0; t=$3+0
        n++; sum_r+=r; sum_t+=t
        if (first<0) first=ts
        last=ts
        if (r>max_r) { max_r=r; max_r_ts=ts }
        if (r<min_r) min_r=r
        if (t>max_t) max_t=t
        if (t<min_t) min_t=t
    }
    END {
        if (n==0) { print "  No samples."; exit }
        w=last-first
        printf "  Samples    : %d  (%.1f s window,  ~%.0f ms avg interval)\n", n, w, w*1000.0/(n>1?n-1:1)
        printf "  Runnable   : min=%-4d  avg=%-5.1f  max=%d  (at t=%.2f s)\n", min_r, sum_r/n, max_r, max_r_ts
        printf "  Total thds : min=%-4d  avg=%-5.1f  max=%d\n", min_t, sum_t/n, max_t
    }' "$_plog"
    printf "  Pressure log: %s\n" "$_plog"

    [ -f "$_rfile" ] || { echo "======================================================"; return; }
    awk -v plog="$_plog" '
    BEGIN {
        while ((getline line < plog) > 0) {
            n=split(line,a); if (n<3) continue
            np++; pts[np]=a[1]+0; prun[np]=a[2]+0; ptot[np]=a[3]+0
            prq[np]=(n>=4)?a[4]:"N/A"
        }
        close(plog); ns=0
    }
    /wakeup->run.*latency=/ {
        match($0,/latency=([0-9.]+)/,la); lat=la[1]+0
        if (lat<5000) next
        match($0,/ts=([0-9.]+)/,ta); spike_t=ta[1]+0
        if (spike_t==0) next
        best=1; bd=9e18
        for (i=1;i<=np;i++) { d=pts[i]-spike_t; if(d<0) d=-d; if(d<bd){bd=d;best=i} }
        ns++
        lats[ns]=lat; runs[ns]=np?prun[best]:"N/A"
        tots[ns]=np?ptot[best]:"N/A"; rqs[ns]=np?prq[best]:"N/A"; dts[ns]=bd*1000
    }
    END {
        print ""
        if (ns==0) {
            print "  No spikes >5ms with ts= field found (re-run script to regenerate report)."
            exit
        }
        print "--- Spikes >5ms correlated with thread pressure ---"
        printf "  %-11s  %-8s  %-7s  %-10s  %s\n", "Latency","Runnable","Total","percpu_rq","Sample offset"
        printf "  %-11s  %-8s  %-7s  %-10s  %s\n", "-----------","--------","-------","----------","-------------"
        for (i=1;i<=ns;i++)
            printf "  %-11s  %-8s  %-7s  %-10s  %.0f ms\n", \
                   sprintf("%.1f ms",lats[i]/1000.0), runs[i], tots[i], rqs[i], dts[i]
    }' "$_rfile"
    echo "======================================================"
}

# ===========================================================================
# ftrace back-end
# ===========================================================================
run_ftrace() {
    debug "run_ftrace: entering with TRACEFS=$TRACEFS"
    if [ ! -d "$TRACEFS" ]; then
        log "ERROR: tracefs not found at $TRACEFS (need CONFIG_FTRACE)."
        debug "ERROR: tracefs directory does not exist: $TRACEFS"
        exit 1
    fi

    for _trace_file in tracing_on current_tracer trace set_event; do
        if [ ! -e "$TRACEFS/$_trace_file" ]; then
            debug "WARN: missing tracefs file: $TRACEFS/$_trace_file"
        fi
    done

    log "Using ftrace at $TRACEFS"
    # clean slate
    echo 0   > "$TRACEFS/tracing_on"     2>/dev/null
    echo nop > "$TRACEFS/current_tracer" 2>/dev/null
    : > "$TRACEFS/trace"                 2>/dev/null
    : > "$TRACEFS/set_event"             2>/dev/null

    # Thunder is about to be restarted, so the Monitor::IResource thread will be
    # re-created with a NEW tid. We therefore arm a name (comm) filter now and
    # restrict to the WPEFramework daemon's tid(s) at parse time (see below).
    enable_filtered sched_wakeup       "comm == \"$COMM\""
    enable_filtered sched_wakeup_new   "comm == \"$COMM\""
    enable_filtered sched_switch       "next_comm == \"$COMM\""
    enable_filtered sched_stat_wait    "comm == \"$COMM\""
    enable_filtered sched_stat_sleep   "comm == \"$COMM\""
    enable_filtered sched_stat_blocked "comm == \"$COMM\""
    enable_filtered sched_stat_iowait  "comm == \"$COMM\""
    enable_filtered sched_stat_runtime "comm == \"$COMM\""

    if ! echo 1 > "$TRACEFS/tracing_on" 2>/dev/null; then
        debug "ERROR: failed to enable tracing: $TRACEFS/tracing_on"
        exit 1
    fi
    debug "ftrace enabled; restarting Thunder"

    #restart_thunder

    log "Tracing for ${DURATION}s ..."
    start_thread_pressure_sampler
    sleep "$DURATION"
    stop_thread_pressure_sampler

    echo 0 > "$TRACEFS/tracing_on"
    if ! cp "$TRACEFS/trace" "$RAW" 2>/dev/null; then
        debug "ERROR: failed to copy $TRACEFS/trace to $RAW"
        exit 1
    fi
    debug "Raw trace copied: $RAW bytes=$(wc -c < "$RAW" 2>/dev/null || echo unknown)"
    # disable events again
    : > "$TRACEFS/set_event" 2>/dev/null
    log "Raw trace saved: $RAW"

    # Keep ONLY the WPEFramework daemon's Monitor::IResource tid(s), resolved now
    # (after the restart, so the tid is the freshly-created one).
    _daemon_tids=$(resolve_daemon_tids)
    if [ -n "$_daemon_tids" ]; then
        log "Restricting report to WPEFramework daemon tid(s): $_daemon_tids"
    else
        log "WARN: could not resolve WPEFramework daemon tid; reporting all '$COMM' threads."
    fi

    if ! parse_ftrace "$RAW" "$COMM" "$_daemon_tids" > "$REPORT" 2>>"$DEBUG_LOG"; then
        debug "ERROR: parse_ftrace failed for $RAW"
        exit 1
    fi
    if [ ! -s "$REPORT" ]; then
        debug "WARN: report was created but is empty: $REPORT"
    else
        debug "Report saved: $REPORT bytes=$(wc -c < "$REPORT" 2>/dev/null || echo unknown)"
        cat "$REPORT"
        report_thread_pressure "$PRESSURE_LOG" "$REPORT" | tee -a "$REPORT"
    fi
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
                    print "  wakeup->run  tid="npid"  latency="sprintf("%.1f",d)" us  ts="sprintf("%.6f",t)
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
    debug "run_perf: entering; OUTDIR=$OUTDIR"
    if ! command -v perf >/dev/null 2>&1; then
        log "ERROR: perf not found. Use METHOD=ftrace instead."
        debug "ERROR: perf command not found"
        exit 1
    fi
    PERFDATA="$OUTDIR/sched_${STAMP}.data"
    log "Recording scheduler events with perf for ${DURATION}s ..."

    # arm perf in the background, then restart Thunder so its start-up lands
    # inside the recording window.
    perf sched record -o "$PERFDATA" -- sleep "$DURATION" &
    PERF_PID=$!
    sleep 1
    #restart_thunder
    wait "$PERF_PID"
    debug "perf record finished with exit=$? data=$PERFDATA bytes=$(wc -c < "$PERFDATA" 2>/dev/null || echo unknown)"

    log "perf data: $PERFDATA"

    # Restrict the report to the WPEFramework daemon's Monitor::IResource tid(s).
    # perf sched latency prints each task as "comm:tid", so we match "$COMM:<tid>"
    # for the daemon tid(s) only (resolved now, after the restart).
    _daemon_tids=$(resolve_daemon_tids)
    if [ -n "$_daemon_tids" ]; then
        # build an alternation like: Monitor::IResou:4556|Monitor::IResou:4560
        _pat=""
        for _tid in $_daemon_tids; do
            _pat="${_pat:+$_pat|}$COMM:$_tid"
        done
        log "Restricting report to WPEFramework daemon tid(s): $_daemon_tids"
    else
        _pat="$COMM"
        log "WARN: could not resolve WPEFramework daemon tid; reporting all '$COMM' threads."
    fi

    {
        echo "=== perf sched latency (filtered: ${_pat}) ==="
        perf sched latency -i "$PERFDATA" -s max 2>/dev/null | \
            awk 'NR<=2 || $0 ~ /'"$_pat"'/'
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

# Resolve the Monitor::IResou tid(s) of the WPEFramework *daemon only*.
# Picks the lowest-PID WPEFramework process (the main daemon, since WPEProcess
# plugin hosts have a different comm and are excluded by pgrep -x) and returns
# every matching thread tid inside it (normally exactly one). This is the single
# source of truth used by every mode so that only the daemon's thread is kept.
resolve_daemon_tids() {
    _dpid=$(pgrep -x "$WPE_PROC" 2>/dev/null | sort -n | head -n1)
    [ -z "$_dpid" ] && return 1
    _tids=""
    for _t in /proc/"$_dpid"/task/*; do
        if grep -q "$COMM" "$_t/comm" 2>/dev/null; then
            _tids="$_tids $(basename "$_t")"
        fi
    done
    _tids=$(echo "$_tids" | sed 's/^ *//')
    [ -n "$_tids" ] && { echo "$_tids"; return 0; }
    return 1
}

# Resolve a single Monitor::IResou tid of the WPEFramework daemon (first match).
resolve_target_tid() {
    _dpid=$(pgrep -x "$WPE_PROC" 2>/dev/null | sort -n | head -n1)
    [ -z "$_dpid" ] && return 1
    for _t in /proc/"$_dpid"/task/*; do
        if grep -q "$COMM" "$_t/comm" 2>/dev/null; then
            echo "$(basename "$_t")"; return 0
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
    debug "arm_boot: starting"
    arm_filtered_trace || exit 1
    start_thread_pressure_sampler
    if ! echo "armed $(date)" > "$OUTDIR/runq_boot_armed.txt" 2>/dev/null; then
        debug "ERROR: failed to write boot marker: $OUTDIR/runq_boot_armed.txt"
        exit 1
    fi
    debug "Boot marker saved: $OUTDIR/runq_boot_armed.txt"
    if ! {
        echo "armed=$(date)"
        echo "tracefs=$TRACEFS"
        echo "comm=$COMM"
        echo "raw=$BOOT_RAW"
        echo "report=$BOOT_REPORT"
        echo "next_action=sh $SELF_INSTALL show-boot"
    } > "$OUTDIR/runq_boot_state.txt" 2>/dev/null; then
        debug "ERROR: failed to write boot state: $OUTDIR/runq_boot_state.txt"
        exit 1
    fi
    debug "Boot trace remains armed; no RAW/REPORT is created until show-boot runs"
    debug "Boot state saved: $OUTDIR/runq_boot_state.txt"
}

# Measure ONLY the running WPEFramework Monitor::IResource thread (no restart).
run_live() {
    # TARGET_TID already set by lock_target_tid before this is called
    debug "run_live: entering with TARGET_TID=$TARGET_TID"
    _mpid=$(pgrep -x "$WPE_PROC" 2>/dev/null | sort -n | head -n1)
    log "Target: $WPE_PROC pid=$_mpid  tid=$TARGET_TID  comm=$COMM"
    arm_filtered_trace || exit 1
    log "Tracing this thread for ${DURATION}s ..."
    start_thread_pressure_sampler
    sleep "$DURATION"
    stop_thread_pressure_sampler
    echo 0 > "$TRACEFS/tracing_on" 2>/dev/null
    if ! cp "$TRACEFS/trace" "$RAW" 2>/dev/null; then
        debug "ERROR: failed to copy $TRACEFS/trace to $RAW"
        exit 1
    fi
    debug "Live raw trace copied: $RAW bytes=$(wc -c < "$RAW" 2>/dev/null || echo unknown)"
    : > "$TRACEFS/set_event" 2>/dev/null
    log "Raw trace saved: $RAW"
    if ! parse_ftrace "$RAW" "$COMM" > "$REPORT" 2>>"$DEBUG_LOG"; then
        debug "ERROR: live parse_ftrace failed for $RAW"
        exit 1
    fi
    cat "$REPORT"
    report_thread_pressure "$PRESSURE_LOG" "$REPORT" | tee -a "$REPORT"
    debug "Live report saved: $REPORT bytes=$(wc -c < "$REPORT" 2>/dev/null || echo unknown)"
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
    debug "show_boot: stopping trace and collecting boot data"
    if [ ! -d "$TRACEFS" ]; then log "tracefs not found."; exit 1; fi
    if [ ! -f "$OUTDIR/runq_boot_armed.txt" ]; then
        debug "WARN: boot marker not found: $OUTDIR/runq_boot_armed.txt"
        log "WARNING: no boot marker found; was arm-boot run before reboot?"
    fi
    stop_thread_pressure_sampler
    echo 0 > "$TRACEFS/tracing_on" 2>/dev/null
    RAW="$BOOT_RAW"
    REPORT="$BOOT_REPORT"
    debug "show_boot: using RAW=$RAW REPORT=$REPORT"
    if ! cp "$TRACEFS/trace" "$RAW" 2>/dev/null; then
        debug "ERROR: failed to copy boot trace from $TRACEFS/trace to $RAW"
        exit 1
    fi
    debug "Boot raw trace copied: $RAW bytes=$(wc -c < "$RAW" 2>/dev/null || echo unknown)"

    # Keep ONLY the WPEFramework daemon (lowest PID) Monitor::IResource tid(s).
    _valid_tids=$(resolve_daemon_tids)
    if [ -n "$_valid_tids" ]; then
        _dpid=$(pgrep -x "$WPE_PROC" 2>/dev/null | sort -n | head -n1)
        for _tid in $_valid_tids; do
            log "  Using: pid=$_dpid tid=$_tid comm=$COMM (WPEFramework daemon only)"
        done
    else
        log "  WARN: no WPEFramework daemon '$COMM' thread found; report may be empty."
    fi

    log "Boot trace dumped: $RAW"
    if ! parse_ftrace "$RAW" "$COMM" "$_valid_tids" > "$REPORT" 2>>"$DEBUG_LOG"; then
        debug "ERROR: boot parse_ftrace failed for $RAW"
        exit 1
    fi
    cat "$REPORT"
    debug "Boot report saved: $REPORT bytes=$(wc -c < "$REPORT" 2>/dev/null || echo unknown)"
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

    # Auto-resolve: WPEFramework daemon only (lowest PID; WPEProcess excluded by -x)
    TARGET_TID=""
    _owner_pid=""
    for _p in $(pgrep -x "$WPE_PROC" 2>/dev/null | sort -n); do
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
    log "  Verified: /proc/$_owner_pid/task/$TARGET_TID/comm = '$_verify'"
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
