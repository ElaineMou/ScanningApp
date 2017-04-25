#
# Copyright 2014 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)
PROJECT_ROOT_FROM_JNI:= ../../../..
PROJECT_ROOT:= $(call my-dir)/../../../..

include $(CLEAR_VARS)
LOCAL_MODULE    := libscanningapp
LOCAL_SHARED_LIBRARIES := tango_client_api tango_support_api tango_3d_reconstruction
LOCAL_STATIC_LIBRARIES := png
LOCAL_CFLAGS    := -std=c++11

LOCAL_SRC_FILES := augmented_reality_app.cc \
                   augmented_reality_scene.cc \
                   jni_interface.cc \
                   mask_processor.cc \
                   model_io.cc \
                   tango_event_data.cc \
                   texture_processor.cc \
                   vertex_processor.cc \
                   viewer_app.cc \
                   viewer_scene.cc \
                   $(PROJECT_ROOT_FROM_JNI)/tango_gl/bounding_box.cc \
                   $(PROJECT_ROOT_FROM_JNI)/tango_gl/camera.cc \
                   $(PROJECT_ROOT_FROM_JNI)/tango_gl/conversions.cc \
                   $(PROJECT_ROOT_FROM_JNI)/tango_gl/drawable_object.cc \
                   $(PROJECT_ROOT_FROM_JNI)/tango_gl/frustum.cc \
                   $(PROJECT_ROOT_FROM_JNI)/tango_gl/gesture_camera.cc \
                   $(PROJECT_ROOT_FROM_JNI)/tango_gl/line.cc \
                   $(PROJECT_ROOT_FROM_JNI)/tango_gl/meshes.cc \
                   $(PROJECT_ROOT_FROM_JNI)/tango_gl/shaders.cc \
                   $(PROJECT_ROOT_FROM_JNI)/tango_gl/tango_gl.cc \
                   $(PROJECT_ROOT_FROM_JNI)/tango_gl/texture.cc \
                   $(PROJECT_ROOT_FROM_JNI)/tango_gl/transform.cc \
                   $(PROJECT_ROOT_FROM_JNI)/tango_gl/util.cc \
                   $(PROJECT_ROOT_FROM_JNI)/tango_gl/video_overlay.cc

LOCAL_C_INCLUDES := $(PROJECT_ROOT)/tango_gl/include \
                    $(PROJECT_ROOT)/third_party/glm/ \
                    $(PROJECT_ROOT)/third_party/libpng/include/ \
                    $(PROJECT_ROOT)/tango_3d_reconstruction/include/ \
                    tango-augmented-reality/augmented_reality_app.h \
                    tango-augmented-reality/augmented_reality_scene.h \
                    tango-augmented-reality/mask_processor.h \
                    tango-augmented-reality/math_utils.h \
                    tango-augmented-reality/model_io.h \
                    tango-augmented-reality/tango_event_data.h \
                    tango-augmented-reality/texture_processor.h \
                    tango-augmented-reality/vertex_processor.h \
                    tango-augmented-reality/viewer_app.h \
                    tango-augmented-reality/viewer_scene.h

LOCAL_LDLIBS    := -llog -lGLESv2 -L$(SYSROOT)/usr/lib -lz -landroid
include $(BUILD_SHARED_LIBRARY)

$(call import-add-path, $(PROJECT_ROOT))
$(call import-add-path, $(PROJECT_ROOT)/third_party)
$(call import-module,libpng)
$(call import-module,tango_client_api)
$(call import-module,tango_support_api)
$(call import-module,tango_3d_reconstruction)