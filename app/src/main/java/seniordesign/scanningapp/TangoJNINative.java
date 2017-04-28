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

package seniordesign.scanningapp;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.util.Log;

/**
 * Interfaces between native C++ code and Java code.
 */
public class TangoJNINative {
    static {
        // This project depends on tango_client_api, so we need to make sure we loadAugmentedReality
        // the correct library first.
        if (TangoInitializationHelper.loadTangoSharedLibrary() ==
                TangoInitializationHelper.ARCH_ERROR) {
            Log.e("TangoJNINative", "ERROR! Unable to loadAugmentedReality libtango_client_api.so!");
        }
        System.loadLibrary("scanningapp");
    }

    /**
     * Interfaces to native OnCreate function.
     *
     * @param callerActivity the caller activity of this function.
     * @param activityOrientation orientation of the display.
     */
    public static native void onCreateAugmentedReality(Activity callerActivity, int activityOrientation);

    /**
     * Called when the Tango service is connected successfully.
     *
     * @param nativeTangoServiceBinder The native binder object.
     */
    public static native void onTangoServiceConnectedAugmentedReality(IBinder nativeTangoServiceBinder, double res,
                                                                      double dmin, double dmax, int noise,
                                                                      boolean land, boolean photo, boolean textures);

    /**
     * Interfaces to native OnPause function.
     */
    public static native void onPauseAugmentedReality();

    /**
     * Signal that the activity has been destroyed and remove any cached references.
     */
    public static native void onDestroyAugmentedReality();

    /**
     * Allocate OpenGL resources for rendering.
     */
    public static native void onGlSurfaceCreatedAugmentedReality(AssetManager assetManager);

    /**
     * Setup the view port width and height.
     */
    public static native void onGlSurfaceChangedAugmentedReality(int width, int height);

    /**
     * Main onGlSurfaceDrawFrameAugmentedReality loop.
     */
    public static native void onGlSurfaceDrawFrameAugmentedReality();

    /**
     * Configuration changed callback, called when screen rotates.
     */
    public static native void onConfigurationChangedAugmentedReality(int displayOrientation);

    /**
     * Load file from name into app's scene's dynamic meshes list.
     */
    public static native void loadAugmentedReality(String fileName);


    /**
     * Interfaces to native OnCreate function.
     *
     * @param callerActivity the caller activity of this function.
     * @param activityOrientation orientation of the display.
     */
    public static native void onCreateViewer(Activity callerActivity, int activityOrientation);

    /**
     * Called when the Tango service is connected successfully.
     *
     * @param nativeTangoServiceBinder The native binder object.
     */
    public static native void onTangoServiceConnectedViewer(IBinder nativeTangoServiceBinder, double res,
                                                                      double dmin, double dmax, int noise,
                                                                      boolean land, boolean photo, boolean textures);

    /**
     * Interfaces to native OnPause function.
     */
    public static native void onPauseViewer();

    /**
     * Signal that the activity has been destroyed and remove any cached references.
     */
    public static native void onDestroyViewer();

    /**
     * Allocate OpenGL resources for rendering.
     */
    public static native void onGlSurfaceCreatedViewer(AssetManager assetManager);

    /**
     * Setup the view port width and height.
     */
    public static native void onGlSurfaceChangedViewer(int width, int height);

    /**
     * Main onGlSurfaceDrawFrameAugmentedReality loop.
     */
    public static native void onGlSurfaceDrawFrameViewer();

    /**
     * Configuration changed callback, called when screen rotates.
     */
    public static native void onConfigurationChangedViewer(int displayOrientation);

    /**
     * Load file from name into app's scene's dynamic meshes list.
     */
    public static native void loadViewer(String fileName);

    public static native void setViewViewer(float yaw, float pitch, float roll, float moveX, float moveY,
                                      float moveZ);
    public static native void setZoomViewer(float zoom);

    public static native void handleTouchViewer(float x, float y);

    public static native void setViewAugmentedReality(float mYaw, float mPitch, float mRoll, float mMoveX, float mMoveY, float mMoveZ);

    public static native void setZoomAugmentedReality(float mZoom);

    public static native void setMarkersVisibleViewer(boolean b);

    public static native void setAddingMarkersViewer(boolean b);

    public static native void removeMarkerAtViewer(int i);
}