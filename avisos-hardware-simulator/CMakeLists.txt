cmake_minimum_required(VERSION 3.15)
project(avisos_hardware_simulator CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Include source files
file(GLOB_RECURSIVE SOURCES "src/*.cpp" "src/*.h")

add_executable(hardware_simulator ${SOURCES})

# If using a basic HTTP client library like cpp-httplib or libcurl, 
# you would link them here. For a pure zero-dependency random stream, 
# standard library works out of the box.