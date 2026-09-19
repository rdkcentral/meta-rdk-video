#include "Module.h"
#include <algorithm>
#include <array>
#include <bitset>
#include <core/ProcessInfo.h>
#include <core/SystemInfo.h>
#include <fstream>
#include <interfaces/IMemory.h>
#include <interfaces/IFCResourceMonitor.h>
#include <iomanip>
#include <numeric>
#include <sstream>
#include <unistd.h>
#include <unordered_map>
#include <vector>

namespace WPEFramework {
namespace Plugin {
    namespace {
        typedef std::array<string, 17> KeysArray;

        string DivideWithPrecision(const double numerator, const double denominator, const uint32_t precisision)
        {
            ASSERT(denominator != 0);
            ASSERT(precisision != 0);

            double result = numerator / denominator;
            std::ostringstream oss;
            oss << std::fixed << std::setprecision(precisision) << result;

            return oss.str();
        }

        string KbToMb(const uint64_t kb)
        {
            return DivideWithPrecision(kb, 1024, 3);
        }

        string CpuAvgLoadToFloat(const uint64_t avgLoad)
        {
            return DivideWithPrecision(static_cast<double>(avgLoad), 65536.0, 2);
        }

        string StringsListToString(const std::list<string>& stringList, const string& delimiter = " ")
        {
            std::ostringstream oss;
            for (auto it = stringList.begin(); it != stringList.end(); ++it) {
                if (it != stringList.begin()) {
                    oss << delimiter;
                }
                oss << *it;
            }
            return oss.str();
        }

        // Custom std::variant implementation
        class ValueBase {
        public:
            virtual ~ValueBase() {}

            const string& GetUnit() const { return _unit; }

            virtual string GetTypeInfo() const = 0;

        protected:
            string _unit;
        };

        // Generic
        template <typename T>
        class Value : public ValueBase {
        public:
            Value(const T& value, const string& unit = "")
                : _value(value)
            {
                this->_unit = unit;
            }

            T GetValue() const { return _value; }

            string GetTypeInfo() const override
            {
                return typeid(T).name();
            }

        private:
            T _value;
        };

        // Specialization for string
        template <>
        class Value<string> : public ValueBase {
        public:
            Value(const string& value, const string& unit = "")
                : _value(new string(value))
            {
                this->_unit = unit;
            }

            string GetValue() const { return *_value; }

            string GetTypeInfo() const override
            {
                return typeid(string).name();
            }

        private:
            std::unique_ptr<string> _value;
        };

        // Specialization for double
        template <>
        class Value<double> : public ValueBase {
        public:
            Value(const double value, const string& unit = "")
                : _value(value)
            {
                this->_unit = unit;
            }

            double GetValue() const
            {
                // Limit decimal place to 2
                double multiplier = std::pow(10.0, 2);
                return std::round(_value * multiplier) / multiplier;
            }

            string GetTypeInfo() const override
            {
                return typeid(double).name();
            }

        private:
            double _value;
        };

        using KeyValueMap = std::unordered_map<string, std::unique_ptr<ValueBase>>;
        template <typename T>
        void InsertItemToKeyValueMap(KeyValueMap& map, const string& key, const T& value, const string& unit = "")
        {
            map[key] = std::unique_ptr<ValueBase>(new Value<T>(value, unit));
        }

        // Custom std::optional implementation
        template <typename T>
        class Optional {
        private:
            bool _hasValue;
            T _value;

        public:
            Optional()
                : _hasValue(false)
            {
            }
            Optional(const T& value)
                : _hasValue(true)
                , _value(value)
            {
            }
            bool HasValue() const { return _hasValue; }
            const T& Value() const { return _value; }
            T& Value() { return _value; }
        };

        // Helper function to get value from KeValueMap item in safe way
        template <typename T>
        Optional<T> GetValue(const ValueBase* value)
        {
            const Value<T>* derived = dynamic_cast<const Value<T>*>(value);
            if (derived) {
                return Optional<T>(derived->GetValue());
            }
            return Optional<T>();
        }

