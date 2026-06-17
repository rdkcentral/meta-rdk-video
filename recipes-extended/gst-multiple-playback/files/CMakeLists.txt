cmake_minimum_required(VERSION 3.10)
project(gst_multiple_playback LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include(GNUInstallDirs)

find_package(PkgConfig REQUIRED)
pkg_check_modules(GST REQUIRED
    gstreamer-1.0
)

add_executable(gst_multiple_playback src/main.cpp)

target_include_directories(gst_multiple_playback PRIVATE ${GST_INCLUDE_DIRS})
target_link_libraries(gst_multiple_playback PRIVATE ${GST_LIBRARIES})
target_compile_options(gst_multiple_playback PRIVATE ${GST_CFLAGS_OTHER})

install(TARGETS gst_multiple_playback DESTINATION ${CMAKE_INSTALL_BINDIR})

