# Common part of recipes building a single apis/<dir> of entservices-apis as a standalone API.
# The recipe sets:
#   STANDALONE_API                - the apis/ subdirectory, e.g. "OpenCDMi"
#   PV, SRCREV_entservices-apis   - optional: the revision this API is pinned to
#                                   (default: same release as entservices-apis.bb)
# and the same directory must be listed in STANDALONE_APIS of entservices-apis.bb.
#
# What gets built and installed: see "Standalone APIs" in entservices-apis CMakeLists.txt.

require entservices-apis-common.inc

# The API headers include common headers (e.g. Ids.h) that only entservices-apis installs
DEPENDS = "wpeframework wpeframework-tools-native entservices-apis"

EXTRA_OECMAKE += " -DSTANDALONE_API=${STANDALONE_API}"

python () {
    if not d.getVar('STANDALONE_API'):
        bb.fatal("%s: STANDALONE_API must be set" % d.getVar('PN'))
}