        string MapToJsonLines(const KeyValueMap& data, const KeysArray& filterKeys)
        {
            std::ostringstream jsonLines;
            bool isFirst = true;
            jsonLines << "{";

            for (const auto& key : filterKeys) {
                auto it = data.find(key);
                if (it != data.end()) {
                    if (!isFirst) {
                        jsonLines << ",";
                    }
                    isFirst = false;
                    jsonLines << "\"" << key << "\": {";
                    jsonLines << "\"value\":";
                    string typeInfo = it->second->GetTypeInfo();
                    auto itValue = it->second.get();
                    if (typeInfo == typeid(string).name()) {
                        Optional<std::string> result = GetValue<std::string>(itValue);
                        if (result.HasValue()) {
                            jsonLines << "\"" << result.Value() << "\"";
                        } else {
                            return "";
                        }
                    } else if (typeInfo == typeid(uint64_t).name()) {
                        Optional<uint64_t> result = GetValue<uint64_t>(itValue);
                        if (result.HasValue()) {
                            jsonLines << result.Value();
                        } else {
                            return "";
                        }
                    } else if (typeInfo == typeid(double).name()) {
                        Optional<double> result = GetValue<double>(itValue);
                        if (result.HasValue()) {
                            jsonLines << result.Value();
                        } else {
                            return "";
                        }
                    } else {
                        // Invalid data type
                        return "";
                    }
                    if (!it->second->GetUnit().empty()) {
                        jsonLines << ",";
                        jsonLines << "\"unit\":\"" << it->second->GetUnit() << "\"";
                    }
                    jsonLines << "}";
                }
            }
            jsonLines << "}\n";
            return jsonLines.str();
        }
    }

    class JsonlFile {
    public:
        JsonlFile() = default;
        JsonlFile(const JsonlFile&) = delete;
        JsonlFile& operator=(const JsonlFile&) = delete;

        ~JsonlFile()
        {
            if (_file.IsOpen()) {
                _file.Close();
            }
        }

        bool Create(const string& storageDirPath, const string& description)
        {
            bool result = false;
            string filePath = GenerateTimestampedFileName(storageDirPath, description, "jsonl");

            if (_file.IsOpen()) {
                _file.Close();
            }

            _file = Core::File(filePath);
            result = _file.Create();

            if (result) {
                _path = filePath;
                TRACE(Trace::Information, (_T("File created successfully: <%s>."), filePath));
            } else {
                TRACE(Trace::Error, (_T("Failed to create file <%s>."), filePath));
            }
            return result;
        }

        bool Save(const string data)
        {
            bool result = false;
            if (_file.IsOpen()) {
                uint32_t bytes = _file.Write(reinterpret_cast<const uint8_t*>(data.c_str()), data.length());
                if (bytes != 0) {
                    result = true;
                } else {
                    TRACE(Trace::Error, (_T("Failed to save data to file.")));
                }
            }
            return result;
        }

        string Load()
        {
            uint8_t buffer[1024 * 1024];
            uint32_t size = 0;

            if (_file.IsOpen()) {
                size = _file.Read(buffer, sizeof(buffer));
            }
            string result(reinterpret_cast<char*>(buffer), size);
            return result;
        }

        bool Exists()
        {
            return _file.Exists();
        }

        bool IsOpen()
        {
            return _file.IsOpen();
        }

        string GetPath()
        {
            return _path;
        }

    private:
        Core::File _file;
        string _path;

        string GenerateTimestampedFileName(const string& dirPath, const string& description, const string& extension)
        {
            auto now = std::chrono::system_clock::now();
            auto time_t_now = std::chrono::system_clock::to_time_t(now);
            auto ms = std::chrono::duration_cast<std::chrono::milliseconds>(now.time_since_epoch()) % 1000;

            std::tm tm = *std::localtime(&time_t_now);
            std::ostringstream oss;
            oss << std::put_time(&tm, "%Y%m%d_%H%M%S")
                << std::setw(3) << std::setfill('0') << ms.count()
                << "_" << description << "." << extension;

            string filePath = dirPath;
            if (!filePath.empty() && filePath.back() != '/') {
                filePath += '/';
            }
            filePath += oss.str();

            return filePath;
        }
    };

    class DataSample {
    public:
        template <typename... Args>
        string Set(Args... args)
        {
            Clean(); // Make sure that stream is empty
            if (sizeof...(args) > 0) {
                _content << '{';
                (void)std::initializer_list<int>{ (_content << args << ",", 0)... };
                _content.seekp(_content.tellp() - std::streamoff(1)); // Move back one position to overwrite the last comma
                _content << "}\n";
            }
            return _content.str();
        }

        string Get()
        {
            return _content.str();
        }

        void Clean()
        {
            _content.str("");
            _content.clear();
        }

