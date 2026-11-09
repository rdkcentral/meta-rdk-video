SUMMARY = "ENTServices webremote plugin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

PV = "1.0.0"
PR = "r0"

S = "${WORKDIR}/git"
inherit cmake pkgconfig

SRC_URI = "${CMF_GITHUB_ROOT}/entservices-webremote;${CMF_GITHUB_SRC_URI_SUFFIX}"

# Release version - 1.0.0
SRCREV = "3489e47d03a92fcc422b28a67d084b418f5dc31c"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"

TOOLCHAIN = "gcc"
DISTRO_FEATURES_CHECK = "wpe_r4_4 wpe_r4"
EXTRA_OECMAKE += "${@bb.utils.contains_any('DISTRO_FEATURES', '${DISTRO_FEATURES_CHECK}', ' -DUSE_THUNDER_R4=ON', '', d)}"

DEPENDS += "wpeframework wpeframework-tools-native"
RDEPENDS:${PN} += "wpeframework"

CXXFLAGS += " -Wall -Werror "
CXXFLAGS:remove_morty = " -Wall -Werror "
SELECTED_OPTIMIZATION:append = " -Wno-deprecated-declarations"

# Device id used to build the X<device_id> Linchpin topic (plugin config "deviceid")
PLUGIN_WEBREMOTE_DEVICEID ?= "device_id"

# ----------------------------------------------------------------------------

PACKAGECONFIG ?= " breakpadsupport \
    webremote \
"
PACKAGECONFIG[breakpadsupport] = ",,breakpad-wrapper,breakpad-wrapper"
PACKAGECONFIG[webremote]       = "-DPLUGIN_WEBREMOTE=ON,-DPLUGIN_WEBREMOTE=OFF,entservices-apis entservices-cpc-apis entservices-helpers,entservices-apis entservices-cpc-apis entservices-helpers"

# ----------------------------------------------------------------------------

EXTRA_OECMAKE += " \
    -DBUILD_REFERENCE=${SRCREV} \
    -DBUILD_SHARED_LIBS=ON \
    -DPLUGIN_WEBREMOTE_DEVICEID=${PLUGIN_WEBREMOTE_DEVICEID} \
"

do_install:append() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'thunder_startup_services', 'true', 'false', d)} == 'true'; then
        if [ -d "${D}/etc/WPEFramework/plugins" ]; then
            find ${D}/etc/WPEFramework/plugins/ -type f ! -name "PowerManager.json" | xargs sed -i -r 's/"autostart"[[:space:]]*:[[:space:]]*true/"autostart":false/g'
        fi
    fi
}

# ----------------------------------------------------------------------------

FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/wpeframework/plugins/*.so ${libdir}/*.so ${datadir}/WPEFramework/*"

INSANE_SKIP:${PN} += "libdir staticdev dev-so"
INSANE_SKIP:${PN}-dbg += "libdir"
