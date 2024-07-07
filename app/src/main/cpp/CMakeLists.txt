cmake_minimum_required(VERSION 3.22.1)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_subdirectory("qrcodegen")
project("filescenter")

add_library(NativeQRCodeGen SHARED qrgenerator.c)
set(CMAKE_EXE_LINKER_FLAGS  "--build-id=none")
target_link_libraries(NativeQRCodeGen PRIVATE qrcodegen)