        DataSample()
        {
            _content.precision(2);
        }

        DataSample(const DataSample&) = delete;
        DataSample& operator=(const DataSample&) = delete;

        ~DataSample()
        {
        }

    private:
        std::ostringstream _content;
    };

    class ResourceMonitorImplementation : public Exchange::IFCResourceMonitor {
    public:
        ResourceMonitorImplementation()
            : _implementation(nullptr)
        {
        }

        ~ResourceMonitorImplementation() override = default;

        void Register(IFCResourceMonitor::INotification* sink)
        {
            _guard.Lock();
            auto item = std::find(_notifications.begin(), _notifications.end(), sink);
            ASSERT(item == _notifications.end());
            if (item == _notifications.end()) {
                sink->AddRef();
                _notifications.push_back(sink);
            }
            _guard.Unlock();
        }

        void Unregister(IFCResourceMonitor::INotification* sink)
        {
            _guard.Lock();
            auto item = std::find(_notifications.begin(), _notifications.end(), sink);
            ASSERT(item != _notifications.end());

            if (item != _notifications.end()) {
                _notifications.erase(item);
                (*item)->Release();
            }
            _guard.Unlock();
        }

        uint32_t Configure(PluginHost::IShell* service) override
        {
            ASSERT(service != nullptr);

            string outputPath;
            Config config;
            config.FromString(service->ConfigLine());

            if ((config.ProcessPrefixes.IsSet() == true) && (config.ProcessPrefixes.Length() > 0)) {
                Core::JSON::ArrayType<Core::JSON::String>::Iterator index(config.ProcessPrefixes.Elements());
                _processPrefixes.reserve(config.ProcessPrefixes.Length());

                while (index.Next() == true) {
                    _processPrefixes.push_back(index.Current().Value());
                }
            } else {
                return Core::ERROR_INCOMPLETE_CONFIG;
            }

            if ((config.ResultFileLocation.IsSet() == true) && (config.ResultFileLocation.Value().length() > 0)) {
                outputPath = config.ResultFileLocation.Value();
            } else {
                outputPath = service->PersistentPath();
            }

            if (Core::File(outputPath).IsDirectory() == false) {
                if (Core::Directory(outputPath.c_str()).CreatePath() == false) {
                    TRACE(Trace::Error, (_T("Failed to create persistent storage folder [%s]"), outputPath.c_str()));
                    return Core::ERROR_INCOMPLETE_CONFIG;
                }
            }
            _implementation.reset(new MemAndCpuMonitor(outputPath, *this));
            return Core::ERROR_NONE;
        }

        uint32_t StartCapture(const uint32_t interval, const IFCResourceMonitor::CaptureMode captureMode) override
        {
            uint32_t result = _implementation->Start(interval, captureMode, _processPrefixes);

            if (result) {
                return Core::ERROR_NONE;
            } else {
                return Core::ERROR_GENERAL;
            }
        }

        uint32_t StopCapture() override
        {
            uint32_t result = _implementation->Stop();
            if (result) {
                return Core::ERROR_NONE;
            } else {
                return Core::ERROR_GENERAL;
            }
        }

        uint32_t GetCurrentUsage(const string& processPrefix, const IFCResourceMonitor::MeasurementType measurementType) override
        {
            uint32_t result = _implementation->GetCurrentUsage(processPrefix, measurementType);
            if (result) {
                return Core::ERROR_NONE;
            } else {
                return Core::ERROR_GENERAL;
            }
        }

        uint32_t AddLabel(const string& annotation) override
        {
            uint32_t result = _implementation->AddLabel(annotation);
            if (result) {
                return Core::ERROR_NONE;
            } else {
                return Core::ERROR_GENERAL;
            }
        }

        void Notify(const string& data)
        {
            Exchange::IFCResourceMonitor::EventData payload;
            payload = data;
            for (auto* notification : _notifications) {
                notification->OnResourceMonitorData(payload);
            }
        }

        BEGIN_INTERFACE_MAP(ResourceMonitorImplementation)
        INTERFACE_ENTRY(Exchange::IFCResourceMonitor)
        END_INTERFACE_MAP

    private:
        ResourceMonitorImplementation(const ResourceMonitorImplementation&) = delete;
        ResourceMonitorImplementation& operator=(const ResourceMonitorImplementation&) = delete;

        Core::CriticalSection _guard;
        std::vector<string> _processPrefixes;

