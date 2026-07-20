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

# -----------------------------------------------------------------------
# Detect header version using DS_INCLUDE_DIR (already resolved by find_path
# above — avoids the lib32 CMAKE_SYSROOT filesystem-path issue).
#   Modern (xione-uk): VideoOutputPort has setAllmEnabled declared
#   Legacy (sharp-a60): setAllmEnabled absent; Display::getPhysicallAddress
#                       and other methods are already inline in the header
# DS_HEADERS_LEGACY excludes method bodies that would be duplicate/absent.
# -----------------------------------------------------------------------
if(EXISTS "${DS_INCLUDE_DIR}/videoOutputPort.hpp")
    file(READ "${DS_INCLUDE_DIR}/videoOutputPort.hpp" _VOP_CONTENT)
    if(NOT _VOP_CONTENT MATCHES "setAllmEnabled")
        message(STATUS "devicesettings-stub: legacy headers (sharp-a60 style) — DS_HEADERS_LEGACY=1")
        target_compile_definitions(ds PRIVATE DS_HEADERS_LEGACY=1)
    else()
        message(STATUS "devicesettings-stub: modern headers (xione-uk style)")
    endif()
else()
    message(WARNING "devicesettings-stub: videoOutputPort.hpp not found at ${DS_INCLUDE_DIR}, assuming legacy")
    target_compile_definitions(ds PRIVATE DS_HEADERS_LEGACY=1)
endif()

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
