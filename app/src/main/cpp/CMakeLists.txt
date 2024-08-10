cmake_minimum_required(VERSION 3.22.1)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
add_link_options(LINKER:--build-id=none)

add_subdirectory("qrcodegen")
project("sharing")

add_library(NativeQRCodeGen SHARED qrgenerator.c)
target_link_libraries(NativeQRCodeGen PRIVATE qrcodegen)
