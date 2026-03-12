 #include <gst/gst.h>
 
    #ifdef __APPLE__
    #include <TargetConditionals.h>
    #endif
 
    /* Structure to contain all our information */
    typedef struct _CustomData {
      GstElement *pipeline;
      GstElement *source;
 
      /* Audio elements */
      GstElement *audioconvert;
      GstElement *audioresample;
      GstElement *audiosink;
 
      /* Video elements */
      GstElement *videoconvert;
      GstElement *videosink;
 
    } CustomData;
 
    /* Handler for the pad-added signal */
    static void pad_added_handler (GstElement *src, GstPad *new_pad, CustomData *data);
 
    /* Main tutorial function */
    int tutorial_main (int argc, char *argv[]) {
 
      CustomData data;
      GstBus *bus;
      GstMessage *msg;
      GstStateChangeReturn ret;
      gboolean terminate = FALSE;
 
      /* Initialize GStreamer */
      gst_init (&argc, &argv);
 
      /* Create elements */
 
      data.source = gst_element_factory_make ("uridecodebin", "source");
 
      /* Audio branch */
      data.audioconvert  = gst_element_factory_make ("audioconvert", "audioconvert");
      data.audioresample = gst_element_factory_make ("audioresample", "audioresample");
      data.audiosink     = gst_element_factory_make ("autoaudiosink", "audiosink");
 
      /* Video branch */
      data.videoconvert  = gst_element_factory_make ("videoconvert", "videoconvert");
      data.videosink     = gst_element_factory_make ("autovideosink", "videosink");
 
      /* Create pipeline */
      data.pipeline = gst_pipeline_new ("test-pipeline");
 
      if (!data.pipeline || !data.source ||
          !data.audioconvert || !data.audioresample || !data.audiosink ||
          !data.videoconvert || !data.videosink) {
        g_printerr ("Not all elements could be created.\n");
        return -1;
      }
 
      /* Build the pipeline */
 
      gst_bin_add_many (GST_BIN (data.pipeline),
                        data.source,
                        data.audioconvert, data.audioresample, data.audiosink,
                        data.videoconvert, data.videosink,
                        NULL);
 
      /* Link audio elements */
      if (!gst_element_link_many (data.audioconvert,
                                  data.audioresample,
                                  data.audiosink,
                                  NULL)) {
        g_printerr ("Audio elements could not be linked.\n");
        gst_object_unref (data.pipeline);
        return -1;
      }
 
      /* Link video elements */
      if (!gst_element_link_many (data.videoconvert,
                                  data.videosink,
                                  NULL)) {
        g_printerr ("Video elements could not be linked.\n");
        gst_object_unref (data.pipeline);
        return -1;
      }
 
      /* Set URI */
      g_object_set (data.source, "uri",
          "https://gstreamer.freedesktop.org/data/media/sintel_trailer-480p.webm",
          NULL);
           GST_DEBUG_BIN_TO_DOT_FILE(GST_BIN(data.pipeline),GST_DEBUG_GRAPH_SHOW_MEDIA_TYPE | GST_DEBUG_GRAPH_SHOW_CAPS_DETAILS , "before_connecting_pads");
 
      /* Connect to pad-added signal */
      g_signal_connect (data.source, "pad-added",
                        G_CALLBACK (pad_added_handler), &data);
 
           GST_DEBUG_BIN_TO_DOT_FILE(GST_BIN(data.pipeline),GST_DEBUG_GRAPH_SHOW_MEDIA_TYPE | GST_DEBUG_GRAPH_SHOW_CAPS_DETAILS , "pipeline_graph_after_connecting");
      /* Start playing */
      ret = gst_element_set_state (data.pipeline, GST_STATE_PLAYING);
      if (ret == GST_STATE_CHANGE_FAILURE) {
        g_printerr ("Unable to set the pipeline to the playing state.\n");
        gst_object_unref (data.pipeline);
        return -1;
      }
      GST_DEBUG_BIN_TO_DOT_FILE(GST_BIN(data.pipeline),GST_DEBUG_GRAPH_SHOW_MEDIA_TYPE | GST_DEBUG_GRAPH_SHOW_CAPS_DETAILS , "pipeline_graph_after_playing");
 
      /* Listen to the bus */
      bus = gst_element_get_bus (data.pipeline);
 
      do {
        msg = gst_bus_timed_pop_filtered (bus,
                GST_CLOCK_TIME_NONE,
                GST_MESSAGE_STATE_CHANGED |
                GST_MESSAGE_ERROR |
                GST_MESSAGE_EOS);
 
        if (msg != NULL) {
          GError *err;
          gchar *debug_info;
 
          switch (GST_MESSAGE_TYPE (msg)) {
 
            case GST_MESSAGE_ERROR:
              gst_message_parse_error (msg, &err, &debug_info);
              g_printerr ("Error received from element %s: %s\n",
                          GST_OBJECT_NAME (msg->src), err->message);
              g_printerr ("Debugging information: %s\n",
                          debug_info ? debug_info : "none");
              g_clear_error (&err);
              g_free (debug_info);
              terminate = TRUE;
              break;
 
            case GST_MESSAGE_EOS:
              g_print ("End-Of-Stream reached.\n");
              terminate = TRUE;
              break;
 
            case GST_MESSAGE_STATE_CHANGED:
              if (GST_MESSAGE_SRC (msg) == GST_OBJECT (data.pipeline)) {
                GstState old_state, new_state, pending_state;
                gst_message_parse_state_changed (msg,
                                                 &old_state,
                                                 &new_state,
                                                 &pending_state);
                g_print ("Pipeline state changed from %s to %s\n",
                         gst_element_state_get_name (old_state),
                         gst_element_state_get_name (new_state));
              }
              break;
 
            default:
              g_printerr ("Unexpected message received.\n");
              break;
          }
 
          gst_message_unref (msg);
        }
              GST_DEBUG_BIN_TO_DOT_FILE(GST_BIN(data.pipeline),GST_DEBUG_GRAPH_SHOW_MEDIA_TYPE | GST_DEBUG_GRAPH_SHOW_CAPS_DETAILS , "pipeline_graph_after_playing_final");
 
      } while (!terminate);
 
      /* Free resources */
      gst_object_unref (bus);
      gst_element_set_state (data.pipeline, GST_STATE_NULL);
      gst_object_unref (data.pipeline);
 
      return 0;
    }
 
    /* Pad-added handler */
    static void pad_added_handler (GstElement *src,
                                   GstPad *new_pad,
                                   CustomData *data)
    {
      GstPad *sink_pad = NULL;
      GstCaps *new_pad_caps = NULL;
      GstStructure *new_pad_struct = NULL;
      const gchar *new_pad_type = NULL;
      GstPadLinkReturn ret;
 
      g_print ("Received new pad '%s' from '%s'\n",
               GST_PAD_NAME (new_pad),
               GST_ELEMENT_NAME (src));
 
      new_pad_caps = gst_pad_get_current_caps (new_pad);
      new_pad_struct = gst_caps_get_structure (new_pad_caps, 0);
      new_pad_type = gst_structure_get_name (new_pad_struct);
 
      /* Handle Audio */
      if (g_str_has_prefix (new_pad_type, "audio/x-raw")) {
 
        sink_pad = gst_element_get_static_pad (data->audioconvert, "sink");
 
        if (!gst_pad_is_linked (sink_pad)) {
          ret = gst_pad_link (new_pad, sink_pad);
          if (GST_PAD_LINK_FAILED (ret))
            g_print ("Audio link failed.\n");
          else
            g_print ("Audio link succeeded.\n");
        }
 
      }
 
      /* Handle Video */
      else if (g_str_has_prefix (new_pad_type, "video/x-raw")) {
 
        sink_pad = gst_element_get_static_pad (data->videoconvert, "sink");
 
        if (!gst_pad_is_linked (sink_pad)) {
          ret = gst_pad_link (new_pad, sink_pad);
          if (GST_PAD_LINK_FAILED (ret))
            g_print ("Video link failed.\n");
          else
            g_print ("Video link succeeded.\n");
        }
 
      }
 
      else {
        g_print ("Unknown pad type '%s'. Ignoring.\n", new_pad_type);
      }
 
      if (new_pad_caps != NULL)
        gst_caps_unref (new_pad_caps);
 
      if (sink_pad != NULL)
        gst_object_unref (sink_pad);
    }
 
    /* Platform specific main */
    int main (int argc, char *argv[]) {
    	return tutorial_main(argc, argv);
    }
