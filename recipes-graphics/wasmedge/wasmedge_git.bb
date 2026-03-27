SUMMARY     = "WasmEdge — lightweight WebAssembly runtime"
DESCRIPTION = "WasmEdge is a lightweight, high-performance, extensible \
               WebAssembly runtime for cloud-native, edge, and decentralized \
               applications. This recipe cross-compiles WasmEdge for RDK-E \
               targets (ARM/ARM64) and provides libwasmedge.so for use by \
               rdkNativeScript as an alternative WASM runtime to JSC built-in."
HOMEPAGE    = "https://wasmedge.org"
LICENSE     = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

# WASMEdge 0.13.5 - latest version compatible with RDK-E dependencies
# Compatible with: fmt 8.1.1, spdlog 1.9.2, LLVM 13.0.1
#
# clang provides: LLVMConfig.cmake, AddLLVM.cmake, LLDConfig.cmake,
#   liblldELF.a, liblldCommon.a, lld/Common/Driver.h — all required for AOT.
# llvm_git.bb alone cannot satisfy AOT because it deletes cmake files and
# does not build LLD. clang_git.bb builds lld via LLVM_ENABLE_PROJECTS.
DEPENDS = "boost fmt spdlog clang clang-native"

# Fetch WASMEdge 0.13.5 release tag
SRC_URI = "git://git@github.com/WasmEdge/WasmEdge.git;branch=master"
SRCREV  = "b8eb435507efb98fa8a799257795fcd9f8440975"
PV      = "0.13.5"
PR      = "r0"

# wasmedge-quickjs: QuickJS JavaScript engine compiled to WASM.
# Enables running JS scripts inside WasmEdge:
#   wasmedge --dir .:. wasmedge_quickjs.wasm your_script.js
# Source: github.com/second-state/wasmedge-quickjs (compatible with WasmEdge 0.13.5)
SRC_URI += "https://github.com/second-state/wasmedge-quickjs/releases/download/v0.5.0-alpha/wasmedge_quickjs.wasm;subdir=qjs;name=qjswasm"
SRC_URI[qjswasm.sha256sum] = "b8451261a244b7bc62ae95acb43882044aed2f3d5f08355889252b418ec89231"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

OECMAKE_GENERATOR = "Ninja"

# Build configuration for minimum libwasmedge.so size:
#
# Size reduction techniques applied:
#   1. LTO (Link Time Optimization): eliminates dead code across all translation units
#      at link time. Single biggest size win (~20-40%). Requires FORCE_DISABLE_LTO=OFF.
#   2. -Os: Optimize for size instead of speed (-O2). Smaller code, similar performance.
#   3. -g0: No debug info emitted in object files. Avoids debug bloat in .so.
#   4. -ffunction-sections -fdata-sections: Place each function/variable in own section.
#   5. -Wl,--gc-sections: Linker garbage-collects unreferenced sections from step 4.
#   6. -fvisibility=hidden: Only explicitly exported symbols visible externally.
#      WasmEdge already sets this in wasmedge_setup_target() but enforcing at recipe level.
#   7. All optional features/plugins OFF: no WASI-NN, WASI-crypto, image, tensorflow, etc.
#   8. Yocto strip (do_package): runs 'strip --strip-unneeded' on installed .so at package time.
#      INHIBIT_PACKAGE_STRIP must NOT be set, and already-stripped skip removed.
#
EXTRA_OECMAKE = " \
    -DWASMEDGE_BUILD_TESTS=OFF                                          \
    -DWASMEDGE_BUILD_TOOLS=ON                                           \
    -DWASMEDGE_BUILD_SHARED_LIB=ON                                      \
    -DWASMEDGE_BUILD_STATIC_LIB=OFF                                     \
    -DWASMEDGE_BUILD_EXAMPLES=OFF                                       \
    -DWASMEDGE_BUILD_PLUGINS=ON                                         \
    -DWASMEDGE_PLUGIN_WASI_NN_BACKEND=OFF                               \
    -DWASMEDGE_USE_LLVM=ON                                              \
    -DWASMEDGE_FORCE_DISABLE_LTO=OFF                                    \
    -DCMAKE_BUILD_TYPE=Release                                          \
    -DWASMEDGE_BUILD_WASI_NN=OFF                                        \
    -DWASMEDGE_BUILD_WASI_CRYPTO=OFF                                    \
    -DWASMEDGE_BUILD_AOT_RUNTIME=ON                                     \
    -DCMAKE_CXX_FLAGS_RELEASE='-Os -g0 -DNDEBUG -ffunction-sections -fdata-sections -fvisibility=hidden -fvisibility-inlines-hidden' \
    -DCMAKE_C_FLAGS_RELEASE='-Os -g0 -DNDEBUG -ffunction-sections -fdata-sections -fvisibility=hidden'                               \
    -DCMAKE_EXE_LINKER_FLAGS='-Wl,--hash-style=gnu -Wl,--gc-sections'                       \
    -DCMAKE_SHARED_LINKER_FLAGS='-Wl,--hash-style=gnu -Wl,--gc-sections'                    \
"


