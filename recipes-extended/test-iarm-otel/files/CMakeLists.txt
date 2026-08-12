cmake_minimum_required(VERSION 3.10)
project(test-iarm-otel VERSION 1.0.0 LANGUAGES C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

# ── Paths ──────────────────────────────────────────────────────────────────
# In Yocto these are set via EXTRA_OECMAKE; on a local build pass -D on the
# cmake command line:
#   cmake -S . -B build \
#     -DIARM_INCLUDE_DIR=/path/to/iarmbus/core/include \
#     -DOTEL_INSTRUMENTATION_DIR=/path/to/opentelemetry/rdk-distributed-tracer/instrumentation
if(NOT DEFINED IARM_INCLUDE_DIR)
    set(IARM_INCLUDE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../iarmbus/core/include")
endif()
if(NOT DEFINED OTEL_INSTRUMENTATION_DIR)
    set(OTEL_INSTRUMENTATION_DIR
        "${CMAKE_CURRENT_SOURCE_DIR}/../../opentelemetry/rdk-distributed-tracer/instrumentation")
endif()
# iarm_otel.h lives in the core/ dir (not include/)
set(IARM_CORE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../iarmbus/core")

# ── Common include / link settings ─────────────────────────────────────────
set(COMMON_INCLUDES
    ${IARM_INCLUDE_DIR}
    ${IARM_CORE_DIR}
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

# ── Install ─────────────────────────────────────────────────────────────────
install(TARGETS iarm_otel_test_pub iarm_otel_test_sub
        DESTINATION ${CMAKE_INSTALL_BINDIR})
install(PROGRAMS run_iarm_otel_test.sh
        DESTINATION ${CMAKE_INSTALL_BINDIR})
