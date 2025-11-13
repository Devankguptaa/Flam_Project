cmake_minimum_required(VERSION 3.22)
project(edgedetector_app)

# Delegate native build to jni/CMakeLists.txt
add_subdirectory(../jni ${CMAKE_CURRENT_BINARY_DIR}/jni_build)