        class MemAndCpuMonitor;
        std::unique_ptr<MemAndCpuMonitor> _implementation;
        std::vector<Exchange::IFCResourceMonitor::INotification*> _notifications;

        class Config : public Core::JSON::Container {
        public:
            Config(const Config&) = delete;
            Config& operator=(const Config&) = delete;

            Config()
                : Core::JSON::Container()
                , ResultFileLocation()
                , ProcessPrefixes()
            {
                Add(_T("processprefixes"), &ProcessPrefixes);
                Add(_T("resultfilelocation"), &ResultFileLocation);
            }
            ~Config() override = default;

        public:
            Core::JSON::String ResultFileLocation;
            Core::JSON::ArrayType<Core::JSON::String> ProcessPrefixes;
        };

        class NotificationWorker {
        public:
            explicit NotificationWorker(ResourceMonitorImplementation& parent)
                : _parent(parent)
                , _measurementsFile()
                , _labelsFile()
                , _job(*this)
            {
            }

            ~NotificationWorker()
            {
                _job.Revoke();
            }

            void Dispatch()
            {
                if (_measurementsFile.Exists() && _labelsFile.Exists()) {
                    string payload = Load();
                    _parent.Notify(payload);
                } else {
                    TRACE(Trace::Error, (_T("Could not send summary notification. Input file does not exist.")));
                }
            }

            void Submit(const string measurementsFilePath, const string labelsFilePath)
            {
                _measurementsFile = Core::File(measurementsFilePath);
                _labelsFile = Core::File(labelsFilePath);
                _job.Submit();
            }

        private:
            friend Core::ThreadPool::JobType<NotificationWorker&>;
            ResourceMonitorImplementation& _parent;
            Core::File _measurementsFile;
            Core::File _labelsFile;
            Core::WorkerPool::JobType<NotificationWorker&> _job;

            string Load()
            {
                uint8_t buffer[1024] = {};
                uint32_t size;
                string content;
                if (_measurementsFile.Open()) {
                    while ((size = _measurementsFile.Read(buffer, sizeof(buffer))) != 0) {
                        content.append(string(reinterpret_cast<char*>(buffer), size));
                    }
                    _measurementsFile.Close();
                }

                if (_labelsFile.Size() > 0) {
                    content.append("{\"marker\": \"labelMarker\"}\n");
                    if (_labelsFile.Open()) {
                        while ((size = _labelsFile.Read(buffer, sizeof(buffer))) != 0) {
                            content.append(string(reinterpret_cast<char*>(buffer), size));
                        }
                        _labelsFile.Close();
                    }
                }

                return content;
            }
        };

        class MemAndCpuMonitor {
        public:
            explicit MemAndCpuMonitor(const string& defaultStoragePath, ResourceMonitorImplementation& parent)
                : _userCpuTime(0)
                , _systemCpuTime(0)
                , _storagePath(defaultStoragePath)
                , _labelFile()
                , _measurementFile()
                , _notificationPayload()
                , _interval(0)
                , _processesToFilter()
                , _job(*this)
                , _jobLastScheduledTime(Core::Time::Now())
                , _jobIsFirstRun(true)
                , _captureMode(IFCResourceMonitor::CaptureMode::NONE)
                , _isCaptureInProgress(false)
                , _isCurrentUsageMode(false)
                , _parent(parent)
                , _notificationWorker(parent)
            {
            }

            ~MemAndCpuMonitor()
            {
                _job.Revoke();
            }

            bool Start(const uint32_t interval, const IFCResourceMonitor::CaptureMode captureMode, const std::vector<string>& processPrefixes)
            {
                ASSERT(interval > 0);
                _stateGuard.Lock();
                bool result = false;
                if (_isCaptureInProgress) {
                    TRACE(Trace::Warning, (_T("Can not start another session. Capture is in progress.")));
                    _stateGuard.Unlock();
                    return result;
                }
                TRACE(Trace::Information, (_T("Session started. Capture is in progress.")));
                _isCaptureInProgress = true;
                _interval = interval;
                _captureMode = captureMode;
                _processesToFilter = processPrefixes;

                switch (_captureMode) {
                case IFCResourceMonitor::CaptureMode::NONE:
                    break;
                case IFCResourceMonitor::CaptureMode::LIVE:
                    result = true;
                    _job.Submit();
                    break;
                case IFCResourceMonitor::CaptureMode::SUMMARY:
                case IFCResourceMonitor::CaptureMode::MIXED:
                    Core::Directory(_storagePath.c_str()).Destroy(); // Remove all existing files from storage
                    result = _measurementFile.Create(_storagePath, "MemAndCpu_Measurements") && _labelFile.Create(_storagePath, "MemAndCpu_Labels");
                    if (result) {
                        _job.Submit();
                    } else {
                        TRACE(Trace::Error, (_T("Can not create output files. Can not acquire data.")));
                    }
                    break;
                default:
                    TRACE(Trace::Error, (_T("Invalid start mode.")));
                    break;
                };
                _stateGuard.Unlock();
                return result;
            }

