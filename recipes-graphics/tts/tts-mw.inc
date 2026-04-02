DEPENDS += "rdk-logger wpeframework wpeframework-clientlibraries firebolt-cpp-client"

CXXFLAGS += " -I${STAGING_INCDIR}/firebolt -I${STAGING_INCDIR}/glib-2.0 -I${STAGING_INCDIR}/../usr/lib/glib-2.0/include "
CXXFLAGS += " -std=gnu++17"
EXTRA_OECMAKE:remove = " -std=gnu++11"
CXXFLAGS:remove = " -std=gnu++11"
