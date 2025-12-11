SUMMARY = "RDK Browser Launcher"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=dc6e390ad71aef79d0c2caf3cde03a19"

S = "${WORKDIR}/git/BrowserLauncher"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
SRC_URI = "${CMF_GITHUB_ROOT}/entservices-runtime;${CMF_GITHUB_SRC_URI_SUFFIX}"
SRCREV = "3966f4f7c23ab29f86f5ae8a764a9aac6344d8b9"
PV .= "+${@bb.fetch2.get_srcrev(d).replace('AUTOINC+','')}"

inherit pkgconfig cmake

PACKAGECONFIG[tests] = "-DENABLE_TESTS=ON,-DENABLE_TESTS=OFF,googletest,"
PACKAGECONFIG[testrunner] = "-DENABLE_TESTRUNNER=ON,-DENABLE_TESTRUNNER=OFF,westeros westeros-simpleshell,"

DEPENDS += "glib-2.0 glib-2.0-native wpe-webkit nlohmann-json firebolt-cpp-client"

EXTRA_OECMAKE:append = " -DBROWSER_LAUNCHER_VERSION=${PV}"

# override install prefix
EXTRA_OECMAKE:append = " -DCMAKE_INSTALL_PREFIX:PATH=${libexecdir}/${BPN}"
INSANE_SKIP:${PN} += "installed-vs-shipped"
