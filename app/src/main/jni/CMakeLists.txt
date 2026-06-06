cmake_minimum_required(VERSION 3.21)

# Corrosion for rust
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/corrosion)

# We use C not C++
# set(CMAKE_CXX_STANDARD 11)
# set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_link_options(LINKER:--build-id=none)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/libs/qrcodegen)

project("sharing")



add_library(nativeutils SHARED 
    ${CMAKE_CURRENT_SOURCE_DIR}/utils.c
    ${CMAKE_CURRENT_SOURCE_DIR}/websocket/utils.c
    ${CMAKE_CURRENT_SOURCE_DIR}/websocket/frame.c)

target_link_libraries(nativeutils PRIVATE qrcodegen log)


corrosion_import_crate(
    MANIFEST_PATH ${CMAKE_CURRENT_SOURCE_DIR}/jni-rs/Cargo.toml
    CRATE_TYPES cdylib
)
file(WRITE ${CMAKE_CURRENT_BINARY_DIR}/dummy.cpp "")
add_library(rust_packaging_wrapper SHARED ${CMAKE_CURRENT_BINARY_DIR}/dummy.cpp)
target_link_libraries(rust_packaging_wrapper PUBLIC rust_native)