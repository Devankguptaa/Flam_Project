cmake_minimum_required(VERSION 3.22)
project(native_edgedetector)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Configure OpenCV: set OpenCV_DIR before configuring, e.g.
# -DOpenCV_DIR="/path/to/OpenCV-android-sdk/sdk/native/jni"
find_package(OpenCV REQUIRED)
if(NOT OpenCV_FOUND)
    message(FATAL_ERROR 
        "OpenCV not found! Set OpenCV_DIR to point to OpenCV-android-sdk/sdk/native/jni")
endif()
message(STATUS "OpenCV found: ${OpenCV_VERSION}")

add_library(native-lib SHARED
    native-lib.cpp
)

find_library(log-lib log)

# Include directories
target_include_directories(native-lib PRIVATE
    ${OpenCV_INCLUDE_DIRS}
)

# Link libraries
target_link_libraries(native-lib
    ${log-lib}
    ${OpenCV_LIBS}
)
