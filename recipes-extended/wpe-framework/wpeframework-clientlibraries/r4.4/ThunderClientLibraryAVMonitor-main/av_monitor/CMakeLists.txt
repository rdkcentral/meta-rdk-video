add_executable(av_monitor main.c)

target_include_directories(av_monitor
    PRIVATE
        $<BUILD_INTERFACE:${CMAKE_CURRENT_LIST_DIR}/../>
        $<INSTALL_INTERFACE:avmonitor.h>
)

target_link_libraries(av_monitor
    PRIVATE
        ${NAMESPACE}AVMonitor)

install(TARGETS av_monitor
    DESTINATION bin)
