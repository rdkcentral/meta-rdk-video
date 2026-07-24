// Stub fogiarm.h — provides FOG IARM type definitions without the fog package
#ifndef _FOG_EVENT_H_
#define _FOG_EVENT_H_

#include "libIBus.h"
#include "libIARM.h"
#include <string>

#define IARM_BUS_FOG_NAME "FOG"
#define IARM_BUS_XDEVICE_NAME "XDEVICE"
#define IARM_BUS_FOG_getCurrentState "getCurrentState"

typedef struct _IARM_Bus_FOG_Param_t
{
    bool status;
    int fogVersion;
    char tsbEndpoint[33];
    bool bIPDVRSupported;
} IARM_Bus_Fog_Param_t;

typedef enum
{
    IARM_BUS_FOG_EVENT_STATUS,
    IARM_BUS_FOG_EVENT_DVR,
    IARM_BUS_FOG_POWER_STATE_CHANGE=200,
    IARM_BUS_FOG_EVENT_MAX
} FOG_EventId_t;

typedef enum
{
    FOG_EVENT_SEGMENT_START,
    FOG_EVENT_SEGMENT_END,
    FOG_EVENT_SEGMENT_STATUS_CHANGE,
    FOG_EVENT_DVR_STATUS_READY
} FOG_SegmentEvent_Id_t;

#define MAX_EVENT_CHAR_DATA_LEN 100

typedef struct _IARM_Bus_FOG_Segments_Event_Param_t
{
    FOG_SegmentEvent_Id_t eEventType;
    char recordingId[MAX_EVENT_CHAR_DATA_LEN];
    char newStatus[MAX_EVENT_CHAR_DATA_LEN];
    int segmentId;
    long long startTimeMs;
} IARM_Bus_FOG_Segments_Event_Param_t;

typedef enum _NetworkManager_EventId_t {
    IARM_BUS_NETWORK_MANAGER_EVENT_SET_INTERFACE_ENABLED=50,
    IARM_BUS_NETWORK_MANAGER_EVENT_INTERFACE_IPADDRESS=55,
    IARM_BUS_NETWORK_MANAGER_MAX
} IARM_Bus_NetworkManager_EventId_t;

typedef struct _IARM_BUS_NetSrvMgr_Iface_EventData_t {
    union {
        char activeIface[10];
        char allNetworkInterfaces[50];
        char enableInterface[10];
    };
    char interfaceCount;
    bool isInterfaceEnabled;
} IARM_BUS_NetSrvMgr_Iface_EventData_t;

// Stub functions — FOG daemon is not present, these are no-ops
static inline bool isXDeviceReady() { return false; }
static inline void fogiarm_init(void) {}
static inline void fogiarm_term(void) {}
static inline bool getStorageMountPath(char *, int) { return false; }
static inline bool getTSBMaxMinutes(int &) { return false; }
static inline bool isTSBEnabled() { return false; }
static inline void fogiarm_generateStatusEvent() {}
static inline void fogIarm_segmentStart(const char *, int, long long) {}
static inline void fogIarm_segmentEnd(const char *, int, const char *) {}
static inline void fogIarm_recordingStatusChange(const char *, int, const char *) {}
static inline void fogIarm_NotifyFogDVRReadyState() {}
static inline std::string fogIarm_getTR181FogConfig(char * const) { return ""; }
static inline void getActiveInterfaceEventHandler(const char *, IARM_EventId_t, void *, size_t) {}

#endif // _FOG_EVENT_H_
