# L2HalMock: remove telemetry dependency (not needed for mock builds)
DEPENDS:remove = "telemetry"
LDFLAGS:remove = "-ltelemetry_msgsender"
