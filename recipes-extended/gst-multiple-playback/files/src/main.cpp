#include <gst/gst.h>
#include <iostream>
#include <string>
#include <cstring>
#include <cstdlib>
#include <chrono>
#include <thread>

struct Options {
    std::string uri;
    int play_duration_ms = 1000;
    int delay_ms = 0;
    int timeout_s = 10;
    int verify_duration_s = 3;
};

static void log_ts(const char* msg) {
    auto now = std::chrono::steady_clock::now();
    auto ms = std::chrono::duration_cast<std::chrono::milliseconds>(now.time_since_epoch()).count();
    std::cout << "[" << (ms / 1000) << "." << (ms % 1000) << "] " << msg << std::endl;
}

static void log_ts(const std::string& msg) {
    log_ts(msg.c_str());
}

static bool bus_has_error(GstElement* pipeline, int id) {
    GstBus* bus = gst_element_get_bus(pipeline);
    GstMessage* msg = gst_bus_pop_filtered(bus, GST_MESSAGE_ERROR);
    if (msg) {
        GError* err = nullptr;
        gchar* debug_info = nullptr;
        gst_message_parse_error(msg, &err, &debug_info);
        std::cerr << "[Pipeline " << id << "] Error: " << err->message << "\n";
        if (debug_info) {
            std::cerr << "[Pipeline " << id << "] Debug: " << debug_info << "\n";
        }
        g_error_free(err);
        g_free(debug_info);
        gst_message_unref(msg);
        gst_object_unref(bus);
        return true;
    }
    gst_object_unref(bus);
    return false;
}

static bool wait_for_state(GstElement* pipeline, GstState target, int id, int timeout_s) {
    GstStateChangeReturn ret = gst_element_get_state(pipeline, nullptr, nullptr,
                                                      static_cast<GstClockTime>(timeout_s) * GST_SECOND);
    if (ret == GST_STATE_CHANGE_FAILURE) {
        log_ts("[Pipeline " + std::to_string(id) + "] State change FAILED");
        return false;
    }
    if (ret == GST_STATE_CHANGE_ASYNC) {
        log_ts("[Pipeline " + std::to_string(id) + "] State change TIMEOUT");
        return false;
    }
    log_ts("[Pipeline " + std::to_string(id) + "] Reached " + gst_element_state_get_name(target));
    return true;
}

static GstElement* create_pipeline(int id, const std::string& uri) {
    std::string name = "pipeline-" + std::to_string(id);
    GstElement* pipeline = gst_element_factory_make("playbin", name.c_str());
    if (!pipeline) {
        log_ts("[Pipeline " + std::to_string(id) + "] Failed to create playbin");
        return nullptr;
    }
    g_object_set(pipeline, "uri", uri.c_str(), nullptr);
    log_ts("[Pipeline " + std::to_string(id) + "] Created with URI: " + uri);
    return pipeline;
}

static void destroy_pipeline(GstElement*& pipeline, int id) {
    if (pipeline) {
        log_ts("[Pipeline " + std::to_string(id) + "] Destroying (setting NULL)...");
        gst_element_set_state(pipeline, GST_STATE_NULL);
        gst_object_unref(pipeline);
        pipeline = nullptr;
        log_ts("[Pipeline " + std::to_string(id) + "] Destroyed");
    }
}

static Options parse_args(int argc, char* argv[]) {
    Options opts;

    if (argc < 2) {
        std::cout << "Usage: " << argv[0] << " <video_uri> [OPTIONS]\n"
                  << "\nOptions:\n"
                  << "  --play-duration-ms N   Play duration before pause (default: 1000)\n"
                  << "  --delay-ms N           Delay between destroy and play (default: 0)\n"
                  << "  --timeout-s N          State change timeout (default: 10)\n"
                  << "  --verify-duration-s N  Verification duration (default: 3)\n";
        std::exit(1);
    }

    opts.uri = argv[1];

    for (int i = 2; i < argc; i++) {
        if (std::strcmp(argv[i], "--play-duration-ms") == 0 && i + 1 < argc) {
            opts.play_duration_ms = std::atoi(argv[++i]);
        } else if (std::strcmp(argv[i], "--delay-ms") == 0 && i + 1 < argc) {
            opts.delay_ms = std::atoi(argv[++i]);
        } else if (std::strcmp(argv[i], "--timeout-s") == 0 && i + 1 < argc) {
            opts.timeout_s = std::atoi(argv[++i]);
        } else if (std::strcmp(argv[i], "--verify-duration-s") == 0 && i + 1 < argc) {
            opts.verify_duration_s = std::atoi(argv[++i]);
        }
    }

    return opts;
}

