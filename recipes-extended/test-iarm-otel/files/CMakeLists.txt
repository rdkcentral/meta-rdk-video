cmake_minimum_required(VERSION 3.10)
project(test-iarm-otel VERSION 1.0.0 LANGUAGES C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

# ── Locate IARM headers ────────────────────────────────────────────────────
# libIBus.h may be installed at different subdirectory depths depending on
# the iarmbus recipe (e.g. <sysroot>/usr/include/libIBus.h  or
# <sysroot>/usr/include/rdk/iarmbus/libIBus.h).  Use find_path so CMake
# searches the sysroot automatically.
find_path(IARM_INCLUDE_DIR
    NAMES libIBus.h
    PATH_SUFFIXES rdk/iarmbus iarmbus rdk
    REQUIRED
)
message(STATUS "IARM headers found at: ${IARM_INCLUDE_DIR}")

# ── Locate OTel instrumentation header ────────────────────────────────────
find_path(OTEL_INSTRUMENTATION_DIR
    NAMES rdk_otlp_instrumentation.h
    PATH_SUFFIXES rdk_otlp rdk/otlp otlp rdk
    REQUIRED
)
message(STATUS "OTel instrumentation headers found at: ${OTEL_INSTRUMENTATION_DIR}")

# ── Common include / link settings ─────────────────────────────────────────
set(COMMON_INCLUDES
    ${IARM_INCLUDE_DIR}
    ${OTEL_INSTRUMENTATION_DIR}
)

find_library(IARM_BUS_LIB  IARMBus  REQUIRED)
find_library(RDK_OTLP_LIB  rdk_otlp REQUIRED)

set(COMMON_LIBS
    ${IARM_BUS_LIB}
    ${RDK_OTLP_LIB}
    pthread
)

# ── Publisher / RPC-caller binary ───────────────────────────────────────────
add_executable(iarm_otel_test_pub iarm_otel_test_pub.c)
target_include_directories(iarm_otel_test_pub PRIVATE ${COMMON_INCLUDES})
target_link_libraries(iarm_otel_test_pub PRIVATE ${COMMON_LIBS})

# ── Subscriber / RPC-handler binary ────────────────────────────────────────
add_executable(iarm_otel_test_sub iarm_otel_test_sub.c)
target_include_directories(iarm_otel_test_sub PRIVATE ${COMMON_INCLUDES})
target_link_libraries(iarm_otel_test_sub PRIVATE ${COMMON_LIBS})

# ── Compatibility matrix binaries (pending scenarios) ─────────────────────
add_executable(iarm_otel_test2_pub iarm_otel_test2_pub.c)
target_include_directories(iarm_otel_test2_pub PRIVATE ${COMMON_INCLUDES})
target_link_libraries(iarm_otel_test2_pub PRIVATE ${COMMON_LIBS})

add_executable(iarm_otel_test2_sub iarm_otel_test2_sub.c)
target_include_directories(iarm_otel_test2_sub PRIVATE ${COMMON_INCLUDES})
target_link_libraries(iarm_otel_test2_sub PRIVATE ${COMMON_LIBS})

# ── Install ─────────────────────────────────────────────────────────────────
install(TARGETS iarm_otel_test_pub iarm_otel_test_sub
        iarm_otel_test2_pub iarm_otel_test2_sub
        DESTINATION ${CMAKE_INSTALL_BINDIR})
install(PROGRAMS run_iarm_otel_test.sh
         run_iarm_otel_test2.sh
        DESTINATION ${CMAKE_INSTALL_BINDIR})
