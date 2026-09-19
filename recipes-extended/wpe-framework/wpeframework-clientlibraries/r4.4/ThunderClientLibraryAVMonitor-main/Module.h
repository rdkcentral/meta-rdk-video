#pragma once

#ifndef MODULE_NAME
#define MODULE_NAME ClientLibrary_AVMonitor
#endif

#include <com/com.h>
#include <core/core.h>
#include <plugins/plugins.h>
#include <tracing/tracing.h>

#if defined(__WINDOWS__) && defined(AVMONITOR_EXPORTS)
#undef EXTERNAL
#define EXTERNAL EXTERNAL_EXPORT
#endif
