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

#ifndef TANGO_AUGMENTED_REALITY_AUGMENTED_REALITY_APP_H_
#define TANGO_AUGMENTED_REALITY_AUGMENTED_REALITY_APP_H_

#include <atomic>
#include <jni.h>
#include <memory>
#include <string>
#include <mutex>
#include <pthread.h>
#include <vector>

#include <tango_client_api.h>  // NOLINT
#include <tango_support_api.h>
#include <tango_3d_reconstruction_api.h>

#include <tango-gl/util.h>
#include <tango-gl/tango-gl.h>

#include <tango-augmented-reality/augmented_reality_scene.h>
#include <tango-augmented-reality/tango_event_data.h>
#include <tango-augmented-reality/texture_processor.h>


namespace tango_augmented_reality {
    struct GridIndex {
        Tango3DR_GridIndex indices;

        bool operator==(const GridIndex &other) const;
    };

    struct GridIndexHasher {
        std::size_t operator()(const tango_augmented_reality::GridIndex &index) const {
          std::size_t val = std::hash<int>()(index.indices[0]);
          val = hash_combine(val, std::hash<int>()(index.indices[1]));
          val = hash_combine(val, std::hash<int>()(index.indices[2]));
          return val;
        }

        static std::size_t hash_combine(std::size_t val1, std::size_t val2) {
          return (val1 << 1) ^ val2;
        }
    };
// AugmentedRealityApp handles the application lifecycle and resources.
class AugmentedRealityApp {
 public:
  AugmentedRealityApp();
  ~AugmentedRealityApp();
  // OnCreate() callback is called when this Android application's
  // OnCreate function is called from UI thread. In the OnCreate
  // function, we are only checking the Tango Core's version.
  //
  // @param env, java environment parameter OnCreate is being called.
  // @param caller_activity, caller of this function.
  // @param display_rotation, orienation param for the current display.
  void OnCreate(JNIEnv* env, jobject caller_activity, int display_rotation);

  // OnPause() callback is called when this Android application's
  // OnCreate function is called from UI thread. In our application,
  // we disconnect Tango Service and free the Tango configuration
  // file. It is important to disconnect Tango Service and release
  // the coresponding resources in the OnPause() callback from
  // Android, otherwise, this application will hold on to the Tango
  // resources and other application will not be able to connect to
  // Tango Service.
  void OnPause();

  // Call when Tango Service is connected successfully.
  void OnTangoServiceConnected(JNIEnv* env, jobject iBinder, double res, double dmin,
                               double dmax, int noise, bool land, bool textures);

  // When the Android activity is destroyed signal the JNI layer to
  // remove references to the activity. This should be called from the
  // onDestroyAugmentedReality() callback of the parent activity lifecycle.
  void OnDestroy();

  // Tango service event callback function for pose data. Called when new events
  // are available from the Tango Service.
  //
  // @param event: Tango event, caller allocated.
  void onTangoEventAvailable(const TangoEvent* event);

  // Tango service texture callback. Called when the texture is updated.
  //
  // @param id: camera Id of the updated camera.
  void onTextureAvailable(TangoCameraId id);

  // Allocate OpenGL resources for rendering, mainly initializing the AugmentedRealityScene.
  void OnSurfaceCreated();

  // Setup the view port width and height.
  void OnSurfaceChanged(int width, int height);

  // Main render loop.
  void OnDrawFrame();

  // Return transform debug string.
  std::string GetTransformString();

  // Retrun Tango event debug string.
  std::string GetEventString();

  // Retrun Tango Service version string.
  std::string GetVersionString();

  // Cache the Java VM
  //
  // @JavaVM java_vm: the Java VM is using from the Java layer.
  void SetJavaVM(JavaVM* java_vm) { java_vm_ = java_vm; }

  void SetAAssetManager(AAssetManager* manager) {
    main_scene_.aAssetManager = manager;
  }
  // Called when the device orientation changed
  //
  // @JavaVM display_rotation: orientation of current display.
  void OnDeviceRotationChanged(int display_rotation);

  //void onPointCloudAvailable(TangoPointCloud *point_cloud);
  //void onFrameAvailable(TangoCameraId id, const TangoImageBuffer *buffer);
  //void OnSurfaceCreated();
  void OnToggleButtonClicked(bool t3dr_is_running);
  //void OnClearButtonClicked();
  void Load(std::string filename);
  //void Save(std::string filename);
  float CenterOfStaticModel(bool horizontal);
  void SetView(float p, float y, float r, bool g) { pitch = p; yaw = y; roll = r; gyro = g;
    main_scene_.object_transform.SetRotation(glm::quat(glm::vec3(yaw,pitch,roll)));}
  void SetZoom(float value) {
    zoom = value;
    main_scene_.object_transform.SetScale(glm::vec3(zoom,zoom,zoom));
  }

