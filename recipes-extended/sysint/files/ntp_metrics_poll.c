#include <stdio.h>
#include <sys/timex.h>
#include <unistd.h>
#include <time.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>

// Structure to track hardware clock drift
typedef struct {
    double initial_wall_time;
    double initial_raw_monotonic_time;
    int initialized;
} drift_tracker_t;

static drift_tracker_t drift_tracker = {0};

double get_monotonic_time() {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return ts.tv_sec + ts.tv_nsec / 1e9;
}

double get_raw_monotonic_time() {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC_RAW, &ts);
    return ts.tv_sec + ts.tv_nsec / 1e9;
}

double get_wall_time() {
    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);
    return ts.tv_sec + ts.tv_nsec / 1e9;
}

int main(int argc, char *argv[]) {
    int csv_mode = 0;
    
    // Check for CSV mode flag
    if (argc > 1 && strcmp(argv[1], "--csv") == 0) {
        csv_mode = 1;
    }
    
    if (csv_mode) {
        // CSV Header with new hardware drift columns
        printf("timestamp,offset_ms,freq_ppm,time_constant,max_error_ms,correction_rate_ms_per_s,status,rtp_ready,hw_drift_ms,hw_drift_ppm,wall_elapsed_s,raw_elapsed_s\n");
    } else {
        printf("Enhanced Time Sync Monitor with Hardware Clock Drift Tracking\n");
        printf("Use --csv flag for CSV output\n");
        printf("Hardware drift shows raw crystal oscillator drift vs NTP-corrected time\n\n");
    }
    
    double last_offset = 0;
    int sample_count = 0;
    
    while (1) {
        struct timex tx = {0};
        int state = adjtimex(&tx);
        
        double offset_ms = (double)tx.offset / 1000.0;
        double freq_ppm = (double)tx.freq / 65536.0;
        double maxerror_ms = (double)tx.maxerror / 1000.0;
        int synced = ((tx.status & STA_UNSYNC) == 0) && (state == TIME_OK);
        
        // Get current times
        double current_wall = get_wall_time();
        double current_raw_monotonic = get_raw_monotonic_time();
        
        // Initialize drift tracking on first run
        if (!drift_tracker.initialized) {
            drift_tracker.initial_wall_time = current_wall;
            drift_tracker.initial_raw_monotonic_time = current_raw_monotonic;
            drift_tracker.initialized = 1;
        }
        
        // Calculate elapsed times
        double wall_elapsed = current_wall - drift_tracker.initial_wall_time;
        double raw_elapsed = current_raw_monotonic - drift_tracker.initial_raw_monotonic_time;
        
        // Calculate hardware clock drift (raw vs NTP-corrected)
        double time_diff = raw_elapsed - wall_elapsed;  // How much raw hardware clock has drifted
        double drift_ms = time_diff * 1000.0;           // Convert to milliseconds
        double drift_ppm = 0;
        if (wall_elapsed > 0) {
            drift_ppm = (time_diff / wall_elapsed) * 1e6;  // Parts per million
        }
        
        time_t now = time(NULL);
        struct tm *tm = localtime(&now);
        
        // Calculate correction rate
        double correction_rate = 0;
        if (sample_count > 0) {
            correction_rate = (last_offset - offset_ms) / 5.0; // ms per second
        }
        
        if (csv_mode) {
            // CSV format output with new columns
            char rtp_status[20];
            if (fabs(offset_ms) < 20) {
                strcpy(rtp_status, "READY");
            } else if (fabs(offset_ms) < 100) {
                strcpy(rtp_status, "MARGINAL");
            } else {
                strcpy(rtp_status, "NOT_READY");
            }
            
            printf("%04d-%02d-%02d %02d:%02d:%02d,%.3f,%.3f,%ld,%.3f,%.6f,%s,%s,%.3f,%.2f,%.1f,%.1f\n",
                   tm->tm_year + 1900, tm->tm_mon + 1, tm->tm_mday,
                   tm->tm_hour, tm->tm_min, tm->tm_sec,
                   offset_ms, freq_ppm, tx.constant, maxerror_ms, correction_rate,
                   synced ? "SYNC" : "UNSYNC", rtp_status,
                   drift_ms, drift_ppm, wall_elapsed, raw_elapsed);
        } else {
            // Human-readable format with monotonic drift info
            printf("%02d:%02d:%02d | Offset: %+8.1f ms | Freq: %+6.1f ppm | TC: %ld | MaxErr: %6.1f ms | Rate: %+5.1f ms/s\n",
                   tm->tm_hour, tm->tm_min, tm->tm_sec,
                   offset_ms, freq_ppm, tx.constant, maxerror_ms, correction_rate);
            
            // Show hardware clock drift information
            if (sample_count > 0) {
                printf("          Hardware drift: %+7.1f ms (%+6.1f ppm) over %.0f seconds\n",
                       drift_ms, drift_ppm, wall_elapsed);
                
                // Provide drift analysis
                if (fabs(drift_ppm) < 1.0) {
                    printf("          EXCELLENT: Crystal oscillator very stable\n");
                } else if (fabs(drift_ppm) < 10.0) {
                    printf("          GOOD: Crystal oscillator reasonably stable\n");
                } else if (fabs(drift_ppm) < 50.0) {
                    printf("         MODERATE: Some crystal oscillator drift\n");
                } else {
                    printf("         HIGH: Significant crystal oscillator drift\n");
                }
                
                // Show NTP correction effectiveness
                printf("         🔧 NTP correction: %+6.1f ppm (compensating for hardware drift)\n", freq_ppm);
            }
            
            // Provide NTP sync recommendations
            if (sample_count > 0) {
                if (fabs(correction_rate) < 1.0 && fabs(offset_ms) > 100) {
                    printf("          SLOW: Correction rate too slow for large offset\n");
                    printf("          Try: ./kernel-clock-control freq %.1f\n", freq_ppm + 20.0);
                } else if (fabs(correction_rate) > 10.0) {
                    printf("          FAST: Good correction rate\n");
                } else if (fabs(offset_ms) < 20) {
                    printf("          READY: Suitable for RTP audio\n");
                } else if (fabs(offset_ms) < 100) {
                    printf("          IMPROVING: Getting closer\n");
                }
                
                // Estimate time to convergence
                if (fabs(correction_rate) > 0.1) {
                    double time_to_20ms = (fabs(offset_ms) - 20) / fabs(correction_rate);
                    if (time_to_20ms > 0 && time_to_20ms < 3600) {
                        printf("           ETA to RTP-ready: %.0f seconds\n", time_to_20ms);
                    }
                }
            }
            
            printf("\n");
        }
        
        // Flush output for real-time CSV logging
        fflush(stdout);
        
        last_offset = offset_ms;
        sample_count++;
        
        sleep(5);
    }
    
    return 0;
}
