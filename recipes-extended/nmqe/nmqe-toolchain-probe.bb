SUMMARY = "C++20 target compiler and standard-library probe for the MoQ POC"
DESCRIPTION = "Builds and links an independent C++20 executable without Rust or player dependencies"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://nmqe-toolchain-probe.cpp;beginline=1;endline=1;md5=6ec41034e04432ee375d0e14fba596f4"

SRC_URI = "file://CMakeLists.txt file://nmqe-toolchain-probe.cpp"
S = "${WORKDIR}"
B = "${WORKDIR}/build"

inherit cmake

do_compile:append() {
    bbnote "NMQE C++20 toolchain probe compiled and linked successfully for ${TARGET_SYS}"
}