            bool Stop()
            {
                _stateGuard.Lock();
                bool result = false;
                if (!_isCaptureInProgress) {
                    TRACE(Trace::Warning, (_T("Can not stop session. ResourceMonitor is not active.")));
                    _stateGuard.Unlock();
                    return result;
                }

                if (IFCResourceMonitor::CaptureMode::NONE == _captureMode) {
                    TRACE(Trace::Warning, (_T("Can not stop data capture. No active acquisition session.")));
                    return result;
                }

                if ((IFCResourceMonitor::CaptureMode::SUMMARY == _captureMode) || (IFCResourceMonitor::CaptureMode::MIXED == _captureMode)) {
                    _notificationWorker.Submit(_measurementFile.GetPath(), _labelFile.GetPath());
                }

                _captureMode = IFCResourceMonitor::CaptureMode::NONE;
                _isCaptureInProgress = false;
                _job.Revoke();
                result = true;
                _stateGuard.Unlock();
                return result;
            }

            bool GetCurrentUsage(const string& processPrefix, const IFCResourceMonitor::MeasurementType measurementType)
            {
                _stateGuard.Lock();
                bool result = false;

                if (_isCaptureInProgress) {
                    TRACE(Trace::Warning, (_T("Can not get current usage. ResourceMonitor is capturing data now.")));
                    _stateGuard.Unlock();
                    return false;
                }
                _processesToFilter.clear();
                _processesToFilter.push_back(processPrefix);
                _captureMode = IFCResourceMonitor::CaptureMode::LIVE;
                _measurementType = measurementType;
                _isCurrentUsageMode = true;
                _job.Submit();
                result = true;
                _stateGuard.Unlock();
                return result;
            }

            bool AddLabel(const string& annotation)
            {
                _stateGuard.Lock();
                bool result = false;
                std::ostringstream jsonLine;

                if (!_isCaptureInProgress) {
                    TRACE(Trace::Error, (_T("Can not add label. ResourceMonitor is not capturing data now.")));
                    _stateGuard.Unlock();
                    return result;
                }

                if (annotation.length() == 0) {
                    TRACE(Trace::Error, (_T("Invalid label. Empty annotation.")));
                    _stateGuard.Unlock();
                    return result;
                }

                jsonLine << "{";
                jsonLine << "\"time\":";
                jsonLine << "{";
                jsonLine << "\"value\":" << Core::Time::Now().Ticks() / 1000;
                jsonLine << ",";
                jsonLine << "\"unit\":\"ms\"";
                jsonLine << "}";
                jsonLine << ",";
                jsonLine << "\"annotation\":";
                jsonLine << "{";
                jsonLine << "\"value\":\"" << annotation << "\"";
                jsonLine << "}";
                jsonLine << "}\n";

                switch (_captureMode) {
                case IFCResourceMonitor::CaptureMode::LIVE:
                    _parent.Notify(jsonLine.str());
                    result = true;
                    break;
                case IFCResourceMonitor::CaptureMode::SUMMARY:
                    result = _labelFile.Save(jsonLine.str());
                    break;
                case IFCResourceMonitor::CaptureMode::MIXED:
                    _parent.Notify(jsonLine.str());
                    result = _labelFile.Save(jsonLine.str());
                    break;
                default:
                    TRACE(Trace::Error, (_T("Can not add label. Invalid mode.")));
                    break;
                };
                _stateGuard.Unlock();
                return result;
            }

