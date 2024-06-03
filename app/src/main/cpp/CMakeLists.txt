cmake_minimum_required(VERSION 3.22.1)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_subdirectory("qrcodegen")
project("filescenter")

add_library(NativeQRCodeGen SHARED qrgenerator.c)

target_link_libraries(NativeQRCodeGen PRIVATE qrcodegen)
# Where is my C files ????????????!!!!!!! Your code will appear when you do successful build this is the old app on your table
