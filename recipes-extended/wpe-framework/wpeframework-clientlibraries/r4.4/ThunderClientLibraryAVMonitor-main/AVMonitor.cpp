#include "Module.h"
#include <vector>
#include <string>
#include <cstring>

#include "avmonitor.h"
#include <interfaces/IAVMonitor.h>

using namespace WPEFramework;
namespace {

WPEFramework::Exchange::IAVMonitor::EventInfo ConvertEventInfo(const avmonitor_event_t& other)
{
    WPEFramework::Exchange::IAVMonitor::EventInfo result;
    result.timeStamp = other.timeStamp;
    result.id = other.id;
    result.type = static_cast<WPEFramework::Exchange::IAVMonitor::EventInfo::EventType>(other.type);
    result.stateChange = static_cast<WPEFramework::Exchange::IAVMonitor::EventInfo::GstStateChange>(other.stateChange);
    result.windowWidth = other.windowWidth;
    result.windowHeight = other.windowHeight;
    result.visible = other.visible;
    result.zorder = other.zorder;
    result.startPTS = other.startPTS;
    result.firstPTS = other.firstPTS;
    result.currentPTS = other.currentPTS;
    result.position = other.position;
    result.frameRenderCount = other.frameRenderCount;
    result.frameDropCount = other.frameDropCount;
    result.fps = other.fps;
    result.fpsMean = other.fpsMean;
    if(other.gstCaps != NULL){
        result.gstCaps = std::string(other.gstCaps, std::strlen(other.gstCaps));
    }
    return result;
}

static string Callsign()
{
    static constexpr const TCHAR Default[] = _T("AVMonitor");
    return (Default);
}

class AVMonitorLink : public WPEFramework::RPC::SmartInterfaceType<WPEFramework::Exchange::IAVMonitor> {
private:
    using BaseClass = WPEFramework::RPC::SmartInterfaceType<WPEFramework::Exchange::IAVMonitor>;

    AVMonitorLink()
        : BaseClass()
        , _lock()
        , _avMonitorInterface(nullptr)
    {
        ASSERT(_singleton==nullptr);
        _singleton = this;
        BaseClass::Open(RPC::CommunicationTimeOut, BaseClass::Connector(), Callsign());
    }

public:
    AVMonitorLink(const AVMonitorLink&) = delete;
    AVMonitorLink& operator=(const AVMonitorLink&) = delete;
    ~AVMonitorLink() override
    {
        BaseClass::Close(WPEFramework::Core::infinite);
        _singleton = nullptr;
    }

    static AVMonitorLink& Instance()
    {
        static AVMonitorLink *instance = new AVMonitorLink;
        ASSERT(instance!=nullptr);
        return *instance;
    }

    static void Dispose()
    {
        ASSERT(_singleton != nullptr);

        if (_singleton != nullptr) {
            delete _singleton;
        }
    }

    uint32_t AVMonitorRegisterEvent(const avmonitor_event_t& event)
    {
        uint32_t errorCode = avmonitor_status::AVMONITOR_ERROR_GENERAL;

        _lock.Lock();

        if (_avMonitorInterface != nullptr) {
            auto convertedEvent = ConvertEventInfo(event);
            if(_avMonitorInterface->RegisterEvent(convertedEvent) == Core::ERROR_NONE){

            }
            errorCode = avmonitor_status::AVMONITOR_OK;
        }

        _lock.Unlock();
        return errorCode;
    }

private:
    void Operational(const bool upAndRunning) override
    {
        _lock.Lock();
        if (upAndRunning) {
            if (_avMonitorInterface == nullptr) {
                _avMonitorInterface = BaseClass::Interface();
            }
        } else {
            if (_avMonitorInterface != nullptr) {
                _avMonitorInterface->Release();
                _avMonitorInterface = nullptr;
            }
        }
        _lock.Unlock();
    }


private:
    Core::CriticalSection _lock;
    static AVMonitorLink* _singleton;
    Exchange::IAVMonitor* _avMonitorInterface;
};

AVMonitorLink* AVMonitorLink::_singleton = nullptr;


}// nameless namespace

extern "C" {

uint32_t avmonitor_register_event(const avmonitor_event_t event) {
    return AVMonitorLink::Instance().AVMonitorRegisterEvent(event);
}

void avmonitor_dispose() {
    AVMonitorLink::Dispose();
}
} // extern "C"

