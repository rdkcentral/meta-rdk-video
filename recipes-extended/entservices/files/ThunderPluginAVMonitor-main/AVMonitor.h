#pragma once

#include "Module.h"
#include <interfaces/json/JAVMonitor.h>
#include <unistd.h>
#include <chrono>
#include <iomanip>
#include <numeric>
#include <sstream>

namespace WPEFramework
{
    namespace Plugin
    {
        class AVMonitor : public PluginHost::IPlugin, public PluginHost::JSONRPC, Exchange::IAVMonitor
        {
        public:
            //FIXME: SinkStatisticsFrequency is currently hardcoded on the westeros-sink side.
            class Config : public Core::JSON::Container
            {
            public:
                Config(const Config &) = delete;
                Config &operator=(const Config &) = delete;

                Config()
                    : Core::JSON::Container(),
                    SinkStatisticsFrequency(),
                    ResultFileLocation()
                {
                    Add(_T("sinkstatisticsfrequency"), &SinkStatisticsFrequency);
                    Add(_T("resultfilelocation"), &ResultFileLocation);
                }

                ~Config() override = default;

            public:
                Core::JSON::DecUInt16 SinkStatisticsFrequency;
                Core::JSON::String ResultFileLocation;
            };

            class EventNotification : public Exchange::IAVMonitor::INotification
            {
            public:
                explicit EventNotification(AVMonitor &parent)
                    : _parent(parent)
                {
                }

                ~EventNotification() override = default;

                EventNotification() = delete;
                EventNotification(const EventNotification &) = delete;
                EventNotification &operator=(const EventNotification &) = delete;

                void OnNewEvent(const Exchange::IAVMonitor::EventInfo &event) override
                {
                    Exchange::JAVMonitor::Event::OnNewEvent(_parent, event);
                }
                void OnSummary(const string &events) override
                {
                    Exchange::JAVMonitor::Event::OnSummary(_parent, events);
                }

                BEGIN_INTERFACE_MAP(EventNotification)
                INTERFACE_ENTRY(Exchange::IAVMonitor::INotification)
                END_INTERFACE_MAP

            private:
                AVMonitor &_parent;
            };

            class JsonlFile
            {
            public:
                JsonlFile() = default;

                ~JsonlFile()
                {
                    if (_file.IsOpen())
                    {
                        _file.Close();
                    }
                }

                void Close()
                {
                    _file.Close();
                }

                bool Create(const string &storageDirPath)
                {
                    auto filePath = GenerateTimestampedFileName(storageDirPath, "avmonitor", "jsonl");

                    if (_file.IsOpen())
                    {
                        _file.Close();
                    }

                    _file = Core::File(filePath);
                    bool success = _file.Create();

                    if (!success)
                    {
                        TRACE(Trace::Error, (_T("Failed to create file <%s>."), filePath));
                    }
                    else
                    {
                        TRACE(Trace::Information, (_T("File created successfully: <%s>"), filePath));
                    }
                    _path = filePath;
                    return success;
                }

                void Save(const string &data)
                {
                    if (_file.IsOpen())
                    {
                        _file.Write(reinterpret_cast<const uint8_t *>(data.c_str()), data.length());
                    }
                }

                string GetPath()
                {
                    return _path;
                }

            private:
                Core::File _file;
                string _path;

                // FIXME: This looks like could be optimized.
                string GenerateTimestampedFileName(const string &dirPath, const string &description, const string &extension)
                {
                    auto t = std::time(nullptr);
                    auto tm = *std::localtime(&t);

                    std::ostringstream oss;
                    oss << std::put_time(&tm, "%Y%m%d_%H%M%S") << "_" << description << "." << extension;

                    string filePath = dirPath;
                    if (!filePath.empty() && filePath.back() != '/')
                    {
                        filePath += '/';
                    }
                    filePath += oss.str();

                    return filePath;
                }
            };

            class NotificationWorker
            {
            public:
                explicit NotificationWorker(AVMonitor &parent)
                    : _parent(parent), _job(*this)
                {
                }

                ~NotificationWorker()
                {
                    _job.Revoke();
                }

                void Dispatch()
                {
                    if (_file.Exists())
                    {
                        auto payload = Load();
                        _parent._eventNotification.OnSummary(payload);
                    }
                    else
                    {
                        TRACE(Trace::Error, (_T("Summary could not be retrieved, input file does not exist")));
                    }
                }

                void Submit(const string &filePath)
                {
                    _file = Core::File(filePath);
                    _file.Open();
                    _job.Submit();
                }

            private:
                friend Core::ThreadPool::JobType<NotificationWorker &>;
                Core::File _file;
                AVMonitor &_parent;
                Core::WorkerPool::JobType<NotificationWorker &> _job;

                string Load()
                {
                    uint8_t buffer[1024 * 10];
                    uint32_t size = 0;

                    if (_file.IsOpen())
                    {
                        size = _file.Read(buffer, sizeof(buffer));
                    }
                    string result(reinterpret_cast<char *>(buffer), size);
                    return result;
                }
            };

        public:
            AVMonitor() : _eventNotification(*this),
                          _mode(NONE),
                          _captureStarted(false),
                          _adminLock(),
                          _fileStoreLock(),
                          _id(0),
                          _resultFile(),
                          _notificationWorker(*this),
                          _resultFileLocation("")
            {
            }
            ~AVMonitor() override = default;

            AVMonitor(const AVMonitor &) = delete;
            AVMonitor &operator=(const AVMonitor &) = delete;

            BEGIN_INTERFACE_MAP(AVMonitor)
            INTERFACE_ENTRY(PluginHost::IPlugin)
            INTERFACE_ENTRY(PluginHost::IDispatcher)
            INTERFACE_ENTRY(Exchange::IAVMonitor)
            END_INTERFACE_MAP

            //  IPlugin methods
            // -------------------------------------------------------------------------------------------------------
            const string Initialize(PluginHost::IShell *service) override;
            void Deinitialize(PluginHost::IShell *service) override;
            string Information() const override;

            //  IAVMonitor methods
            // -------------------------------------------------------------------------------------------------------}
            uint32_t StartCapture(const uint32_t interval, const CaptureMode captureMode) override;
            uint32_t StopCapture() override;
            uint32_t RegisterEvent(const Exchange::IAVMonitor::EventInfo &event) override;
            uint32_t Register(IAVMonitor::INotification *) override;
            uint32_t Unregister(IAVMonitor::INotification *) override;

        private:
            Config _config;
            Core::Sink<EventNotification> _eventNotification;
            CaptureMode _mode;
            bool _captureStarted;
            Core::CriticalSection _adminLock;
            Core::CriticalSection _fileStoreLock;
            uint64_t _id;
            JsonlFile _resultFile;
            NotificationWorker _notificationWorker;
            string _resultFileLocation;
        };

    } // namespace
} // namespace