  void AddMarkerToScene(float x, float y, float z);

    void MoveModel(float f, jfloat dX, jfloat dY);

    void HandleTouch(float x, float y);

private:
  // Request the render function from Java layer.
  void RequestRender();

  // Update current transform and previous transform.
  //
  // @param transform: transform data of current frame.
  // @param timestamp: timestamp of the current transform.
  void UpdateTransform(const double transform[16], double timestamp);

  // Format debug string with current and last transforms information.
  void FormatTransformString();

  void UpdateViewportAndProjectionMatrix();

  // Setup the configuration file for the Tango Service. We'll also see whether
  // we'd like auto-recover enabled.
  void TangoSetupConfig();

  // Connect the OnTextureAvailable and OnTangoEvent callbacks.
  void TangoConnectCallbacks();

  // Connect to Tango Service.
  // This function will start the Tango Service pipeline, in this case, it will
  // start Motion Tracking.
  void TangoConnect();

  // Disconnect from Tango Service, release all the resources that the app is
  // holding from Tango Service.
  void TangoDisconnect();

  // Release all non-OpenGL resources that allocate from the program.
  void DeleteResources();

  // Current position of the Color Camera with respect to Start of Service.
  glm::mat4 cur_start_service_T_camera_;
  // prev_start_service_T_camera_, transform_counter_ and transform_string_ are
  // used for
  // composing the debug string to display the useful information on screen.
  //glm::mat4 prev_start_service_T_camera_;

  // Debug transform string.
  std::string transform_string_;

  // Timestamps of the current and last transforms.
  double cur_timestamp_;
  double prev_timestamp_;

  // Pose counter for debug purpose.
  size_t transform_counter_;

  // Mutex for protecting the transform data. The transform data is shared
  // between render thread and TangoService callback thread.
  std::mutex transform_mutex_;

  // tango_event_data_ handles all Tango event callbacks,
  // onTangoEventAvailable() in this object will be routed to tango_event_data_
  // to handle.
  TangoEventData tango_event_data_;

  // tango_event_data_ is share between the UI thread we start for updating
  // debug texts and the TangoService event callback thread. We keep
  // event_mutex_ to
  // protect tango_event_data_.
  std::mutex tango_event_mutex_;

  // main_scene_ includes all drawable object for visualizing Tango device's
  // movement.
  AugmentedRealityScene main_scene_;

  // Tango configration file, this object is for configuring Tango Service setup
  // before connect to service. For example, we set the flag
  // config_enable_auto_recovery based user's input and then start Tango.
  TangoConfig tango_config_;

  // Device color camera intrinsics, these intrinsics value is used for
  // calculate the camera frustum and image aspect ratio. In the AR view, we
  // want to match the virtual camera's intrinsics to the actual physical camera
  // as close as possible.
  TangoCameraIntrinsics color_camera_intrinsics_;

  // Tango service version string.
  std::string tango_core_version_string_;

  // Cached Java VM, caller activity object and the request render method. These
  // variables are used for on demand render request from the onTextureAvailable
  // callback.
  JavaVM* java_vm_;
  jobject calling_activity_obj_;
  jmethodID on_demand_render_;
  jmethodID on_demand_show_marker_info;

  std::atomic<bool> is_service_connected_;
  std::atomic<bool> is_gl_initialized_;
  std::atomic<bool> is_video_overlay_rotation_set_;

  int viewport_width_;
  int viewport_height_;

  int display_rotation_;

  Tango3DR_Context TangoSetup3DR(double res, double dmin, double dmax, int noise);

  bool t3dr_is_running_;
  Tango3DR_CameraCalibration t3dr_intrinsics_;
  Tango3DR_CameraCalibration t3dr_intrinsics_depth;
  std::mutex binder_mutex_;
  std::mutex render_mutex_;

  TextureProcessor* textureProcessor;
  bool gyro;
  bool landscape;
  bool textured;
  float scale;
  float pitch;
  float yaw;
  float roll;
  float zoom;

    void CheckForMarkerTouch(float x, float y);

    bool intersectRayTriangle(glm::vec3 tvec3, glm::vec3 dir, glm::vec3 v0, glm::vec3 v1, glm::vec3 v2,
                              float time);

    void RequestShowMarkerAt(int index);
};
}  // namespace tango_augmented_reality

#endif  // TANGO_AUGMENTED_REALITY_AUGMENTED_REALITY_APP_H_
