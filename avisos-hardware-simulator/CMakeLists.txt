cmake_minimum_required(VERSION 3.15)
project(avisos_hardware_simulator CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# External packages
find_package(Boost REQUIRED)
find_package(spdlog REQUIRED)

# Header-only libraries via FetchContent
include(FetchContent)

FetchContent_Declare(
    json
    GIT_REPOSITORY https://github.com/nlohmann/json.git
    GIT_TAG        v3.11.3
    GIT_SHALLOW    TRUE
)

FetchContent_Declare(
    httplib
    GIT_REPOSITORY https://github.com/yhirose/cpp-httplib.git
    GIT_TAG        v0.18.3
    GIT_SHALLOW    TRUE
)

FetchContent_MakeAvailable(json httplib)

# Source files
file(GLOB_RECURSE SOURCES "src/*.cpp" "src/*.h")

add_executable(${PROJECT_NAME} ${SOURCES})

target_link_libraries(${PROJECT_NAME}
    Boost::boost
    spdlog::spdlog
    nlohmann_json::nlohmann_json
    httplib::httplib
)
