cmake_minimum_required(VERSION 3.10)
project(OtelPluginTest VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

if (USE_THUNDER_R5)
    find_package(Thunder REQUIRED)
else ()
    find_package(WPEFramework REQUIRED)
endif()

find_package(${NAMESPACE}Core REQUIRED)
find_package(${NAMESPACE}Plugins REQUIRED)

find_library(RDK_OTLP_LIB rdk_otlp)

set(MODULE_NAME ${NAMESPACE}OtelPluginTest)

add_library(${MODULE_NAME} SHARED OtelPluginTest.cpp)

target_link_libraries(${MODULE_NAME}
    PRIVATE
        ${NAMESPACE}Core::${NAMESPACE}Core
        ${NAMESPACE}Plugins::${NAMESPACE}Plugins
        $<$<BOOL:${RDK_OTLP_LIB}>:${RDK_OTLP_LIB}>
)

# Pick up rdk_otlp_instrumentation.h if present
if(RDK_OTLP_LIB)
    target_compile_definitions(${MODULE_NAME} PRIVATE RDK_OTEL_ENABLED=1)
endif()

set_target_properties(${MODULE_NAME} PROPERTIES
    CXX_STANDARD 14
    CXX_STANDARD_REQUIRED YES
)

if (USE_THUNDER_R5)
    install(TARGETS ${MODULE_NAME} DESTINATION ${CMAKE_INSTALL_PREFIX}/lib/thunder/plugins)
else ()
    install(TARGETS ${MODULE_NAME} DESTINATION ${CMAKE_INSTALL_PREFIX}/lib/wpeframework/plugins)
endif()

install(FILES OtelPluginTest.json
    DESTINATION ${CMAKE_INSTALL_PREFIX}/etc/WPEFramework/plugins
)
