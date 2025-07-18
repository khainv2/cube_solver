# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("kubesolver")

set(SOURCE_FILES
        algorithm/blockcube.cpp
        algorithm/colorfulcube.cpp
        algorithm/defines.cpp
        algorithm/move.cpp
        algorithm/permutationphase.cpp
        algorithm/rotationphase.cpp
        algorithm/solver.cpp
        algorithm/utils.cpp
        native-lib.cpp
)

add_library(${CMAKE_PROJECT_NAME} SHARED ${SOURCE_FILES})


include_directories(${CMAKE_SOURCE_DIR}/algorithm)

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        android
        log)