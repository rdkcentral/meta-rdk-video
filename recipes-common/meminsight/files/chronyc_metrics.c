#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <time.h>
#include <math.h>

// -------------------------
// Helpers
// -------------------------
static void rtrim(char *s) {
    size_t n = strlen(s);
    while (n && (s[n-1] == '\n' || s[n-1] == '\r' || s[n-1] == ' ' || s[n-1] == '\t')) s[--n] = '\0';
}

static char* dupstr(const char* s) {
    size_t n = strlen(s);
    char* p = (char*)malloc(n+1);
    if (!p) return NULL;
    memcpy(p, s, n+1);
    return p;
}

static int starts_with(const char* s, const char* pfx) {
    return strncmp(s, pfx, strlen(pfx)) == 0;
}

static FILE* popen_safe(const char* cmd) {
    FILE* f = popen(cmd, "r");
    return f;
}

// -------------------------
// Drift tracker (same idea as your ntpmetrics_poll)
// -------------------------
typedef struct {
    double initial_wall;
    double initial_raw;
    int initialized;
} drift_tracker_t;

static double prev_wall = 0.0, prev_raw = 0.0;
static int have_prev_samples = 0;
static int prev_synced_flag = -1;
static double cum_drift_s = 0.0;

static drift_tracker_t drift_tracker = {0};

static double now_wall() {
    struct timespec ts; clock_gettime(CLOCK_REALTIME, &ts);
    return ts.tv_sec + ts.tv_nsec / 1e9;
}

static double now_raw() {
    struct timespec ts; clock_gettime(CLOCK_MONOTONIC_RAW, &ts);
    return ts.tv_sec + ts.tv_nsec / 1e9;
}

// -------------------------
// Chrony tracking fields we care about
// -------------------------
typedef struct {
    char   reference_ip[64];
    int    stratum;
    double system_time_s;     // + = fast of NTP, - = slow of NTP
    double last_offset_s;
    double rms_offset_s;
    double frequency_ppm;     // signed: fast (+) / slow (-)
    double residual_freq_ppm;
    double skew_ppm;
    double root_delay_s;
    double root_dispersion_s;
    double update_interval_s;
    char   leap_status[32];   // Normal / Not synchronised / etc.
    int    have;              // parsed OK flag
} chrony_tracking_t;

static void chrony_tracking_init(chrony_tracking_t* t) {
    memset(t, 0, sizeof(*t));
    strcpy(t->reference_ip, "-");
    strcpy(t->leap_status, "Unknown");
}

// Parse a line like: "System time     : 0.000076367 seconds fast of NTP time"
static int parse_system_time_line(const char* line, double* out_s) {
    double val = 0.0; char unit[32] = {0}; char dir[32] = {0};
    // Accept both "fast" and "slow"
    if (sscanf(line, "System time%*[^:]: %lf %31s %31s", &val, unit, dir) >= 2) {
        // dir could be "fast" or "slow" ("fast of NTP time")
        if (strstr(line, "slow")) val = -val;
        *out_s = val;
        return 1;
    }
    return 0;
}

// Extract content in parentheses from a line like: Reference ID : C3A25F26 (195.162.95.38)
static void extract_parenthesized_ip(const char* line, char out[64]) {
    const char* l = strchr(line, '(');
    const char* r = l ? strchr(l+1, ')') : NULL;
    if (l && r && r > l+1) {
        size_t n = (size_t)(r - (l+1));
        if (n >= 63) n = 63;
        memcpy(out, l+1, n); out[n] = '\0';
    }
}

