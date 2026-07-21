# CMakeLists.txt for devicesettings-stub
# Builds libds.so with no-op implementations — no IARM/dsMgr/HAL dependency at runtime.

cmake_minimum_required(VERSION 3.5)
project(devicesettings-stub VERSION 1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED YES)

# -----------------------------------------------------------------------
# Find devicesettings-hal-headers (provides dsTypes.h, dsAVDTypes.h etc.)
# These are compile-time-only; no libds.so is linked.
# -----------------------------------------------------------------------
find_path(DS_HAL_INCLUDE_DIR NAMES dsTypes.h
    PATHS "${CMAKE_SYSROOT}/usr/include/rdk/halif/ds-hal"
    NO_DEFAULT_PATH)

if(NOT DS_HAL_INCLUDE_DIR)
    message(FATAL_ERROR "devicesettings-stub: dsTypes.h not found — "
        "add devicesettings-hal-headers to DEPENDS in the recipe")
endif()

find_path(DS_INCLUDE_DIR NAMES manager.hpp
    PATHS "${CMAKE_SYSROOT}/usr/include/rdk/ds"
    NO_DEFAULT_PATH)

if(NOT DS_INCLUDE_DIR)
    message(FATAL_ERROR "devicesettings-stub: manager.hpp not found — "
        "add devicesettings to DEPENDS (dev package) in the recipe")
endif()

add_library(ds SHARED
    devicesettings_stub.cpp)

target_include_directories(ds PRIVATE
    ${DS_INCLUDE_DIR}
    ${DS_HAL_INCLUDE_DIR}
    "${CMAKE_SYSROOT}/usr/include/rdk/halif/ds-hal"
    "${CMAKE_SYSROOT}/usr/include/rdk/ds-hal"
    "${CMAKE_SYSROOT}/usr/include/rdk/ds")

target_compile_options(ds PRIVATE
    -fvisibility=default
    -Wno-unused-parameter
    -Wno-return-type
    -Wno-missing-field-initializers)

set_target_properties(ds PROPERTIES
    OUTPUT_NAME "ds"
    SOVERSION   1
    VERSION     1.0.0)

install(TARGETS ds
    LIBRARY DESTINATION ${CMAKE_INSTALL_PREFIX}/lib)
