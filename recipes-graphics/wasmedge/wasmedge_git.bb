SUMMARY     = "WasmEdge — lightweight WebAssembly runtime"
DESCRIPTION = "WasmEdge is a lightweight, high-performance, extensible \
               WebAssembly runtime for cloud-native, edge, and decentralized \
               applications. This recipe cross-compiles WasmEdge for RDK-E \
               targets (ARM/ARM64) and provides libwasmedge.so for use by \
               rdkNativeScript as an alternative WASM runtime to JSC built-in."
HOMEPAGE    = "https://wasmedge.org"
LICENSE     = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"


# WASMEdge 0.13.5 - latest version compatible with RDK-E dependencies
# Compatible with: fmt 8.1.1, spdlog 1.9.2, LLVM 13.0.1 (no workarounds needed)
DEPENDS = "boost fmt spdlog llvm llvm-native clang"

# Fetch WASMEdge 0.13.5 release tag
SRC_URI = "git://git@github.com/WasmEdge/WasmEdge.git;branch=master"
SRCREV  = "b8eb435507efb98fa8a799257795fcd9f8440975"
PV      = "0.13.5"
PR      = "r0"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

OECMAKE_GENERATOR = "Ninja"

# Build configuration:
#   - TOOLS ON: builds wasmedge CLI + wasmedgec AOT compiler (needed to run/compile .wasm files)
#   - AOT_RUNTIME ON: enables ahead-of-time compilation support
#   - Release build (no size trade-offs, full optimizations)
EXTRA_OECMAKE = " \
    -DWASMEDGE_BUILD_TESTS=OFF        \
    -DWASMEDGE_BUILD_TOOLS=ON         \
    -DWASMEDGE_BUILD_SHARED_LIB=ON    \
    -DWASMEDGE_BUILD_STATIC_LIB=OFF   \
    -DWASMEDGE_BUILD_EXAMPLES=OFF     \
    -DWASMEDGE_USE_LLVM=ON           \
    -DWASMEDGE_FORCE_DISABLE_LTO=ON   \
    -DCMAKE_BUILD_TYPE=Release        \
    -DWASMEDGE_BUILD_WASI_NN=OFF      \
    -DWASMEDGE_BUILD_WASI_CRYPTO=OFF  \
    -DWASMEDGE_BUILD_AOT_RUNTIME=ON   \
    -DLLVM_CONFIG=${STAGING_BINDIR_NATIVE}/llvm-config \
"

do_install() {
    install -d ${D}${libdir}
    install -d ${D}${includedir}/wasmedge
    install -d ${D}${bindir}

    # Install shared library with symlinks using Yocto helper
    # oe_soinstall automatically creates: libwasmedge.so.0.0.3 → libwasmedge.so.0 → libwasmedge.so
    oe_soinstall ${B}/lib/api/libwasmedge.so.0.0.3 ${D}${libdir}

    # Install wasmedge CLI and wasmedgec AOT compiler from tools/wasmedge/
    install ${B}/tools/wasmedge/wasmedge  ${D}${bindir}/wasmedge
    install ${B}/tools/wasmedge/wasmedgec ${D}${bindir}/wasmedgec

    # Install all headers from include/api/wasmedge/
    install ${B}/include/api/wasmedge/*.h ${D}${includedir}/wasmedge/
    install ${B}/include/api/wasmedge/*.inc ${D}${includedir}/wasmedge/ || true

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


FILES:${PN} += "${libdir}/libwasmedge.so.0.0.3"
FILES:${PN} += "${libdir}/libwasmedge.so.0"
FILES:${PN} += "${libdir}/libwasmedge.so"
FILES:${PN} += "${bindir}/wasmedge"
FILES:${PN} += "${bindir}/wasmedgec"

# Development package (headers, symlinks, pkg-config)
FILES:${PN}-dev   += "${includedir}/wasmedge/" 

# Prevent stripping of shared libraries during packaging
SOLIBS             = ".so.*"
FILES_SOLIBSDEV    = ""
INSANE_SKIP:${PN} += "dev-so"
INSANE_SKIP:${PN} += "file-rdeps"

# Do not warn about library being stripped
INSANE_SKIP:${PN} += "already-stripped"