            void ProcessWorker(const Core::ProcessInfo& process)
            {
                string sample;

                CalculateCpuUsage(process.Id());
                if (_isCurrentUsageMode) {
                    sample = GetProcessData(process, _measurementType);
                } else {
                    sample = GetProcessData(process, IFCResourceMonitor::MeasurementType::ALL);
                }

                if (sample.empty()) {
                    TRACE(Trace::Warning, (_T("ProcessWorker received empty data sample.")));
                }

                switch (_captureMode) {
                case IFCResourceMonitor::CaptureMode::LIVE:
                    _notificationPayload << sample;
                    break;
                case IFCResourceMonitor::CaptureMode::SUMMARY:
                    _measurementFile.Save(sample);
                    break;
                case IFCResourceMonitor::CaptureMode::MIXED:
                    _notificationPayload << sample;
                    _measurementFile.Save(sample);
                    break;
                default:
                    TRACE(Trace::Error, (_T("Invalid ResourceMonitor mode.")));
                    break;
                };
            }

            void SendLiveModeNotification()
            {
                if ((IFCResourceMonitor::CaptureMode::LIVE == _captureMode) || (IFCResourceMonitor::CaptureMode::MIXED == _captureMode)) {
                    _parent.Notify(_notificationPayload.str());
                    _notificationPayload.str("");
                    _notificationPayload.clear();
                }
            }

            void Dispatch()
            {
                _stateGuard.Lock();
                auto start = Core::Time::Now();
                if (_jobIsFirstRun) {
                    _jobLastScheduledTime = start;
                    _jobIsFirstRun = false;
                }

                for (const auto& processName : _processesToFilter) {
                    std::list<Core::ProcessInfo> processes;
                    Core::ProcessInfo::FindByName(processName, false, processes);

                    if (!processes.empty()) {
                        for (const Core::ProcessInfo& process : processes) {
                            ProcessWorker(process);
                        }
                    }
                    SendLiveModeNotification();
                }
                auto end = Core::Time::Now();

                if (_isCurrentUsageMode) {
                    _captureMode = IFCResourceMonitor::CaptureMode::NONE;
                    _isCurrentUsageMode = false;
                    _stateGuard.Unlock();
                    _job.Revoke();
                } else {
                    // Reschedule if started and adjust to fixed rate.
                    uint32_t processingDurationMs = static_cast<uint32_t>(end.Ticks() - start.Ticks()) / Core::Time::TicksPerMillisecond;
                    if (processingDurationMs < _interval) {
                        _jobLastScheduledTime = _jobLastScheduledTime.Add(_interval);
                    } else {
                        // Adding minimal delay is necessary for scheduler to do its work.
                        _jobLastScheduledTime = end.Add(1);
                    }

                    _stateGuard.Unlock();
                    _job.Reschedule(_jobLastScheduledTime);
                }
            }

        private:
            friend Core::ThreadPool::JobType<MemAndCpuMonitor&>;
            struct Time {
                Time()
                    : totalTime(0)
                    , sTime(0)
                    , uTime(0)
                    , prevTotalTime(0)
                    , prevUTime(0)
                    , prevSTime(0)
                {
                }

                uint64_t totalTime;
                uint64_t sTime;
                uint64_t uTime;
                uint64_t prevTotalTime;
                uint64_t prevUTime;
                uint64_t prevSTime;
            };

            // NOTE: these two indicate usage of the whole CPU, not the single core (as 'top' shows by default)
            double _userCpuTime;
            double _systemCpuTime;

            std::map<Core::process_t, Time> _processTimeInfo;

            string _storagePath;
            JsonlFile _labelFile;
            JsonlFile _measurementFile;
            std::ostringstream _notificationPayload;
            uint32_t _interval;
            std::vector<string> _processesToFilter;

            Core::CriticalSection _guard;
            Core::WorkerPool::JobType<MemAndCpuMonitor&> _job;
            Core::Time _jobLastScheduledTime;
            bool _jobIsFirstRun;
            IFCResourceMonitor::CaptureMode _captureMode;
            bool _isCaptureInProgress;
            bool _isCurrentUsageMode;
            IFCResourceMonitor::MeasurementType _measurementType;
            ResourceMonitorImplementation& _parent;
            NotificationWorker _notificationWorker;
            Core::CriticalSection _stateGuard;

            void UpdateTotalTime(Core::process_t pid)
            {
                std::ifstream stat("/proc/stat");
                if (!stat.is_open()) {
                    TRACE(Trace::Error, (_T("Could not open /proc/stat.")));

                    return;
                }
                string line;
                std::istringstream iss;

                std::getline(stat, line);
                iss.str(line);

                string dummy;
                uint64_t user = 0, nice = 0, system = 0, idle = 0, iowait = 0, irq = 0, softirq = 0, steal = 0, guest = 0;

                iss >> dummy >> user >> nice >> system >> idle >> iowait >> irq >> softirq >> steal >> guest;
                _processTimeInfo[pid].totalTime = user + nice + system + idle + iowait + irq + softirq + steal + guest;
            }