int main(int argc, char* argv[]) {
    gst_init(&argc, &argv);

    Options opts = parse_args(argc, argv);

    log_ts("=== Decoder Handoff Test ===");
    log_ts("URI: " + opts.uri);
    log_ts("Play duration: " + std::to_string(opts.play_duration_ms) + " ms");
    log_ts("Delay after destroy: " + std::to_string(opts.delay_ms) + " ms");
    log_ts("State change timeout: " + std::to_string(opts.timeout_s) + " s");
    log_ts("Verify duration: " + std::to_string(opts.verify_duration_s) + " s");

    // Step 1: Create pipeline1
    GstElement* pipeline1 = create_pipeline(1, opts.uri);
    if (!pipeline1) return 1;

    // Step 2-3: Set pipeline1 → PLAYING and wait
    log_ts("[Pipeline 1] Setting PLAYING...");
    gst_element_set_state(pipeline1, GST_STATE_PLAYING);
    if (!wait_for_state(pipeline1, GST_STATE_PLAYING, 1, opts.timeout_s)) {
        destroy_pipeline(pipeline1, 1);
        log_ts("FAIL: Pipeline 1 could not reach PLAYING");
        return 1;
    }
    if (bus_has_error(pipeline1, 1)) {
        destroy_pipeline(pipeline1, 1);
        log_ts("FAIL: Pipeline 1 error during PLAYING");
        return 1;
    }

    // Step 4: Play for configured duration
    log_ts("[Pipeline 1] Playing for " + std::to_string(opts.play_duration_ms) + " ms...");
    std::this_thread::sleep_for(std::chrono::milliseconds(opts.play_duration_ms));

    // Step 5-6: Pause pipeline1
    log_ts("[Pipeline 1] Setting PAUSED...");
    gst_element_set_state(pipeline1, GST_STATE_PAUSED);
    if (!wait_for_state(pipeline1, GST_STATE_PAUSED, 1, opts.timeout_s)) {
        destroy_pipeline(pipeline1, 1);
        log_ts("FAIL: Pipeline 1 could not reach PAUSED");
        return 1;
    }

    // Step 7: Create pipeline2
    GstElement* pipeline2 = create_pipeline(2, opts.uri);
    if (!pipeline2) {
        destroy_pipeline(pipeline1, 1);
        return 1;
    }

    // Step 8-9: Set pipeline2 → PAUSED (preroll, decoder revoked from p1, granted to p2)
    log_ts("[Pipeline 2] Setting PAUSED (preroll)...");
    gst_element_set_state(pipeline2, GST_STATE_PAUSED);
    if (!wait_for_state(pipeline2, GST_STATE_PAUSED, 2, opts.timeout_s)) {
        destroy_pipeline(pipeline1, 1);
        destroy_pipeline(pipeline2, 2);
        log_ts("FAIL: Pipeline 2 could not reach PAUSED");
        return 1;
    }
    if (bus_has_error(pipeline2, 2)) {
        destroy_pipeline(pipeline1, 1);
        destroy_pipeline(pipeline2, 2);
        log_ts("FAIL: Pipeline 2 error during PAUSED");
        return 1;
    }

    // Step 10: Destroy pipeline1 (triggers spurious ESS RM notification)
    log_ts("--- CRITICAL SECTION: Destroying pipeline1 ---");
    destroy_pipeline(pipeline1, 1);

    // Step 11: Optional delay
    if (opts.delay_ms > 0) {
        log_ts("Waiting " + std::to_string(opts.delay_ms) + " ms after destroy...");
        std::this_thread::sleep_for(std::chrono::milliseconds(opts.delay_ms));
    }

    // Check if pipeline2 already got an error from the notification
    if (bus_has_error(pipeline2, 2)) {
        destroy_pipeline(pipeline2, 2);
        log_ts("FAIL: Pipeline 2 received error after pipeline1 destruction (bug reproduced)");
        return 1;
    }

    // Step 12: Set pipeline2 → PLAYING
    log_ts("[Pipeline 2] Setting PLAYING...");
    GstStateChangeReturn ret = gst_element_set_state(pipeline2, GST_STATE_PLAYING);
    if (ret == GST_STATE_CHANGE_FAILURE) {
        log_ts("FAIL: Pipeline 2 state change to PLAYING returned FAILURE (bug reproduced)");
        destroy_pipeline(pipeline2, 2);
        return 1;
    }
    if (!wait_for_state(pipeline2, GST_STATE_PLAYING, 2, opts.timeout_s)) {
        destroy_pipeline(pipeline2, 2);
        log_ts("FAIL: Pipeline 2 could not reach PLAYING (bug reproduced)");
        return 1;
    }
    if (bus_has_error(pipeline2, 2)) {
        destroy_pipeline(pipeline2, 2);
        log_ts("FAIL: Pipeline 2 error when set to PLAYING (bug reproduced)");
        return 1;
    }

    // Step 13: Verify pipeline2 plays — position must advance
    log_ts("[Pipeline 2] Verifying playback for " + std::to_string(opts.verify_duration_s) + " s...");

    gint64 pos1 = -1, pos2 = -1;
    gst_element_query_position(pipeline2, GST_FORMAT_TIME, &pos1);
    log_ts("[Pipeline 2] Position start: " + std::to_string(pos1 / GST_MSECOND) + " ms");

    std::this_thread::sleep_for(std::chrono::seconds(opts.verify_duration_s));

    if (bus_has_error(pipeline2, 2)) {
        destroy_pipeline(pipeline2, 2);
        log_ts("FAIL: Pipeline 2 error during verification (bug reproduced)");
        return 1;
    }

    gst_element_query_position(pipeline2, GST_FORMAT_TIME, &pos2);
    log_ts("[Pipeline 2] Position end: " + std::to_string(pos2 / GST_MSECOND) + " ms");

    // Cleanup
    destroy_pipeline(pipeline2, 2);

    if (pos2 > pos1 && pos1 >= 0) {
        log_ts("PASS: Pipeline 2 played successfully (position advanced from "
               + std::to_string(pos1 / GST_MSECOND) + " ms to "
               + std::to_string(pos2 / GST_MSECOND) + " ms)");
        return 0;
    } else {
        log_ts("FAIL: Pipeline 2 position did not advance (stuck at "
               + std::to_string(pos2 / GST_MSECOND) + " ms, bug reproduced)");
        return 1;
    }
}