do_install() {
    install -d ${D}${libdir}
    install -d ${D}${includedir}/wasmedge
    install -d ${D}${bindir}

    # Install shared library with symlinks using Yocto helper
    # oe_soinstall automatically creates: libwasmedge.so.0.0.3 → libwasmedge.so.0 → libwasmedge.so
    oe_soinstall ${B}/lib/api/libwasmedge.so.0.0.3 ${D}${libdir}

    # Install wasmedge CLI and wasmedgec AOT compiler
    install ${B}/tools/wasmedge/wasmedge  ${D}${bindir}/wasmedge
    install ${B}/tools/wasmedge/wasmedgec ${D}${bindir}/wasmedgec

    # Install all headers from include/api/wasmedge/
    install ${B}/include/api/wasmedge/*.h ${D}${includedir}/wasmedge/
    install ${B}/include/api/wasmedge/*.inc ${D}${includedir}/wasmedge/ || true

    # Install wasmedge-quickjs WASM binary — QuickJS JS engine compiled to WASM.
    # Allows running JavaScript inside WasmEdge:
    #   wasmedge --dir .:. /usr/share/wasmedge/wasmedge_quickjs.wasm script.js
    install -d ${D}/usr/share/wasmedge
    install -m 0644 ${WORKDIR}/qjs/wasmedge_quickjs.wasm ${D}/usr/share/wasmedge/

    # Create pkg-config file
    #install -d ${D}${libdir}/pkgconfig
    #cat > ${D}${libdir}/pkgconfig/wasmedge.pc << 'EOF'
#prefix=${exec_prefix}/..
#exec_prefix=${prefix}
#libdir=${exec_prefix}/lib
#includedir=${exec_prefix}/include
#
#Name: WasmEdge
#Description: WebAssembly Runtime Environment
#Version: ${PV}
#Libs: -L${libdir} -lwasmedge
#Cflags: -I${includedir}
#EOF
}

# Declare runtime dependency: wasmedge CLI requires libwasmedge.so.0 (in same package)
RDEPENDS:${PN} = ""

FILES:${PN} += "${libdir}/libwasmedge.so.0.0.3"
FILES:${PN} += "${libdir}/libwasmedge.so.0"
FILES:${PN} += "${libdir}/libwasmedge.so"
FILES:${PN} += "${bindir}/wasmedge"
FILES:${PN} += "${bindir}/wasmedgec"
# wasmedge-quickjs WASM binary — deployed to target for JS-in-WASM execution
FILES:${PN} += "/usr/share/wasmedge/wasmedge_quickjs.wasm"

# Development package (headers, symlinks, pkg-config)
FILES:${PN}-dev   += "${includedir}/wasmedge/"
FILES:${PN}-dev   += "${libdir}/libwasmedge.so"

# Prevent stripping of shared libraries during packaging
SOLIBS             = ".so.*"
FILES_SOLIBSDEV    = ""
INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN} += "file-rdeps"
# Allow Yocto's do_package to run 'strip --strip-unneeded' on libwasmedge.so
# Removing 'already-stripped' so the packager strips debug symbols at package time