            void UpdateProcessTimes(Core::process_t pid)
            {
                std::ostringstream pathToSmaps;
                pathToSmaps << "/proc/" << pid << "/stat";

                std::ifstream stat(pathToSmaps.str());
                if (!stat.is_open()) {
                    TRACE(Trace::Error, (_T("Could not open %s."), pathToSmaps.str()));
                    return;
                }

                string uTimeString, sTimeString;

                // ignoring 13 words spearated by space
                for (uint32_t i = 0; i < 13; ++i) {
                    stat.ignore(std::numeric_limits<std::streamsize>::max(), ' ');
                }

                stat >> _processTimeInfo[pid].uTime >> _processTimeInfo[pid].sTime;
            }

            void CalculateCpuUsage(Core::process_t pid)
            {
                UpdateTotalTime(pid);
                UpdateProcessTimes(pid);

                _userCpuTime = 100.0 * (_processTimeInfo[pid].uTime - _processTimeInfo[pid].prevUTime) / (_processTimeInfo[pid].totalTime - _processTimeInfo[pid].prevTotalTime);

                _systemCpuTime = 100.0 * (_processTimeInfo[pid].sTime - _processTimeInfo[pid].prevSTime) / (_processTimeInfo[pid].totalTime - _processTimeInfo[pid].prevTotalTime);

                _processTimeInfo[pid].prevTotalTime = _processTimeInfo[pid].totalTime;
                _processTimeInfo[pid].prevSTime = _processTimeInfo[pid].sTime;
                _processTimeInfo[pid].prevUTime = _processTimeInfo[pid].uTime;
            }

            KeyValueMap GetResourceUsageDetails(const Core::ProcessInfo& process)
            {
                KeyValueMap result;
                auto timestamp = Core::Time::Now().Ticks() / 1000; // ms from epoch
                process.MemoryStats();
                auto memSnapshot = Core::SystemInfo::Instance().TakeMemorySnapshot();
                uint64_t totalRam = memSnapshot.Total();
                uint64_t freeRam = memSnapshot.Free();
                uint64_t availableRam = memSnapshot.Available();
                uint64_t cpuLoad = Core::SystemInfo::Instance().GetCpuLoad();
                uint64_t* cpuLoadAvgPtr = Core::SystemInfo::Instance().GetCpuLoadAvg();
                auto processChildren = process.Children();
                string cpidString;
                while (processChildren.Next() == true) {
                    if (!cpidString.empty()) {
                        cpidString += ", ";
                    }
                    auto childPID = processChildren.Current().Id();
                    cpidString.append(std::to_string(childPID));
                }
                cpidString.insert(0, "[");
                cpidString.push_back(']');

                string cmdLine = StringsListToString(process.CommandLine());
                cmdLine.erase(std::remove(cmdLine.begin(), cmdLine.end(), '"'), cmdLine.end());

                InsertItemToKeyValueMap(result, "name", string(process.Name()), "");
                InsertItemToKeyValueMap(result, "time", static_cast<uint64_t>(timestamp), "ms");
                InsertItemToKeyValueMap(result, "pid", static_cast<uint64_t>(process.Id()), "");
                InsertItemToKeyValueMap(result, "cpids", string(cpidString), "");
                InsertItemToKeyValueMap(result, "uss", static_cast<uint64_t>(process.USS() / 1024), "MB");
                InsertItemToKeyValueMap(result, "pss", static_cast<uint64_t>(process.PSS() / 1024), "MB");
                InsertItemToKeyValueMap(result, "rss", static_cast<uint64_t>(process.RSS() / 1024), "MB");
                InsertItemToKeyValueMap(result, "userTotalCpu", static_cast<double>(_userCpuTime), "ms");
                InsertItemToKeyValueMap(result, "systemTotalCpu", static_cast<double>(_systemCpuTime), "ms");
                InsertItemToKeyValueMap(result, "cpuLoad", static_cast<uint64_t>(cpuLoad), "%");
                InsertItemToKeyValueMap(result, "cpuLoadAvg1m", static_cast<double>(cpuLoadAvgPtr[0] / 65536.0), "%");
                InsertItemToKeyValueMap(result, "cpuLoadAvg5m", static_cast<double>(cpuLoadAvgPtr[1] / 65536.0), "%");
                InsertItemToKeyValueMap(result, "cpuLoadAvg15m", static_cast<double>(cpuLoadAvgPtr[2] / 65536.0), "%");
                InsertItemToKeyValueMap(result, "totalRam", static_cast<uint64_t>(totalRam / 1024), "MB");
                InsertItemToKeyValueMap(result, "freeRam", static_cast<uint64_t>(freeRam / 1024), "MB");
                InsertItemToKeyValueMap(result, "availableRam", static_cast<uint64_t>(availableRam / 1024), "MB");
                InsertItemToKeyValueMap(result, "command", string(cmdLine), "");

                return result;
            }

