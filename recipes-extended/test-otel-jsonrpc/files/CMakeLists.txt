cmake_minimum_required(VERSION 3.10)
project(otel-jsonrpc-test VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(WPEFrameworkCore QUIET)
find_package(WPEFrameworkWebSocket QUIET)
find_package(WPEFrameworkCryptalgo QUIET)

find_library(RDK_OTLP_LIB rdk_otlp REQUIRED)

add_executable(${PROJECT_NAME} main.cpp)

# Thunder R4_4-RDK uses "namespace Thunder" in source but compiles with -DThunder=WPEFramework
# to remap the C++ namespace. We must do the same or symbol resolution will fail.
target_compile_definitions(${PROJECT_NAME} PRIVATE Thunder=WPEFramework)

if(TARGET WPEFrameworkCore::WPEFrameworkCore)
    target_link_libraries(${PROJECT_NAME}
        PRIVATE
            WPEFrameworkCore::WPEFrameworkCore
            WPEFrameworkWebSocket::WPEFrameworkWebSocket
            WPEFrameworkCryptalgo::WPEFrameworkCryptalgo
            ${RDK_OTLP_LIB}
            pthread
            rt
    )
else()
    # Manual fallback
    find_library(THUNDER_CORE_LIB NAMES WPEFrameworkCore)
    find_library(THUNDER_WEBSOCKET_LIB NAMES WPEFrameworkWebSocket)
    find_library(THUNDER_CRYPTALGO_LIB NAMES WPEFrameworkCryptalgo)
    target_link_libraries(${PROJECT_NAME}
        PRIVATE
            ${THUNDER_CORE_LIB}
            ${THUNDER_WEBSOCKET_LIB}
            ${THUNDER_CRYPTALGO_LIB}
            ${RDK_OTLP_LIB}
            pthread
            rt
    )
endif()

find_path(WPEFRAMEWORK_INCLUDE_DIR
    NAMES core/core.h
    PATH_SUFFIXES WPEFramework
)
if(WPEFRAMEWORK_INCLUDE_DIR)
    target_include_directories(${PROJECT_NAME} PRIVATE ${WPEFRAMEWORK_INCLUDE_DIR})
endif()

install(TARGETS ${PROJECT_NAME} DESTINATION ${CMAKE_INSTALL_BINDIR})
