cmake_minimum_required(VERSION 3.21)

# We're use C not C++
# set(CMAKE_CXX_STANDARD 11)
# set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_link_options(LINKER:--build-id=none)

add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/libs/qrcodegen)
project("sharing")

add_library(nativeutils SHARED 
    ${CMAKE_CURRENT_SOURCE_DIR}/utils.c)

target_link_libraries(nativeutils PRIVATE qrcodegen log)
