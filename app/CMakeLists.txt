# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/native-lib.cpp )

add_library( # Sets the name of the library.
             my-native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/jni/tango-augmented-reality/augmented_reality_app.h
             src/main/jni/tango-augmented-reality/augmented_reality_scene.h
             src/main/jni/tango-augmented-reality/tango_event_data.h
             src/main/jni/tango-augmented-reality/viewer_app.h
             src/main/jni/tango-augmented-reality/viewer_scene.h
             src/main/jni/Android.mk
             src/main/jni/Application.mk
             src/main/jni/augmented_reality_app.cc
             src/main/jni/jni_interface.cc
             src/main/jni/augmented_reality_scene.cc
             src/main/jni/viewer_app.cc
             src/main/jni/viewer_scene.cc
             src/main/jni/tango_event_data.cc)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib
                       my-native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
