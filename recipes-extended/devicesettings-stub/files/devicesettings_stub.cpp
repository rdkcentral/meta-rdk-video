/**
 * devicesettings_stub.cpp
 *
 * No-op stub implementation of libds (devicesettings client library).
 *
 * PURPOSE
 *   Provides all device:: C++ symbols exported by the real libds.so so that
 *   non-Thunder clients (ctrlm-main, tr69hostif, aamp, hdmicec, netflix, ...)
 *   can compile, link, and load without a running dsMgr daemon.
 *
 * BEHAVIOUR
 *   - Manager::Initialize / DeInitialize    → no-op (no IARM, no HAL)
 *   - All query methods                     → return safe defaults / empty
 *   - All setter methods                    → silently accepted, no-op
 *   - HDCP queries                          → "unauthenticated / not supported"
 *   - isDisplayConnected                    → false
 *   - isEnabled (ports)                     → true  (port appears up)
 *
 * ROLLBACK
 *   Replace 'devicesettings-stub' with 'devicesettings' in all client recipes.
 *
 * Copyright 2024 RDK Management
 * Apache-2.0 License
 */

/* -----------------------------------------------------------------------
 * IarmImpl must be a complete type before host.hpp instantiates
 * std::unique_ptr<DefaultImpl> (alias DefaultImpl = IarmImpl).
 * Provide a minimal stub definition here, before including host.hpp.
 * ----------------------------------------------------------------------- */
namespace device {
    class IarmImpl {
    public:
        IarmImpl()  = default;
        virtual ~IarmImpl() = default;
    };
} // namespace device

/* -----------------------------------------------------------------------
 * Real devicesettings headers — class layouts come from here.
 * The stub implements the method bodies; the headers supply declarations.
 * ----------------------------------------------------------------------- */
#include "manager.hpp"
#include "host.hpp"
#include "audioOutputPort.hpp"
#include "audioOutputPortType.hpp"
#include "audioEncoding.hpp"
#include "audioStereoMode.hpp"
#include "audioCompression.hpp"
#include "videoOutputPort.hpp"
#include "videoOutputPortType.hpp"
#include "videoDevice.hpp"
#include "pixelResolution.hpp"
#include "aspectRatio.hpp"
#include "frameRate.hpp"
#include "frontPanelIndicator.hpp"
#include "hdmiIn.hpp"
#include "list.hpp"
#include "exception.hpp"

#include <string>
#include <vector>
#include <list>
#include <cstdint>
#include <cstdio>

/* Halif types needed for return values */
#include "dsAVDTypes.h"
#include "dsError.h"

