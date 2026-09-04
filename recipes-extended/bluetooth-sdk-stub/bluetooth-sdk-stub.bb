SUMMARY = "RDK Bluetooth SDK stub (librdk_bluetooth) used for bring-up of the ENTServices Bluetooth plugin"
SECTION = "console/utils"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=be469927b9722d71bc41ecd5e71fe35f"

PV = "1.0.0"
PR = "r0"

# Vendor layers may ship a real SDK recipe that PROVIDES the same virtual.
PROVIDES += "virtual/vendor-bluetooth-sdk"
RPROVIDES:${PN} = "virtual/vendor-bluetooth-sdk"

# Keep in sync with entservices-connectivity.bb - the stub lives in that repo.
SRCREV = "d9ba34d3d495e918974aa135d1180494b1ced941"
SRC_URI = "${CMF_GITHUB_ROOT}/entservices-connectivity;${CMF_GITHUB_SRC_URI_SUFFIX}"

S = "${WORKDIR}/git"
STUB_SRC = "${S}/Bluetooth/bluetooth-sdk-stub"

inherit cmake

OECMAKE_SOURCEPATH = "${STUB_SRC}"

PACKAGE_ARCH = "${MIDDLEWARE_ARCH}"
TOOLCHAIN = "gcc"

PACKAGECONFIG ??= ""
PACKAGECONFIG[audio] = "-DAUDIO_SUPPORT=ON,-DAUDIO_SUPPORT=OFF,,"

# FindBLUETOOTH_SDK.cmake expects the real SDK layout:
#   ${libdir}/bluetoothsdk/librdk_bluetooth.so and ${includedir}/bluetoothsdk/**
do_install() {
    install -d ${D}${libdir}/bluetoothsdk
    install -m 0755 ${B}/librdk_bluetooth.so.1.0.0 ${D}${libdir}/bluetoothsdk/librdk_bluetooth.so.1.0.0
    ln -sf librdk_bluetooth.so.1.0.0 ${D}${libdir}/bluetoothsdk/librdk_bluetooth.so.1
    ln -sf librdk_bluetooth.so.1.0.0 ${D}${libdir}/bluetoothsdk/librdk_bluetooth.so

    install -d ${D}${includedir}/bluetoothsdk/bluetooth
    install -m 0644 ${STUB_SRC}/include/*.h ${D}${includedir}/bluetoothsdk/
    install -m 0644 ${STUB_SRC}/include/bluetooth/*.h ${D}${includedir}/bluetoothsdk/bluetooth/

    # The SONAME is resolved from a non-standard libdir at runtime.
    install -d ${D}${sysconfdir}/ld.so.conf.d
    echo "${libdir}/bluetoothsdk" > ${D}${sysconfdir}/ld.so.conf.d/bluetoothsdk.conf
}

FILES_SOLIBSDEV = ""
FILES:${PN} = "${libdir}/bluetoothsdk/librdk_bluetooth.so.* ${sysconfdir}/ld.so.conf.d/bluetoothsdk.conf"
FILES:${PN}-dev = "${includedir}/bluetoothsdk ${libdir}/bluetoothsdk/librdk_bluetooth.so"
