cmake_minimum_required(VERSION 3.10)
project(test-iarm-tp VERSION 1.0.0 LANGUAGES C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

# ── Locate IARM headers (iarmbus2 - libIBus.h exposes IARM_Bus_SetTraceparent/
#    IARM_Bus_GetTraceparent, no OTel dependency inside IARM itself) ────────
find_path(IARM_INCLUDE_DIR
    NAMES libIBus.h
    PATH_SUFFIXES rdk/iarmbus iarmbus rdk
    REQUIRED
)
message(STATUS "IARM headers found at: ${IARM_INCLUDE_DIR}")

# ── Locate OTel instrumentation header - used only by these test apps to
#    obtain/consume traceparent strings, never by IARM itself ─────────────
find_path(OTEL_INSTRUMENTATION_DIR
    NAMES rdk_otlp_instrumentation.h
    PATH_SUFFIXES rdk_otlp rdk/otlp otlp rdk
    REQUIRED
)
message(STATUS "OTel instrumentation headers found at: ${OTEL_INSTRUMENTATION_DIR}")

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

add_executable(iarm_tp_test_pub iarm_tp_test_pub.c)
target_include_directories(iarm_tp_test_pub PRIVATE ${COMMON_INCLUDES})
target_link_libraries(iarm_tp_test_pub PRIVATE ${COMMON_LIBS})

add_executable(iarm_tp_test_sub iarm_tp_test_sub.c)
target_include_directories(iarm_tp_test_sub PRIVATE ${COMMON_INCLUDES})
target_link_libraries(iarm_tp_test_sub PRIVATE ${COMMON_LIBS})

install(TARGETS iarm_tp_test_pub iarm_tp_test_sub
        DESTINATION ${CMAKE_INSTALL_BINDIR})
install(PROGRAMS run_iarm_tp_test.sh
        DESTINATION ${CMAKE_INSTALL_BINDIR})
