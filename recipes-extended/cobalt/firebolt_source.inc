PACKAGECONFIG ??= "firebolt-prebuilt"

PACKAGECONFIG[firebolt-src]      = "rdk_enable_firebolt_api=true use_system_firebolt_libs=false,rdk_enable_firebolt_api=false,,,,firebolt-prebuilt"
PACKAGECONFIG[firebolt-prebuilt] = "rdk_enable_firebolt_api=true use_system_firebolt_libs=true,rdk_enable_firebolt_api=false,firebolt-cpp-client firebolt-cpp-transport,,,firebolt-src"

FIREBOLT_CPP_CLIENT_SRC_URI    ?= "${CMF_GITHUB_ROOT}/firebolt-cpp-client"
FIREBOLT_CPP_TRANSPORT_SRC_URI ?= "${CMF_GITHUB_ROOT}/firebolt-cpp-transport"

FIREBOLT_CPP_CLIENT_BRANCH    ?= "main"
FIREBOLT_CPP_TRANSPORT_BRANCH ?= "main"

# firebolt-cpp-client v0.5.5
FIREBOLT_CPP_CLIENT_SRCREV    ?= "51cdabf4170e96fed19ebda9589da6236c30d2ce"
# firebolt-cpp-transport v1.1.6
FIREBOLT_CPP_TRANSPORT_SRCREV ?= "b092fab9683b5c9537906102b20389909790da57"

SRCREV_fbclient    = "${FIREBOLT_CPP_CLIENT_SRCREV}"
SRCREV_fbtransport = "${FIREBOLT_CPP_TRANSPORT_SRCREV}"

def firebolt_src_mode(d):
    pkgs = (d.getVar('PACKAGECONFIG') or '').split()
    return 'firebolt-src' in pkgs and 'firebolt-prebuilt' not in pkgs

SRC_URI:append = " \
    ${@'${FIREBOLT_CPP_CLIENT_SRC_URI};protocol=${CMF_GITHUB_PROTOCOL};destsuffix=firebolt-cpp-client;name=fbclient;branch=${FIREBOLT_CPP_CLIENT_BRANCH}' if firebolt_src_mode(d) else ''} \
"

SRC_URI:append = " \
    ${@'${FIREBOLT_CPP_TRANSPORT_SRC_URI};protocol=${CMF_GITHUB_PROTOCOL};destsuffix=firebolt-cpp-transport;name=fbtransport;branch=${FIREBOLT_CPP_TRANSPORT_BRANCH}' if firebolt_src_mode(d) else ''} \
"

DEPENDS:append = " ${@'nlohmann-json websocketpp boost' if firebolt_src_mode(d) else ''}"

do_unpack_firebolt() {
    if ${@'true' if firebolt_src_mode(d) else 'false'}; then
        mkdir -p "${S}/third_party"
        ( cd "${S}/third_party" && ln -snf ../../firebolt-cpp-client firebolt-cpp-client )
        ( cd "${S}/third_party" && ln -snf ../../firebolt-cpp-transport firebolt-cpp-transport )
    fi
}