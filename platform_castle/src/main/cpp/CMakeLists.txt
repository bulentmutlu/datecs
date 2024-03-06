# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("ctos_api")

#${PROJECT_SOURCE_DIR} is the directory where the main CMakeList.txt file resides.
#That's typically app/src/main/cpp
#include_directories(${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/openssl/armeabi-v7a/include)


# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        ctos_api

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ctos_api.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        ctos_api
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libcartc.so
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libcamsr.so
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libcasc.so
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libcacl.so
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libcasystem.so
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libcaprt.so
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libcafont.so
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libcainterface.so
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libcalcd.so
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libcafs.so
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libcasound.so
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libsbcrypto.so
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libsbkms.so
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libcabarcode.so
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libcaclmdl.so
        ${PROJECT_SOURCE_DIR}/../../../libs/jniLibs/ctos/${ANDROID_ABI}/libcaemvl2ap.so
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})