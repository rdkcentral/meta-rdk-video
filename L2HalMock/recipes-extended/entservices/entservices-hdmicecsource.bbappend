# Standalone L2HalMock build does not include the full telemetry stack.
PACKAGECONFIG:remove = "telemetrysupport breakpadsupport"
TARGET_LDFLAGS:remove = " -Wl,--no-as-needed -ltelemetry_msgsender -Wl,--as-needed "