static int chrony_get_tracking(chrony_tracking_t* out) {
    chrony_tracking_init(out);

    FILE* fp = popen_safe("chronyc -n tracking");
    if (!fp) return 0;

    char buf[256];
    while (fgets(buf, sizeof(buf), fp)) {
        rtrim(buf);
        if (starts_with(buf, "Reference ID")) {
            extract_parenthesized_ip(buf, out->reference_ip);
            out->have = 1;
        } else if (starts_with(buf, "Stratum")) {
            sscanf(buf, "%*[^:]: %d", &out->stratum);
        } else if (starts_with(buf, "System time")) {
            parse_system_time_line(buf, &out->system_time_s);
        } else if (starts_with(buf, "Last offset")) {
            sscanf(buf, "%*[^:]: %lf", &out->last_offset_s);
        } else if (starts_with(buf, "RMS offset")) {
            sscanf(buf, "%*[^:]: %lf", &out->rms_offset_s);
        } else if (starts_with(buf, "Frequency")) {
            double v = 0.0; // detect fast/slow
            if (sscanf(buf, "%*[^:]: %lf", &v) == 1) {
                // Chrony prints e.g. "Frequency : 4.363 ppm slow" → make slow negative
                if (strstr(buf, "slow")) v = -v;
                out->frequency_ppm = v;
            }
        } else if (starts_with(buf, "Residual freq")) {
            sscanf(buf, "%*[^:]: %lf", &out->residual_freq_ppm);
        } else if (starts_with(buf, "Skew")) {
            sscanf(buf, "%*[^:]: %lf", &out->skew_ppm);
        } else if (starts_with(buf, "Root delay")) {
            sscanf(buf, "%*[^:]: %lf", &out->root_delay_s);
        } else if (starts_with(buf, "Root dispersion")) {
            sscanf(buf, "%*[^:]: %lf", &out->root_dispersion_s);
        } else if (starts_with(buf, "Update interval")) {
            sscanf(buf, "%*[^:]: %lf", &out->update_interval_s);
        } else if (starts_with(buf, "Leap status")) {
            char val[64] = {0};
            if (sscanf(buf, "%*[^:]: %63[^\n]", val) == 1) {
                rtrim(val);
                // trim leading spaces
                size_t i=0; while (val[i]==' ') i++;
                strncpy(out->leap_status, val+i, sizeof(out->leap_status)-1);
            }
        }
    }

    pclose(fp);
    return out->have;
}

