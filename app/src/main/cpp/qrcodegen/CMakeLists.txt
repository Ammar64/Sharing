cmake_minimum_required(VERSION 3.5)

project(qrcodegen VERSION 1.8.0)

add_library(qrcodegen STATIC)
target_sources(qrcodegen PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/qrcodegen.cpp)
target_include_directories(qrcodegen PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})