            string GetProcessData(const Core::ProcessInfo& process, IFCResourceMonitor::MeasurementType measurementType)
            {
                string result;
                auto measurements = GetResourceUsageDetails(process);
                bool isInvalidMeasurementType = false;
                KeysArray keys;

                switch (measurementType) {
                case IFCResourceMonitor::MeasurementType::ALL: {
                    keys = { "time", "name", "pid", "cpids", "uss", "pss", "rss", "userTotalCpu", "systemTotalCpu", "cpuLoad", "cpuLoadAvg1m", "cpuLoadAvg5m", "cpuLoadAvg15m", "totalRam", "freeRam", "availableRam", "command" };
                    break;
                }
                case IFCResourceMonitor::MeasurementType::ALLMEMORY: {
                    keys = { "time", "name", "pid", "cpids", "uss", "pss", "rss", "totalRam", "freeRam", "availableRam", "command" };
                    break;
                }
                case IFCResourceMonitor::MeasurementType::ALLCPU: {
                    keys = { "time", "name", "pid", "cpids", "userTotalCpu", "systemTotalCpu", "cpuLoad", "cpuLoadAvg1m", "cpuLoadAvg5m", "cpuLoadAvg15m", "command" };
                    break;
                }
                case IFCResourceMonitor::MeasurementType::MEMORYUSS: {
                    keys = { "time", "name", "pid", "cpids", "uss", "command" };
                    break;
                }
                case IFCResourceMonitor::MeasurementType::MEMORYPSS: {
                    keys = { "time", "name", "pid", "cpids", "pss", "command" };
                    break;
                }
                case IFCResourceMonitor::MeasurementType::MEMORYRSS: {
                    keys = { "time", "name", "pid", "cpids", "rss", "command" };
                    break;
                }
                case IFCResourceMonitor::MeasurementType::MEMORYRAMFREE: {
                    keys = { "time", "name", "pid", "cpids", "freeRam", "command" };
                    break;
                }
                case IFCResourceMonitor::MeasurementType::MEMORYRAMTOTAL: {
                    keys = { "time", "name", "pid", "cpids", "totalRam", "command" };
                    break;
                }
                case IFCResourceMonitor::MeasurementType::MEMORYRAMAVAILABLE: {
                    keys = { "time", "name", "pid", "cpids", "availableRam", "command" };
                    break;
                }
                case IFCResourceMonitor::MeasurementType::CPULOADTOTAL: {
                    keys = { "time", "name", "pid", "cpids", "cpuLoad", "command" };
                    break;
                }
                case IFCResourceMonitor::MeasurementType::CPULOADTOTALUSER: {
                    keys = { "time", "name", "pid", "cpids", "userTotalCpu", "command" };
                    break;
                }
                case IFCResourceMonitor::MeasurementType::CPULOADTOTALSYSTEM: {
                    keys = { "time", "name", "pid", "cpids", "systemTotalCpu", "command" };
                    break;
                }
                case IFCResourceMonitor::MeasurementType::NONE:
                    TRACE(Trace::Error, (_T("GetProcessData: not defined measurement request.")));
                    isInvalidMeasurementType = true;
                    break;
                default:
                    TRACE(Trace::Error, (_T("GetProcessData: invalid measurement request.")));
                    isInvalidMeasurementType = true;
                    break;
                }

                if (!isInvalidMeasurementType) {
                    result = MapToJsonLines(measurements, keys);
                }

                return result;
            }
        };
    };

    SERVICE_REGISTRATION(ResourceMonitorImplementation, 1, 0)
} /* namespace Plugin */
} /* namespace WPEFramework */