#define DS_STUB_LOG(fmt, ...) \
    fprintf(stderr, "[DS-STUB] " fmt "\n", ##__VA_ARGS__)

using namespace device;

/* ======================================================================
 * Singleton storage for stub port objects.
 * Clients call getInstance(0) / getVideoOutputPort("HDMI0") etc.
 * We return one shared stub instance per port type.
 * ====================================================================== */

/* AudioOutputPortType stub instance (dsAUDIOPORT_TYPE_HDMI = 1) */
static AudioOutputPortType& stubAudioPortType()
{
    static AudioOutputPortType s(dsAUDIOPORT_TYPE_HDMI);
    return s;
}

/* AudioOutputPort stub instance */
static AudioOutputPort& stubAudioPort()
{
    /* type=dsAUDIOPORT_TYPE_HDMI(1), index=0, id=0 */
    static AudioOutputPort s(dsAUDIOPORT_TYPE_HDMI, 0, 0);
    return s;
}

/* VideoOutputPortType stub instance (dsVIDEOPORT_TYPE_HDMI = 0) */
static VideoOutputPortType& stubVideoPortType()
{
    static VideoOutputPortType s(dsVIDEOPORT_TYPE_HDMI);
    return s;
}

/* VideoOutputPort stub instance */
static VideoOutputPort& stubVideoPort()
{
    /* type=dsVIDEOPORT_TYPE_HDMI(0), index=0, id=0, aPortId=0, resolution="1080p" */
    static VideoOutputPort s(dsVIDEOPORT_TYPE_HDMI, 0, 0, 0, "1080p");
    return s;
}

/* VideoDevice stub instance */
static VideoDevice& stubVideoDevice()
{
    static VideoDevice s(0);
    return s;
}

/* FrontPanelIndicator stub instance
 * id=0, maxBrightness=100, maxCycleRate=1, levels=10, colorMode=0 */
static FrontPanelIndicator& stubFrontPanel()
{
    static FrontPanelIndicator s(0, 100, 1, 10, 0);
    return s;
}

/* ======================================================================
 * device::Manager
 * ====================================================================== */

int device::Manager::IsInitialized = 0;

void device::Manager::Initialize()
{
    DS_STUB_LOG("Manager::Initialize (stub — no dsMgr)");
    IsInitialized = 1;
}

void device::Manager::DeInitialize()
{
    DS_STUB_LOG("Manager::DeInitialize (stub)");
    IsInitialized = 0;
}

void device::Manager::load() {}

device::Manager::Manager()  {}
device::Manager::~Manager() {}

/* ======================================================================
 * device::Host
 * ====================================================================== */

/* Static constants */
const int Host::kPowerOn      = dsPOWER_ON;
const int Host::kPowerOff     = dsPOWER_OFF;
const int Host::kPowerStandby = dsPOWER_STANDBY;

/* Private constructor/destructor — m_impl left null (unique_ptr default) */
Host::Host()  {}
Host::~Host() {}

Host& Host::getInstance()
{
    static Host s_instance;
    return s_instance;
}

bool Host::setPowerMode(int /*mode*/)           { return true; }
int  Host::getPowerMode()                        { return kPowerOn; }
float Host::getCPUTemperature()                  { return 0.0f; }
intptr_t Host::getAudioPortHandle()              { return 0; }

List<VideoOutputPort> Host::getVideoOutputPorts()
{
    /* Return empty list — clients that iterate will see no ports.
     * Clients that call getVideoOutputPort(name) get the stub instance. */
    return List<VideoOutputPort>();
}

List<AudioOutputPort> Host::getAudioOutputPorts()
{
    return List<AudioOutputPort>();
}

VideoOutputPort& Host::getVideoOutputPort(const std::string& /*name*/)
{
    return stubVideoPort();
}

VideoOutputPort& Host::getVideoOutputPort(int /*id*/)
{
    return stubVideoPort();
}

AudioOutputPort& Host::getAudioOutputPort(const std::string& /*name*/)
{
    return stubAudioPort();
}

AudioOutputPort& Host::getAudioOutputPort(int /*id*/)
{
    return stubAudioPort();
}

/* Event registration — silently accepted */
dsError_t Host::Register(Host::IHdmiInEvents* /*l*/, const std::string& /*n*/)    { return dsERR_NONE; }
dsError_t Host::UnRegister(Host::IHdmiInEvents* /*l*/)                             { return dsERR_NONE; }
dsError_t Host::Register(Host::ICompositeInEvents* /*l*/, const std::string& /*n*/){ return dsERR_NONE; }
dsError_t Host::UnRegister(Host::ICompositeInEvents* /*l*/)                        { return dsERR_NONE; }
dsError_t Host::Register(Host::IDisplayEvents* /*l*/, const std::string& /*n*/)    { return dsERR_NONE; }
dsError_t Host::UnRegister(Host::IDisplayEvents* /*l*/)                            { return dsERR_NONE; }
dsError_t Host::Register(Host::IVideoDeviceEvents* /*l*/, const std::string& /*n*/){ return dsERR_NONE; }
dsError_t Host::UnRegister(Host::IVideoDeviceEvents* /*l*/)                        { return dsERR_NONE; }
dsError_t Host::Register(Host::IVideoOutputPortEvents* /*l*/, const std::string& /*n*/){ return dsERR_NONE; }
dsError_t Host::UnRegister(Host::IVideoOutputPortEvents* /*l*/)                    { return dsERR_NONE; }
dsError_t Host::Register(Host::IAudioOutputPortEvents* /*l*/, const std::string& /*n*/){ return dsERR_NONE; }
dsError_t Host::UnRegister(Host::IAudioOutputPortEvents* /*l*/)                    { return dsERR_NONE; }
dsError_t Host::Register(Host::IDisplayDeviceEvents* /*l*/, const std::string& /*n*/){ return dsERR_NONE; }
dsError_t Host::UnRegister(Host::IDisplayDeviceEvents* /*l*/)                      { return dsERR_NONE; }

/* IHdmiInEvents / ICompositeInEvents / IDisplayEvents default implementations */
void Host::IHdmiInEvents::OnHdmiInEventHotPlug(dsHdmiInPort_t, bool) {}
void Host::IHdmiInEvents::OnHdmiInEventSignalStatus(dsHdmiInPort_t, dsHdmiInSignalStatus_t) {}
void Host::IHdmiInEvents::OnHdmiInEventStatus(dsHdmiInPort_t, bool) {}
void Host::IHdmiInEvents::OnHdmiInVideoModeUpdate(dsHdmiInPort_t, const dsVideoPortResolution_t&) {}
void Host::IHdmiInEvents::OnHdmiInAllmStatus(dsHdmiInPort_t, bool) {}
void Host::IHdmiInEvents::OnHdmiInAVIContentType(dsHdmiInPort_t, dsAviContentType_t) {}
void Host::IHdmiInEvents::OnHdmiInVRRStatus(dsHdmiInPort_t, dsVRRType_t) {}
void Host::IHdmiInEvents::OnHdmiInAVLatency(int, int) {}

void Host::ICompositeInEvents::OnCompositeInHotPlug(dsCompositeInPort_t, bool) {}
void Host::ICompositeInEvents::OnCompositeInSignalStatus(dsCompositeInPort_t, dsCompInSignalStatus_t) {}
void Host::ICompositeInEvents::OnCompositeInStatus(dsCompositeInPort_t, bool) {}
void Host::ICompositeInEvents::OnCompositeInVideoModeUpdate(dsCompositeInPort_t, dsVideoPortResolution_t) {}

void Host::IDisplayEvents::OnDisplayRxSense(dsDisplayEvent_t) {}

/* ======================================================================
 * device::AudioOutputPortType
 * ====================================================================== */

AudioOutputPortType::AudioOutputPortType(int id) : _id(id) {}
AudioOutputPortType::~AudioOutputPortType() {}

AudioOutputPortType& AudioOutputPortType::getInstance(int /*id*/)
{
    return stubAudioPortType();
}

AudioOutputPortType& AudioOutputPortType::getInstance(const std::string& /*name*/)
{
    return stubAudioPortType();
}

int  AudioOutputPortType::getId()   const { return _id; }
bool AudioOutputPortType::isConnected() const { return false; }

/* ======================================================================
 * device::AudioOutputPort
 * ====================================================================== */

AudioOutputPort::AudioOutputPort(const int type, const int index, const int id)
    : _type(type), _index(index), _id(id), _handle(0),
      _name("HDMI0"),
      _encoding(dsAUDIO_ENC_PCM), _stereoMode(dsAUDIO_STEREO_STEREO),
      _stereoAuto(false), _gain(0.0f), _db(0.0f),
      _maxDb(0.0f), _minDb(-90.0f), _optimalLevel(0.0f), _level(50.0f),
      _loopThru(false), _muted(false)
{}

AudioOutputPort::~AudioOutputPort() {}

AudioOutputPort& AudioOutputPort::getInstance(int /*id*/)
{
    return stubAudioPort();
}

AudioOutputPort& AudioOutputPort::getInstance(const std::string& /*name*/)
{
    return stubAudioPort();
}

const AudioOutputPortType& AudioOutputPort::getType()  const { return stubAudioPortType(); }
int         AudioOutputPort::getId()                   const { return _id; }
int         AudioOutputPort::getIndex()                const { return _index; }
intptr_t    AudioOutputPort::getOutputPortHandle()     const { return _handle; }
const std::string& AudioOutputPort::getName()          const { return _name; }

bool  AudioOutputPort::isEnabled()            const { return true; }
bool  AudioOutputPort::isConnected()          const { return false; }
bool  AudioOutputPort::isMuted()              const { return false; }
bool  AudioOutputPort::isLoopThru()           const { return false; }
bool  AudioOutputPort::isAudioMSDecode()      const { return false; }
bool  AudioOutputPort::isAudioMS12Decode()    const { return false; }
bool  AudioOutputPort::getStereoAuto()              { return false; }
bool  AudioOutputPort::getDolbyVolumeMode()   const { return false; }
bool  AudioOutputPort::isSurroundDecoderEnabled() const { return false; }
bool  AudioOutputPort::getMISteering()        const { return false; }
bool  AudioOutputPort::getEnablePersist()     const { return false; }
float AudioOutputPort::getGain()              const { return 0.0f; }
float AudioOutputPort::getDB()                const { return 0.0f; }
float AudioOutputPort::getMaxDB()             const { return 0.0f; }
float AudioOutputPort::getMinDB()             const { return -90.0f; }
float AudioOutputPort::getOptimalLevel()      const { return 0.0f; }
float AudioOutputPort::getLevel()             const { return 50.0f; }
int   AudioOutputPort::getCompression()       const { return 0; }
int   AudioOutputPort::getDialogEnhancement() const { return 0; }
int   AudioOutputPort::getIntelligentEqualizerMode() const { return 0; }
int   AudioOutputPort::getBassEnhancer()      const { return 0; }
int   AudioOutputPort::getDRCMode()           const { return 0; }
int   AudioOutputPort::getGraphicEqualizerMode() const { return 0; }

const AudioEncoding& AudioOutputPort::getEncoding() const
{
    return AudioEncoding::getInstance(dsAUDIO_ENC_PCM);
}

dsVolumeLeveller_t AudioOutputPort::getVolumeLeveller() const
{
    dsVolumeLeveller_t v = {};
    return v;
}

dsSurroundVirtualizer_t AudioOutputPort::getSurroundVirtualizer() const
{
    dsSurroundVirtualizer_t s = {};
    return s;
}

const std::string AudioOutputPort::getMS12AudioProfile() const       { return std::string(); }
std::vector<std::string> AudioOutputPort::getMS12AudioProfileList() const { return {}; }

bool AudioOutputPort::getAudioDelay(uint32_t& ms) const        { ms = 0; return true; }
bool AudioOutputPort::getAudioDelayOffset(uint32_t& ms) const  { ms = 0; return true; }

/* Setters — silently accepted */
void  AudioOutputPort::enable()                                  {}
void  AudioOutputPort::disable()                                 {}
void  AudioOutputPort::setEnablePersist(bool)                    {}
dsError_t AudioOutputPort::setEnablePort(bool)                   { return dsERR_NONE; }
void  AudioOutputPort::setEncoding(const std::string&)           {}
void  AudioOutputPort::setEncoding(int)                          {}
void  AudioOutputPort::setStereoMode(const std::string&, bool)   {}
void  AudioOutputPort::setStereoMode(int, bool)                  {}
void  AudioOutputPort::setStereoAuto(bool, bool)                 {}
void  AudioOutputPort::setMuted(bool)                            {}
void  AudioOutputPort::setLoopThru(bool)                         {}
void  AudioOutputPort::setLevel(float)                           {}
void  AudioOutputPort::setDB(float)                              {}
void  AudioOutputPort::setCompression(int)                       {}
void  AudioOutputPort::setDialogEnhancement(int)                 {}
void  AudioOutputPort::setDRCMode(int)                           {}
void  AudioOutputPort::setMISteering(bool)                       {}
void  AudioOutputPort::setBassEnhancer(int)                      {}
void  AudioOutputPort::resetBassEnhancer()                       {}
void  AudioOutputPort::setVolumeLeveller(dsVolumeLeveller_t)     {}
void  AudioOutputPort::setFaderControl(int)                      {}
int   AudioOutputPort::getFaderControl(int* v) const             { if (v) *v = 0; return 0; }
void  AudioOutputPort::setAudioDelay(uint32_t)                   {}
void  AudioOutputPort::enableLEConfig(bool)                      {}
void  AudioOutputPort::enableMS12Config(dsMS12FEATURE_t, bool)   {}
void  AudioOutputPort::setAudioDucking(dsAudioDuckingAction_t, dsAudioDuckingType_t, uint8_t) {}
void  AudioOutputPort::setPrimaryLanguage(const std::string&)    {}
void  AudioOutputPort::setSecondaryLanguage(const std::string&)  {}
bool  AudioOutputPort::GetLEConfig()                              { return false; }
void  AudioOutputPort::getPrimaryLanguage(std::string& s) const   { s.clear(); }
int   AudioOutputPort::getHdmiArcPortId(int* id) const            { if (id) *id = 0; return 0; }
dsError_t AudioOutputPort::reInitializeAudioOutputPort()          { return dsERR_NONE; }

List<AudioEncoding>    AudioOutputPort::getSupportedEncodings()   const { return List<AudioEncoding>(); }
List<AudioCompression> AudioOutputPort::getSupportedCompressions()const { return List<AudioCompression>(); }
List<AudioStereoMode>  AudioOutputPort::getSupportedStereoModes() const { return List<AudioStereoMode>(); }

/* ======================================================================
 * device::AudioEncoding
 * ====================================================================== */

AudioEncoding::AudioEncoding(int id) : _id(id) {}
AudioEncoding::~AudioEncoding() {}

const AudioEncoding& AudioEncoding::getInstance(int id)
{
    static AudioEncoding s_none(dsAUDIO_ENC_NONE);
    static AudioEncoding s_pcm(dsAUDIO_ENC_PCM);
    static AudioEncoding s_ac3(dsAUDIO_ENC_AC3);
    static AudioEncoding s_eac3(dsAUDIO_ENC_EAC3);
    switch (id) {
        case dsAUDIO_ENC_NONE:    return s_none;
        case dsAUDIO_ENC_PCM:     return s_pcm;
        case dsAUDIO_ENC_AC3:     return s_ac3;
        case dsAUDIO_ENC_EAC3:    return s_eac3;
        default:                   return s_pcm;
    }
}

const AudioEncoding& AudioEncoding::getInstance(const std::string& /*name*/)
{
    return AudioEncoding::getInstance(dsAUDIO_ENC_PCM);
}

int AudioEncoding::getId() const { return _id; }

/* ======================================================================
 * device::AudioStereoMode
 * ====================================================================== */

AudioStereoMode::AudioStereoMode(int id) : _id(id) {}
AudioStereoMode::~AudioStereoMode() {}

const AudioStereoMode& AudioStereoMode::getInstance(int id)
{
    static AudioStereoMode s(dsAUDIO_STEREO_STEREO);
    (void)id;
    return s;
}

const AudioStereoMode& AudioStereoMode::getInstance(const std::string& /*name*/)
{
    return AudioStereoMode::getInstance(dsAUDIO_STEREO_STEREO);
}

int AudioStereoMode::getId() const { return _id; }

/* ======================================================================
 * device::AudioCompression
 * ====================================================================== */

AudioCompression::AudioCompression(int id) : _id(id) {}
AudioCompression::~AudioCompression() {}

const AudioCompression& AudioCompression::getInstance(int id)
{
    static AudioCompression s(0);
    (void)id;
    return s;
}

const AudioCompression& AudioCompression::getInstance(const std::string& /*name*/)
{
    return AudioCompression::getInstance(0);
}

int AudioCompression::getId() const { return _id; }

/* ======================================================================
 * device::VideoOutputPortType
 * ====================================================================== */

VideoOutputPortType::VideoOutputPortType(const int id) : _id(id) {}
VideoOutputPortType::~VideoOutputPortType() {}

VideoOutputPortType& VideoOutputPortType::getInstance(const int /*id*/)
{
    return stubVideoPortType();
}

VideoOutputPortType& VideoOutputPortType::getInstance(const std::string& /*name*/)
{
    return stubVideoPortType();
}

int  VideoOutputPortType::getId()         const { return _id; }
bool VideoOutputPortType::isHDCPSupported() const { return false; }
bool VideoOutputPortType::isDTCPSupported() const { return false; }

/* ======================================================================
 * device::VideoOutputPort  (and inner Display class)
 * ====================================================================== */

VideoOutputPort::VideoOutputPort(const int type, const int index, const int id,
                                  int audioPortId, const std::string& resolution)
    : _type(type), _index(index), _id(id), _handle(0),
      _name("HDMI0"), _enabled(true), _contentProtected(false),
      _displayConnected(false), _aPortId(audioPortId),
      _defaultResolution(resolution), _resolution(resolution)
{}

VideoOutputPort::~VideoOutputPort() {}

VideoOutputPort& VideoOutputPort::getInstance(int /*id*/)
{
    return stubVideoPort();
}

VideoOutputPort& VideoOutputPort::getInstance(const std::string& /*name*/)
{
    return stubVideoPort();
}

const VideoOutputPortType& VideoOutputPort::getType() const { return stubVideoPortType(); }
int  VideoOutputPort::getId()    const { return _id; }
int  VideoOutputPort::getIndex() const { return _index; }
const std::string& VideoOutputPort::getName() const { return _name; }

bool VideoOutputPort::isDisplayConnected()  const { return false; }
bool VideoOutputPort::isContentProtected()  const { return false; }
bool VideoOutputPort::isEnabled()           const { return true; }
bool VideoOutputPort::isActive()            const { return false; }
bool VideoOutputPort::isDynamicResolutionSupported() const { return false; }

void VideoOutputPort::enable()  {}
void VideoOutputPort::disable() {}
void VideoOutputPort::setResolution(const std::string& res, bool, bool) { _resolution = res; }
void VideoOutputPort::setDisplayConnected(const bool connected) { _displayConnected = connected; }
void VideoOutputPort::setAudioPort(int id) { _aPortId = id; }
void VideoOutputPort::setAllmEnabled(bool) const {}

int VideoOutputPort::getHDCPStatus()            { return dsHDCP_STATUS_UNAUTHENTICATED; }
int VideoOutputPort::getHDCPProtocol()          { return dsHDCP_VERSION_1X; }
int VideoOutputPort::getHDCPReceiverProtocol()  { return dsHDCP_VERSION_1X; }
int VideoOutputPort::getHDCPCurrentProtocol()   { return dsHDCP_VERSION_1X; }

int  VideoOutputPort::getPixelResolution()      { return dsVIDEO_PIXELRES_1920x1080; }
bool VideoOutputPort::IsOutputHDR()             { return false; }
void VideoOutputPort::ResetOutputToSDR()        {}
bool VideoOutputPort::setForceHDRMode(dsHDRStandard_t) { return false; }
int  VideoOutputPort::forceDisable4KSupport(bool)       { return 0; }
void VideoOutputPort::getTVHDRCapabilities(int* cap) const { if (cap) *cap = 0; }
void VideoOutputPort::getSupportedTvResolutions(int* res) const { if (res) *res = 0; }
void VideoOutputPort::SetHdmiPreference(dsHdmiInPreference_t) {}
void VideoOutputPort::GetHdmiPreference(dsHdmiInPreference_t* pref) { if (pref) *pref = {}; }

bool VideoOutputPort::isInterlaced() const { return false; }
void VideoOutputPort::getSupportedResolutions(std::list<std::string>& res) const { res.clear(); }

const FrameRate& VideoOutputPort::getFrameRate()
{
    return FrameRate::getInstance(dsVIDEO_FRAMERATE_60);
}

/* ---- VideoOutputPort::Display (inner class) ---- */

VideoOutputPort::Display::Display()
    : _handle(0), _productCode(0), _serialNumber(0),
      _manufacturerYear(0), _manufacturerWeek(0), _aspectRatio(0),
      _hdmiDeviceType(true), _isSurroundCapable(false), _isDeviceRepeater(false)
{}

VideoOutputPort::Display::Display(VideoOutputPort& /*vPort*/)
    : _handle(0), _productCode(0), _serialNumber(0),
      _manufacturerYear(0), _manufacturerWeek(0), _aspectRatio(0),
      _hdmiDeviceType(true), _isSurroundCapable(false), _isDeviceRepeater(false)
{}

VideoOutputPort::Display::~Display() {}

void VideoOutputPort::Display::getEDIDBytes(std::vector<uint8_t>& edid) const { edid.clear(); }
void VideoOutputPort::Display::setAllmEnabled(bool) const {}
void VideoOutputPort::Display::setAVIContentType(dsAviContentType_t) const {}
void VideoOutputPort::Display::setAVIScanInformation(dsAVIScanInformation_t) const {}
bool VideoOutputPort::Display::hasSurround() const { return false; }
int  VideoOutputPort::Display::getSurroundMode() const { return 0; }
void VideoOutputPort::Display::getPhysicallAddress(uint8_t& a, uint8_t& b, uint8_t& c, uint8_t& d) const
{ a = 1; b = 0; c = 0; d = 0; }

/* ======================================================================
 * device::AspectRatio
 * ====================================================================== */

AspectRatio::AspectRatio(int id) : _id(id) {}
AspectRatio::~AspectRatio() {}

const AspectRatio& AspectRatio::getInstance(int id)
{
    static AspectRatio s_16x9(dsVIDEO_ASPECT_RATIO_16x9);
    (void)id;
    return s_16x9;
}

const AspectRatio& AspectRatio::getInstance(const std::string& /*name*/)
{
    return AspectRatio::getInstance(dsVIDEO_ASPECT_RATIO_16x9);
}

int AspectRatio::getId() const { return _id; }

/* ======================================================================
 * device::FrameRate
 * ====================================================================== */

FrameRate::FrameRate(int id) : _id(id) {}
FrameRate::~FrameRate() {}

const FrameRate& FrameRate::getInstance(int id)
{
    static FrameRate s_60(dsVIDEO_FRAMERATE_60);
    (void)id;
    return s_60;
}

const FrameRate& FrameRate::getInstance(const std::string& /*name*/)
{
    return FrameRate::getInstance(dsVIDEO_FRAMERATE_60);
}

int FrameRate::getId() const { return _id; }

/* ======================================================================
 * device::PixelResolution
 * ====================================================================== */

PixelResolution::PixelResolution(int id) : _id(id) {}
PixelResolution::~PixelResolution() {}

const PixelResolution& PixelResolution::getInstance(int id)
{
    static PixelResolution s_1080(dsVIDEO_PIXELRES_1920x1080);
    (void)id;
    return s_1080;
}

const PixelResolution& PixelResolution::getInstance(const std::string& /*name*/)
{
    return PixelResolution::getInstance(dsVIDEO_PIXELRES_1920x1080);
}

int PixelResolution::getId() const { return _id; }

/* ======================================================================
 * device::VideoDevice
 * ====================================================================== */

VideoDevice::VideoDevice(int id) : _id(id) {}
VideoDevice::~VideoDevice() {}

VideoDevice& VideoDevice::getInstance(int /*id*/)
{
    return stubVideoDevice();
}

VideoDevice& VideoDevice::getInstance(const std::string& /*name*/)
{
    return stubVideoDevice();
}

void VideoDevice::setDFC(const VideoDFC&)       {}
void VideoDevice::setDFC(const std::string&)    {}
void VideoDevice::setDFC(int)                   {}
void VideoDevice::setDFC()                      {}
void VideoDevice::setPlatformDFC()              {}
void VideoDevice::addDFC(const VideoDFC&)       {}

const VideoDFC& VideoDevice::getDFC()
{
    static VideoDFC s(0);
    return s;
}

List<VideoDFC> VideoDevice::getSupportedDFCs() const { return List<VideoDFC>(); }

void VideoDevice::getHDRCapabilities(int* cap)   { if (cap) *cap = 0; }
unsigned int VideoDevice::getSupportedVideoCodingFormats() const { return 0; }
int  VideoDevice::forceDisableHDRSupport(bool)   { return 0; }
int  VideoDevice::getFRFMode(int* m) const       { if (m) *m = 0; return 0; }
int  VideoDevice::setFRFMode(int) const          { return 0; }
int  VideoDevice::getCurrentDisframerate(char* fr) const { if (fr) fr[0] = '\0'; return 0; }
int  VideoDevice::setDisplayframerate(const char*) const { return 0; }

void VideoDevice::getSettopSupportedResolutions(std::list<std::string>& res)
{
    res.clear();
}

dsVideoCodecInfo_t VideoDevice::getVideoCodecInfo(dsVideoCodingFormat_t /*fmt*/) const
{
    dsVideoCodecInfo_t info = {};
    return info;
}

/* ======================================================================
 * device::VideoDFC
 * ====================================================================== */

VideoDFC::VideoDFC(int id) : _id(id) {}
VideoDFC::~VideoDFC() {}

const VideoDFC& VideoDFC::getInstance(int id)
{
    static VideoDFC s(0);
    (void)id;
    return s;
}

const VideoDFC& VideoDFC::getInstance(const std::string& /*name*/)
{
    return VideoDFC::getInstance(0);
}

int VideoDFC::getId() const { return _id; }

/* ======================================================================
 * device::FrontPanelIndicator
 * ====================================================================== */

FrontPanelIndicator::FrontPanelIndicator(int id, int maxBrightness, int maxCycleRate,
                                          int levels, int colorMode)
    : _id(id), _maxBrightness(maxBrightness), _maxCycleRate(maxCycleRate),
      _brightness(50), _levels(levels), _colorMode(colorMode),
      _state(1), _blink(), _color(0)
{}

FrontPanelIndicator::~FrontPanelIndicator() {}

FrontPanelIndicator& FrontPanelIndicator::getInstance(int /*id*/)
{
    return stubFrontPanel();
}

FrontPanelIndicator& FrontPanelIndicator::getInstance(const std::string& /*name*/)
{
    return stubFrontPanel();
}

void FrontPanelIndicator::setBrightness(const int& /*brightness*/, const bool /*persist*/) {}
void FrontPanelIndicator::setColor(const FrontPanelIndicator::Color& /*color*/, const bool /*persist*/) {}
void FrontPanelIndicator::setColor(const uint32_t /*color*/, const bool /*persist*/) {}
void FrontPanelIndicator::setState(const bool& /*enable*/) {}
void FrontPanelIndicator::setBlink(const FrontPanelIndicator::Blink& /*blink*/) {}

int  FrontPanelIndicator::getBrightness(const bool /*persist*/) { return _brightness; }
void FrontPanelIndicator::getBrightnessLevels(int& levels, int& min, int& max)
    { levels = _levels; min = 0; max = _maxBrightness; }
int  FrontPanelIndicator::getColorMode()  { return _colorMode; }
uint32_t FrontPanelIndicator::getColor()  { return _color; }
bool FrontPanelIndicator::getState()      { return (_state != 0); }

List<FrontPanelIndicator::Color> FrontPanelIndicator::getSupportedColors() const
{
    return List<FrontPanelIndicator::Color>();
}

/* FrontPanelIndicator::Color inner class */
FrontPanelIndicator::Color::Color(int id) : _id(id) {}
FrontPanelIndicator::Color::~Color() {}

const FrontPanelIndicator::Color& FrontPanelIndicator::Color::getInstance(int id)
{
    static FrontPanelIndicator::Color s(0);
    (void)id;
    return s;
}

const FrontPanelIndicator::Color& FrontPanelIndicator::Color::getInstance(const std::string& /*name*/)
{
    return FrontPanelIndicator::Color::getInstance(0);
}

/* ======================================================================
 * device::HdmiInput
 * ====================================================================== */

HdmiInput& HdmiInput::getInstance()
{
    static HdmiInput s_instance;
    return s_instance;
}

HdmiInput::HdmiInput()  {}
HdmiInput::~HdmiInput() {}

uint8_t HdmiInput::getNumberOfInputs()        const { return 0; }
bool    HdmiInput::isPresented()               const { return false; }
bool    HdmiInput::isActivePort(int8_t)        const { return false; }
int8_t  HdmiInput::getActivePort()             const { return -1; }
bool    HdmiInput::isPortConnected(int8_t)     const { return false; }

void HdmiInput::selectPort(int8_t /*port*/, bool /*requestAudioMix*/,
                            int /*videoPlaneType*/, bool /*topMost*/) const {}
void HdmiInput::scaleVideo(int32_t, int32_t, int32_t, int32_t)     const {}
void HdmiInput::selectZoomMode(int8_t)                              const {}
void HdmiInput::pauseAudio()                                        const {}
void HdmiInput::resumeAudio()                                       const {}
std::string HdmiInput::getCurrentVideoMode()                        const { return std::string(); }
void HdmiInput::getCurrentVideoModeObj(dsVideoPortResolution_t& res) { res = {}; }
void HdmiInput::getEDIDBytesInfo(int, std::vector<uint8_t>& d)     const { d.clear(); }
void HdmiInput::getHDMISPDInfo(int, std::vector<uint8_t>& d)            { d.clear(); }
void HdmiInput::setEdidVersion(int, int)                                 {}
void HdmiInput::getEdidVersion(int, int* v)                              { if (v) *v = 0; }
void HdmiInput::getHdmiALLMStatus(int, bool* s)                         { if (s) *s = false; }
void HdmiInput::getSupportedGameFeatures(std::vector<std::string>& f)   { f.clear(); }
void HdmiInput::getAVLatency(int* a, int* v)                            { if (a) *a=0; if (v) *v=0; }
void HdmiInput::setEdid2AllmSupport(int, bool)                          {}
void HdmiInput::getEdid2AllmSupport(int, bool* s)                       { if (s) *s = false; }
void HdmiInput::setVRRSupport(int, bool)                                {}

/* ======================================================================
 * Misc globals required by libds ABI
 * ====================================================================== */

/* Logging stub */
extern "C" void DS_RegisterForLog(void* /*fn*/) {}

/* LoadDLSymbols — called by real devicesettings to dlopen HAL */
bool device::LoadDLSymbols(void*, const device::dlSymbolLookup*, int) { return true; }
void device::loadDeviceCapabilities(unsigned int) {}
