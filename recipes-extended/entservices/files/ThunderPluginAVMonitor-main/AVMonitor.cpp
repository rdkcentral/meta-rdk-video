#include "AVMonitor.h"
#include <interfaces/json/JsonData_AVMonitor.h>
#include <iostream>

namespace WPEFramework
{
    namespace Plugin
    {
        namespace
        {
            static Metadata<AVMonitor> metadata(
                // Version
                1, 0, 0,
                // Preconditions
                {subsystem::PLATFORM, subsystem::INTERNET},
                // Terminations
                {},
                // Controls
                {});

            Exchange::IAVMonitor::EventInfo CreateBlankEvent(Exchange::IAVMonitor::EventInfo::EventType eventType)
            {
                Exchange::IAVMonitor::EventInfo event;
                event.timeStamp = std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()).count();
                event.id = 0;
                event.type = eventType;
                event.stateChange = Exchange::IAVMonitor::EventInfo::GstStateChange::GST_STATE_UNKNOWN;
                event.windowWidth = -1;
                event.windowHeight = -1;
                event.visible = -1;
                event.zorder = -1;
                event.startPTS = -1;
                event.firstPTS = -1;
                event.currentPTS = -1;
                event.position = -1;
                event.frameRenderCount = -1;
                event.frameDropCount = -1;
                event.fps = -1;
                event.fpsMean = -1;
                event.gstCaps = "";
                return event;
            }
        }

        const string AVMonitor::Initialize(PluginHost::IShell *service)
        {
            ASSERT(service != nullptr);
            string result;

            _config.FromString(service->ConfigLine());

            if (_config.ResultFileLocation.IsSet() && !_config.ResultFileLocation.Value().empty())
            {
                _resultFileLocation = _config.ResultFileLocation.Value();
            }
            else
            {
                _resultFileLocation = service->VolatilePath();
            }

            Exchange::JAVMonitor::Register(*this, this);

            return result;
        }

        void AVMonitor::Deinitialize(PluginHost::IShell *service)
        {
            ASSERT(service != nullptr);
            Exchange::JAVMonitor::Unregister(*this);
        }

        string AVMonitor::Information() const
        {
            return _T("");
        }

        //TODO: interval not used in westeros-sink - fixed time used.
        uint32_t AVMonitor::StartCapture(const uint32_t, const CaptureMode captureMode)
        {

            if (_captureStarted)
            {
                Trace::Error(_T("Capture already started - first stop current session"));
                return Core::ERROR_GENERAL;
            }
            else
            {
                _adminLock.Lock();

                _captureStarted = true;
                _mode = captureMode;
                _id = 0;

                // if not Live Mode it means samples should be captured to the file
                if (_mode == SUMMARY || _mode == MIXED)
                {
                    _fileStoreLock.Lock();
                    _resultFile.Create(_resultFileLocation);
                    _fileStoreLock.Unlock();
                }

                _adminLock.Unlock();

                RegisterEvent(CreateBlankEvent(Exchange::IAVMonitor::EventInfo::EventType::CAPTURE_START));
            }

            return Core::ERROR_NONE;
        }

        uint32_t AVMonitor::StopCapture()
        {

            if (!_captureStarted)
            {
                Trace::Error(_T("Capture already stopped - first start a session"));
                return Core::ERROR_GENERAL;
            }
            else
            {
                _adminLock.Lock();
                _captureStarted = false;
                _adminLock.Unlock();

                RegisterEvent(CreateBlankEvent(Exchange::IAVMonitor::EventInfo::EventType::CAPTURE_STOP));

                if (_mode == SUMMARY || _mode == MIXED)
                {
                    _fileStoreLock.Lock();
                    _resultFile.Close();
                    _notificationWorker.Submit(_resultFile.GetPath());
                    _fileStoreLock.Unlock();
                }
            }

            return Core::ERROR_NONE;
        }

        uint32_t AVMonitor::RegisterEvent(const Exchange::IAVMonitor::EventInfo &event)
        {
            Trace::Information(_T("Registered an event"));
            Core::SafeSyncType<Core::CriticalSection> scopedLock(_adminLock);

            if (_captureStarted)
            {
                Exchange::IAVMonitor::EventInfo eventWithId(event);
                eventWithId.id = ++_id;

                if (_mode == LIVE || _mode == MIXED)
                {
                    _eventNotification.OnNewEvent(eventWithId);
                }
                if (_mode == SUMMARY || _mode == MIXED)
                {
                    JsonData::AVMonitor::OnNewEventParamsData::EventInfoData eventInfoData(eventWithId);
                    string eventInfoDataString;
                    eventInfoData.ToString(eventInfoDataString);
                    eventInfoDataString += "\n";

                    _fileStoreLock.Lock();
                    _resultFile.Save(eventInfoDataString);
                    _fileStoreLock.Unlock();
                }
            }
            else {
                Trace::Error(_T("Unable to register Event - capture session not started"));
                return Core::ERROR_GENERAL;
            }

            return Core::ERROR_NONE;
        }

        uint32_t AVMonitor::Register(IAVMonitor::INotification *)
        {
            return Core::ERROR_NONE;
        }

        uint32_t AVMonitor::Unregister(IAVMonitor::INotification *)
        {
            return Core::ERROR_NONE;
        }

    }
}
