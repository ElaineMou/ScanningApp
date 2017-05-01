/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define GLM_FORCE_RADIANS

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#include <jni.h>
#include <tango-augmented-reality/augmented_reality_app.h>
#include <tango-augmented-reality/viewer_app.h>

static tango_augmented_reality::AugmentedRealityApp augmentedRealityApp;
static tango_augmented_reality::ViewerApp viewerApp;

std::string jstring2string(JNIEnv* env, jstring name)
{
  const char *s = env->GetStringUTFChars(name,NULL);
  std::string str( s );
  env->ReleaseStringUTFChars(name,s);
  return str;
}


#ifdef __cplusplus
extern "C" {
#endif
jint JNI_OnLoad(JavaVM* vm, void*) {
  // We need to store a reference to the Java VM so that we can call into the
  // Java layer to trigger rendering.
  augmentedRealityApp.SetJavaVM(vm);
  viewerApp.SetJavaVM(vm);
  return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onCreateAugmentedReality(
    JNIEnv* env, jobject, jobject activity, int display_orientation) {
  augmentedRealityApp.OnCreate(env, activity, display_orientation);
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onTangoServiceConnectedAugmentedReality(
    JNIEnv* env, jobject, jobject iBinder, jdouble res, jdouble dmin, jdouble dmax,
    jint noise, jboolean land, jboolean photo, bool textures) {
  augmentedRealityApp.OnTangoServiceConnected(env, iBinder, res, dmin, dmax, noise, land, textures);
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onPauseAugmentedReality(
    JNIEnv*, jobject) {
  augmentedRealityApp.OnPause();
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onDestroyAugmentedReality(
    JNIEnv*, jobject) {
    augmentedRealityApp.OnDestroy();
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onGlSurfaceCreatedAugmentedReality(
    JNIEnv* env, jobject, jobject j_asset_manager) {
  AAssetManager* aasset_manager = AAssetManager_fromJava(env, j_asset_manager);
  augmentedRealityApp.OnSurfaceCreated();
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onGlSurfaceChangedAugmentedReality(
    JNIEnv*, jobject, jint width, jint height) {
  augmentedRealityApp.OnSurfaceChanged(width, height);
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onGlSurfaceDrawFrameAugmentedReality(
    JNIEnv*, jobject) {
  augmentedRealityApp.OnDrawFrame();
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onConfigurationChangedAugmentedReality(
    JNIEnv*, jobject, int display_orientation) {
  augmentedRealityApp.OnDeviceRotationChanged(display_orientation);
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_loadAugmentedReality(JNIEnv* env, jobject, jstring name) {
  augmentedRealityApp.Load(jstring2string(env,name));
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_setViewAugmentedReality(JNIEnv *env, jclass type,
                                                                     jfloat mYaw, jfloat mPitch,
                                                                     jfloat mRoll, jfloat mMoveX,
                                                                     jfloat mMoveY, jfloat mMoveZ) {

  augmentedRealityApp.SetView(mPitch,mYaw,mRoll,mMoveX,mMoveY,mMoveZ,false);

}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_setZoomAugmentedReality(JNIEnv *env, jclass type,
                                                                     jfloat mZoom) {

  augmentedRealityApp.SetZoom(mZoom);
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onCreateViewer(
        JNIEnv* env, jobject, jobject activity, int display_orientation) {
  viewerApp.OnCreate(env,activity,display_orientation);
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onTangoServiceConnectedViewer(
        JNIEnv* env, jobject, jobject iBinder, jdouble res, jdouble dmin, jdouble dmax,
        jint noise, jboolean land, jboolean photo, bool textures) {
  viewerApp.OnTangoServiceConnected(env, iBinder, res, dmin, dmax, noise, land, textures);
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onPauseViewer(JNIEnv *env, jclass type) {
  viewerApp.OnPause();
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onDestroyViewer(JNIEnv *env, jclass type) {
  viewerApp.OnDestroy();
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onGlSurfaceCreatedViewer(JNIEnv *env, jclass type,
                                                                      jobject assetManager) {

  viewerApp.SetAAssetManager(AAssetManager_fromJava(env, assetManager));
  viewerApp.OnSurfaceCreated();
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onGlSurfaceChangedViewer(JNIEnv *env, jobject,
                                                                      jint width, jint height) {
  viewerApp.OnSurfaceChanged(width,height);
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onGlSurfaceDrawFrameViewer(JNIEnv *env, jobject) {
  viewerApp.OnDrawFrame();
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_onConfigurationChangedViewer(JNIEnv *env, jclass type,
                                                                          jint displayOrientation) {
  viewerApp.OnDeviceRotationChanged(displayOrientation);
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_loadViewer(JNIEnv *env, jclass type,
                                                        jstring fileName_) {
  viewerApp.Load(jstring2string(env,fileName_));
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_setViewViewer(JNIEnv *env, jclass type, jfloat yaw,
                                                     jfloat pitch, jfloat roll) {
    viewerApp.SetView(pitch,yaw,roll);
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_setZoomViewer(JNIEnv *env, jclass type, jfloat zoom) {
    viewerApp.SetZoom(zoom);
}


JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_handleTouchViewer(JNIEnv *env, jclass type, jfloat x,
                                                             jfloat y) {

    viewerApp.HandleTouch(x, y);
}


JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_setMarkersVisibleViewer(JNIEnv *env, jclass type,
                                                                     jboolean b) {
    viewerApp.SetMarkersVisible(b);
}


JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_setAddingMarkersViewer(JNIEnv *env, jclass type,
                                                                    jboolean b) {
    viewerApp.SetAddingMarkers(b);
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_removeMarkerAtViewer(JNIEnv *env, jclass type, jint i) {

    viewerApp.removeMarkerAt(i);
}


JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_renderMarkerViewer(JNIEnv *env, jclass type, jfloat x,
                                                           jfloat y,
                                                           jfloat z) {
    viewerApp.AddMarkerToScene(x,y,z);
}

JNIEXPORT void JNICALL
Java_seniordesign_scanningapp_JNINative_moveCameraViewer(JNIEnv *env, jclass type, jfloat factor,
                                                         jfloat dX, jfloat dY) {
    viewerApp.MoveCamera(factor, dX, dY);
}

#ifdef __cplusplus
}
#endif
