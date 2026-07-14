cmake_minimum_required(VERSION 3.10)
project(OtelPluginTest VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(WPEFramework REQUIRED)
find_package(${NAMESPACE}Plugins REQUIRED)

find_library(RDK_OTLP_LIB rdk_otlp)

add_library(${PROJECT_NAME} MODULE OtelPluginTest.cpp)

target_link_libraries(${PROJECT_NAME}
    PRIVATE
        ${NAMESPACE}::${NAMESPACE}Core
        ${NAMESPACE}::${NAMESPACE}Plugins
        $<$<BOOL:${RDK_OTLP_LIB}>:rdk_otlp>
)

# Pick up rdk_otlp_instrumentation.h if present
if(RDK_OTLP_LIB)
    target_compile_definitions(${PROJECT_NAME} PRIVATE RDK_OTEL_ENABLED=1)
endif()

set_target_properties(${PROJECT_NAME} PROPERTIES
    CXX_STANDARD 14
    PREFIX ""
)

install(TARGETS ${PROJECT_NAME}
    DESTINATION ${CMAKE_INSTALL_PREFIX}/${CMAKE_INSTALL_LIBDIR}/${NAMESPACE}/plugins
)

install(FILES OtelPluginTest.json
    DESTINATION ${CMAKE_INSTALL_PREFIX}/etc/${NAMESPACE}/plugins
)