// -------------------------
// Main
// -------------------------
int main(int argc, char** argv) {
    int csv = 0; int interval = 5;
    if (argc > 1 && strcmp(argv[1], "--csv") == 0) csv = 1;

    if (!csv) {
        printf("Chrony Metrics Monitor (tracking + drift)\n");
        printf("Uses `chronyc -n tracking` to read daemon metrics.\n");
        printf("--csv for CSV output. Ctrl+C to stop.\n\n");
    }

    // init drift baseline
    if (!drift_tracker.initialized) {
        drift_tracker.initial_wall = now_wall();
        drift_tracker.initial_raw  = now_raw();
        drift_tracker.initialized  = 1;
    }

    // CSV header
    if (csv) {
        printf("timestamp,last_offset_ms,frequency_ppm,root_delay_ms,update_interval_s,status,rtp_ready,hw_drift_ms,hw_drift_ppm,wall_elapsed_s,raw_elapsed_s\n");
        fflush(stdout);
    }

    double prev_last_offset_ms = 0.0;
    int have_prev = 0;

    for (;;) {
        chrony_tracking_t t;
        if (!chrony_get_tracking(&t)) {
            if (csv) {
                // emit minimal placeholder row
                time_t now = time(NULL); struct tm tm; localtime_r(&now, &tm);
                printf("%04d-%02d-%02d %02d:%02d:%02d,,,,,,,,,,,,UNSYNC,NOT_READY,,,,\n",
                       tm.tm_year+1900, tm.tm_mon+1, tm.tm_mday,
                       tm.tm_hour, tm.tm_min, tm.tm_sec);
                fflush(stdout);
            } else {
                fprintf(stderr, "chronyc tracking failed (is chronyc/chronyd available?)\n");
            }
            sleep(interval);
            continue;
        }

        // compute basics
        double last_offset_ms = t.last_offset_s * 1e3;
        double rms_offset_ms  = t.rms_offset_s  * 1e3;
        double system_time_ms = t.system_time_s * 1e3;
        double root_delay_ms  = t.root_delay_s  * 1e3;
        double root_disp_ms   = t.root_dispersion_s * 1e3;

        // ready flag based on last offset magnitude
        const char* rtp_ready = (fabs(last_offset_ms) < 20.0) ? "READY" : (fabs(last_offset_ms) < 100.0 ? "MARGINAL" : "NOT_READY");

        // daemon sync status from Leap status (Normal => SYNC)
        const char* status = strstr(t.leap_status, "Normal") ? "SYNC" : "UNSYNC";

        // compute drift vs raw clock
       // double wall_elapsed = now_wall() - drift_tracker.initial_wall;
        //double raw_elapsed  = now_raw()  - drift_tracker.initial_raw;
        //double time_diff    = raw_elapsed - wall_elapsed;  // positive if raw runs faster
        //double drift_ms     = time_diff * 1e3;
        //double drift_ppm    = (wall_elapsed > 0) ? (time_diff / wall_elapsed) * 1e6 : 0.0;


double w = now_wall();
double r = now_raw();

if (!drift_tracker.initialized) {
    drift_tracker.initial_wall = w;
    drift_tracker.initial_raw  = r;
    drift_tracker.initialized  = 1;
    prev_wall = w;
    prev_raw  = r;
    prev_synced_flag = 1;
    have_prev_samples = 0;        // first iteration, no delta yet
}

// deltas since last sample (if any)
double dw = 0.0, dr = 0.0;
int time_step = 0;

if (have_prev_samples) {
    dw = w - prev_wall;
    dr = r - prev_raw;

    // Heuristic: if wall vs raw delta mismatches by >0.5s over one loop, assume a step
    // (tune 0.5 if your sampling interval is different)
    if (fabs((dr - dw)) > 0.5 || dw < 0.0) {
        time_step = 1;
    }
}

// Re-baseline if a time step is detected or the sync state flips
if (time_step || (prev_synced_flag != -1 && prev_synced_flag != 1)) {
    drift_tracker.initial_wall = w;
    drift_tracker.initial_raw  = r;
    cum_drift_s = 0.0;
    have_prev_samples = 0;   // reset accumulation starting next sample
} else if (have_prev_samples) {
    // accumulate incremental drift (raw - wall) since last sample
    cum_drift_s += (dr - dw);
}

// elapsed since the last (re)baseline
double wall_elapsed = w - drift_tracker.initial_wall;
double raw_elapsed  = r - drift_tracker.initial_raw;

// Report cumulative drift since baseline (stable, no explosion on step)
double drift_ms  = cum_drift_s * 1e3;
double drift_ppm = (wall_elapsed > 0.0) ? (cum_drift_s / wall_elapsed) * 1e6 : 0.0;

// update previous-sample state
prev_wall = w;
prev_raw  = r;
prev_synced_flag = 1;
have_prev_samples = 1;
        
        // simple correction rate from last_offset trend
        double corr_rate_ms_per_s = 0.0;
        if (have_prev) {
            // approximate with sampling interval; we don't know exact daemon update, but for monitoring it's fine
            corr_rate_ms_per_s = (prev_last_offset_ms - last_offset_ms) / (double)interval;
        }
        prev_last_offset_ms = last_offset_ms; have_prev = 1;

        time_t now = time(NULL); struct tm tm; localtime_r(&now, &tm);

        if (csv) {
            printf("%04d-%02d-%02d %02d:%02d:%02d,%.6f,%.6f,%.6f,%.1f,%s,%s,%.3f,%.2f,%.1f,%.1f\n",
                   tm.tm_year+1900, tm.tm_mon+1, tm.tm_mday,
                   tm.tm_hour, tm.tm_min, tm.tm_sec,
                   last_offset_ms,
                   t.frequency_ppm,
                   root_delay_ms, t.update_interval_s,
                   status, rtp_ready,
                   drift_ms, drift_ppm,
                   wall_elapsed, raw_elapsed);
            fflush(stdout);
        } else {
            printf("%02d:%02d:%02d | last_offset: %+7.3f ms | rms: %7.3f ms | sys_time: %+7.3f ms | freq: %+7.3f ppm | skew: %6.3f ppm | root(d=%.2f ms, disp=%.2f ms) | upd: %.1fs\n",
                   tm.tm_hour, tm.tm_min, tm.tm_sec,
                   last_offset_ms, rms_offset_ms, system_time_ms,
                   t.frequency_ppm, t.skew_ppm, root_delay_ms, root_disp_ms, t.update_interval_s);
            printf("           ref=%s stratum=%d leap=%s status=%s rtp=%s\n",
                   t.reference_ip, t.stratum, t.leap_status, status, rtp_ready);
            if (wall_elapsed > 0.0) {
                printf("           drift: %+7.1f ms (%+6.1f ppm) over %.0f s | corr_rate: %+5.2f ms/s\n\n",
                       drift_ms, drift_ppm, wall_elapsed, corr_rate_ms_per_s);
            } else {
                printf("           drift: warming up...\n\n");
            }
            fflush(stdout);
        }

        sleep(interval);
    }
}
