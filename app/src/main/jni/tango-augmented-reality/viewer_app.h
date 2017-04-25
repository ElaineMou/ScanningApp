//
// Created by Elaine on 4/6/2017.
//

#ifndef SCANNINGAPP_VIEWER_APP_H
#define SCANNINGAPP_VIEWER_APP_H

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

#include <tango-augmented-reality/texture_processor.h>
#include "texture_processor.h"
#include "viewer_scene.h"

namespace tango_augmented_reality {

// AugmentedRealityApp handles the application lifecycle and resources.
class ViewerApp {
    public:
        ViewerApp();
        ~ViewerApp();
        // OnCreate() callback is called when this Android application's
        // OnCreate function is called from UI thread. In the OnCreate
        // function, we are only checking the Tango Core's version.
        //
        // @param env, java environment parameter OnCreate is being called.
        // @param caller_activity, caller of this function.
        // @param display_rotation, orienation param for the current display.
        void OnCreate(JNIEnv* env, jobject caller_activity, int display_rotation);

        void OnTangoServiceConnected(JNIEnv *env, jobject iBinder, double res, double dmin, double dmax,
                                 int noise, bool land, bool textures);

        // OnPause() callback is called when this Android application's
        // OnCreate function is called from UI thread. In our application,
        // we disconnect Tango Service and free the Tango configuration
        // file. It is important to disconnect Tango Service and release
        // the coresponding resources in the OnPause() callback from
        // Android, otherwise, this application will hold on to the Tango
        // resources and other application will not be able to connect to
        // Tango Service.
        void OnPause();

        // When the Android activity is destroyed signal the JNI layer to
        // remove references to the activity. This should be called from the
        // onDestroyAugmentedReality() callback of the parent activity lifecycle.
        void OnDestroy();

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

        void Load(std::string filename);
        void AddBall(float x, float y);
        glm::vec3 CenterOfStaticModel();
        void Save(std::string filename);
        void SetView(float p, float y, float r, float mx, float my, float mz, bool g) { pitch = p; yaw = y; roll = r;
            movex = mx; movey = my; movez = mz;}
        void SetZoom(float value) { zoom = value;}

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

        void TangoConnect();

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

        // main_scene_ includes all drawable object for visualizing Tango device's
        // movement.
        ViewerScene main_scene_;

        // Tango configration file, this object is for configuring Tango Service setup
        // before connect to service. For example, we set the flag
        // config_enable_auto_recovery based user's input and then start Tango.
        TangoConfig tango_config_;

        // Tango service version string.
        std::string tango_core_version_string_;

        // Cached Java VM, caller activity object and the request render method. These
        // variables are used for on demand render request from the onTextureAvailable
        // callback.
        JavaVM* java_vm_;
        jobject calling_activity_obj_;
        jmethodID on_demand_render_;

        std::atomic<bool> is_service_connected_;
        std::atomic<bool> is_gl_initialized_;
        std::atomic<bool> is_video_overlay_rotation_set_;

        int viewport_width_;
        int viewport_height_;

        int display_rotation_;
        Tango3DR_CameraCalibration t3dr_intrinsics_;
        Tango3DR_CameraCalibration t3dr_intrinsics_depth;

    //glm::mat4 image_matrix;
        //glm::quat image_rotation;
        std::mutex binder_mutex_;
        std::mutex render_mutex_;

        //glm::mat4 point_cloud_matrix_;

        TextureProcessor* textureProcessor;
        std::vector<TextureProcessor*> toDelete;

        bool landscape;
        bool textured;
        float scale;
        float movex;
        float movey;
        float movez;
        float pitch;
        float yaw;
        float roll;
        float zoom;
        float lastBall=0.0f;

    void TangoDisconnect();
};
}  // namespace tango_augmented_reality


#endif //SCANNINGAPP_VIEWER_APP_H
