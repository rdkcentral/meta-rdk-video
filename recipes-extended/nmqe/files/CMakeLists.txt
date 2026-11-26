cmake_minimum_required(VERSION 3.21)
project(NmqeToolchainProbe LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_VERBOSE_MAKEFILE ON)

find_package(Threads REQUIRED)
message(STATUS "NMQE C++20 probe compiler: ${CMAKE_CXX_COMPILER_ID} ${CMAKE_CXX_COMPILER_VERSION}")

add_executable(nmqe-toolchain-probe nmqe-toolchain-probe.cpp)
target_link_libraries(nmqe-toolchain-probe PRIVATE Threads::Threads)

include(GNUInstallDirs)
install(TARGETS nmqe-toolchain-probe RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR})
