//
// Created by Elaine on 4/6/2017.
//

#include "tango-augmented-reality/viewer_app.h"
#include <sstream>
#include <glm/gtx/transform.hpp>

#include <tango-gl/conversions.h>
#include <tango-gl/util.h>

#include <tango_support_api.h>
#include <tango-augmented-reality/mask_processor.h>
#include <tango-augmented-reality/math_utils.h>
#include <tango-augmented-reality/vertex_processor.h>
#include <tango-gl/meshes.h>
#include <tango-gl/shaders.h>

namespace {
    const int kVersionStringLength = 128;
// The minimum Tango Core version required from this application.
    const int kTangoCoreMinimumVersion = 9377;

// Far clipping plane of the AR camera.
    const float kArCameraNearClippingPlane = 0.1f;
    const float kArCameraFarClippingPlane = 100.0f;

    const int kSubdivisionSize = 5000;
    const int kInitialVertexCount = 30;
    const int kInitialIndexCount = 99;
    const int kGrowthFactor = 2;
}  // namespace

namespace tango_augmented_reality {

    ViewerApp::ViewerApp() :  landscape(false),
                              textured(false),
                              scale(1),
                              zoom(0)
    {
    }

    ViewerApp::~ViewerApp() {
        if (tango_config_ != nullptr) {
            TangoConfig_free(tango_config_);
            tango_config_ = nullptr;
        }
    }


    void ViewerApp::OnCreate(JNIEnv* env, jobject activity,
                                       int display_rotation) {

        int version;
        TangoErrorType err = TangoSupport_GetTangoVersion(env, activity, &version);
        if (err != TANGO_SUCCESS || version < kTangoCoreMinimumVersion) {
            LOGE("ViewerApp::OnCreate, Tango Core version is out of date.");
            std::exit(EXIT_SUCCESS);
        }

        // We want to be able to trigger rendering on demand in our Java code.
        // As such, we need to store the activity we'd like to interact with and the
        // id of the method we'd like to call on that activity.
        calling_activity_obj_ = env->NewGlobalRef(activity);
        jclass cls = env->GetObjectClass(activity);
        on_demand_render_ = env->GetMethodID(cls, "requestRender", "()V");

        is_service_connected_ = false;
        is_gl_initialized_ = false;

        display_rotation_ = display_rotation;
        is_video_overlay_rotation_set_ = false;
    }


    void ViewerApp::OnTangoServiceConnected(JNIEnv* env, jobject iBinder, double res,
                                                      double dmin, double dmax, int noise, bool land,
                                                      bool textures) {
        landscape = land;
        textured = textures;
        if (res < 0.00999)
            scale = 10;
        else
            scale = 1;

        TangoErrorType ret = TangoService_setBinder(env, iBinder);
        if (ret != TANGO_SUCCESS) {
            LOGE(
                    "AugmentedRealityApp: Failed to set Tango binder with"
                            "error code: %d",
                    ret);
            std::exit(EXIT_SUCCESS);
        }
        TangoSetupConfig();
        TangoConnect();

        is_service_connected_ = true;

        binder_mutex_.lock();
        //t3dr_context_ = TangoSetup3DR(res, dmin, dmax, noise);
        binder_mutex_.unlock();
    }

    void ViewerApp::OnDestroy() {
        JNIEnv* env;
        java_vm_->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
        env->DeleteGlobalRef(calling_activity_obj_);

        calling_activity_obj_ = nullptr;
        on_demand_render_ = nullptr;
    }

    void ViewerApp::TangoSetupConfig() {
        // Here, we'll configure the service to run in the way we'd want. For this
        // application, we'll start from the default configuration
        // (TANGO_CONFIG_DEFAULT). This enables basic motion tracking capabilities.
        tango_config_ = TangoService_getConfig(TANGO_CONFIG_DEFAULT);
        if (tango_config_ == nullptr) {
            LOGE("AugmentedRealityApp: Failed to get default config form");
            std::exit(EXIT_SUCCESS);
        }

        // Get TangoCore version string from service.
        char tango_core_version[kVersionStringLength];
        int ret = TangoConfig_getString(tango_config_, "tango_service_library_version",
                                    tango_core_version, kVersionStringLength);
        if (ret != TANGO_SUCCESS) {
            LOGE(
                    "ViewerApp: get tango core version failed with error"
                            "code: %d",
                    ret);
            std::exit(EXIT_SUCCESS);
        }
        tango_core_version_string_ = tango_core_version;
    }


// Connect to Tango Service, service will start running, and
// pose can be queried.
    void ViewerApp::TangoConnect() {
        TangoErrorType err = TangoService_connect(this, tango_config_);
        if (err != TANGO_SUCCESS) {
            LOGE(
                    "ViewerApp: Failed to connect to the Tango service with"
                            "error code: %d",
                    err);
            std::exit(EXIT_SUCCESS);
        }

        // Initialize TangoSupport context.
        TangoSupport_initializeLibrary();

        // Update the camera intrinsics too.
        /*TangoCameraIntrinsics intrinsics;
        err = TangoService_getCameraIntrinsics(TANGO_CAMERA_COLOR, &intrinsics);
        if (err != TANGO_SUCCESS)
            std::exit(EXIT_SUCCESS);
        t3dr_intrinsics_.calibration_type =
                static_cast<Tango3DR_TangoCalibrationType>(intrinsics.calibration_type);
        t3dr_intrinsics_.width = intrinsics.width;
        t3dr_intrinsics_.height = intrinsics.height;
        t3dr_intrinsics_.fx = intrinsics.fx;
        t3dr_intrinsics_.fy = intrinsics.fy;
        t3dr_intrinsics_.cx = intrinsics.cx;
        t3dr_intrinsics_.cy = intrinsics.cy;
        std::copy(std::begin(intrinsics.distortion), std::end(intrinsics.distortion),
                  std::begin(t3dr_intrinsics_.distortion));

        // Update the depth intrinsics too.
        err = TangoService_getCameraIntrinsics(TANGO_CAMERA_DEPTH, &intrinsics);
        if (err != TANGO_SUCCESS)
            std::exit(EXIT_SUCCESS);
        t3dr_intrinsics_depth.calibration_type =
                static_cast<Tango3DR_TangoCalibrationType>(intrinsics.calibration_type);
        t3dr_intrinsics_depth.width = intrinsics.width;
        t3dr_intrinsics_depth.height = intrinsics.height;
        t3dr_intrinsics_depth.fx = intrinsics.fx;
        t3dr_intrinsics_depth.fy = intrinsics.fy;
        t3dr_intrinsics_depth.cx = intrinsics.cx;
        t3dr_intrinsics_depth.cy = intrinsics.cy;
        std::copy(std::begin(intrinsics.distortion), std::end(intrinsics.distortion),
                  std::begin(t3dr_intrinsics_depth.distortion));*/
    }


    void ViewerApp::OnPause() {
        TangoDisconnect();
        DeleteResources();
    }

    void ViewerApp::TangoDisconnect() {
        is_service_connected_ = false;
        is_gl_initialized_ = false;
        is_video_overlay_rotation_set_ = false;
        TangoConfig_free(tango_config_);
        tango_config_ = nullptr;
        TangoService_disconnect();
    }

    void ViewerApp::OnSurfaceCreated() {
        render_mutex_.lock();
        main_scene_.InitGLContent();
        is_gl_initialized_ = true;
        render_mutex_.unlock();

    }

    void ViewerApp::OnSurfaceChanged(int width, int height) {
        viewport_width_ = width;
        viewport_height_ = height;

        render_mutex_.lock();
        main_scene_.SetupViewPort(width,height);
        render_mutex_.unlock();
    }

    void ViewerApp::OnDeviceRotationChanged(int display_rotation) {
        display_rotation_ = display_rotation;
        is_video_overlay_rotation_set_ = false;
    }

    void ViewerApp::OnDrawFrame() {
        // If tracking is lost, further down in this method AugmentedRealityScene::Render
        // will not be called. Prevent flickering that would otherwise
        // happen by rendering solid white as a fallback.
        render_mutex_.lock();
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        if (!is_gl_initialized_ || !is_service_connected_) {
            return;
        }
        main_scene_.camera_->SetPosition(glm::vec3(movex, movey, movez));
        main_scene_.camera_->SetRotation(glm::quat(glm::vec3(yaw, pitch, roll)));
        main_scene_.camera_->SetScale(glm::vec3(1, 1, 1));

        glm::vec4 move = main_scene_.camera_->GetTransformationMatrix() * glm::vec4(0, 0, zoom, 0);
        main_scene_.camera_->Translate(glm::vec3(move.x, move.y, move.z));
        main_scene_.Render(false);
        render_mutex_.unlock();
    }

    void ViewerApp::DeleteResources() {
        render_mutex_.lock();
        main_scene_.DeleteResources();
        is_gl_initialized_ = false;
        render_mutex_.unlock();
    }

    void ViewerApp::Load(std::string filename) {
        binder_mutex_.lock();
        render_mutex_.lock();
        ModelIO io(filename, false);
        int size = main_scene_.static_meshes_.size();
        const std::vector<std::string> &model = io.readModel(kSubdivisionSize, main_scene_.static_meshes_);
        size = main_scene_.static_meshes_.size() - size;
        for(int i=0;i<size;i++) {
            main_scene_.addTransform(new tango_gl::Transform());
        }
        textureProcessor->Add(model);
        render_mutex_.unlock();
        binder_mutex_.unlock();
    }

    void ViewerApp::AddBall(float x, float y) {
        // Init earth mesh and material
        tango_gl::StaticMesh* ball_mesh_ = tango_gl::meshes::MakeSphereMesh(15, 15, 0.2f);
        //ball_mesh_->render_mode = GL_TRIANGLES;
        tango_gl::Transform* ball_transform = new tango_gl::Transform();
        //ball_transform->SetPosition(glm::vec3(x,y,0.0));
        //ball_transform->SetRotation(glm::quat(1.0f,1.0f,0,1.0f));
        ball_transform->SetPosition(CenterOfStaticModel());
        lastBall += .1f;

        tango_gl::Material* ball_material = new tango_gl::Material();
        tango_gl::Texture* ball_texture_ = new tango_gl::Texture(main_scene_.aAssetManager, "earth.png");

        ball_material->SetShader(
                tango_gl::shaders::GetTexturedVertexShader().c_str(),
                tango_gl::shaders::GetTexturedFragmentShader().c_str());
        ball_material->SetParam("texture", ball_texture_);

        main_scene_.ball_meshes_.push_back(ball_mesh_);
        main_scene_.ball_mesh_transforms_.push_back(ball_transform);
        main_scene_.ball_mesh_materials_.push_back(ball_material);
        main_scene_.ball_mesh_textures_.push_back(ball_texture_);
        //std::vector<std::string> pngFiles;
        //pngFiles.push_back("earth.png");
        //textureProcessor->Add(pngFiles);
    }

    std::string ViewerApp::GetTransformString() {
        std::lock_guard<std::mutex> lock(transform_mutex_);
        return transform_string_;
    }

    std::string ViewerApp::GetVersionString() {
        return tango_core_version_string_.c_str();
    }

    void ViewerApp::RequestRender() {
        if (calling_activity_obj_ == nullptr || on_demand_render_ == nullptr) {
            LOGE("Can not reference Activity to request render");
            return;
        }

        // Here, we notify the Java activity that we'd like it to trigger a render.
        JNIEnv* env;
        java_vm_->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
        env->CallVoidMethod(calling_activity_obj_, on_demand_render_);
    }

    glm::vec3 ViewerApp::CenterOfStaticModel(){
        float minX = 99999999;
        float maxX = -99999999;
        float minY = 99999999;
        float maxY = -99999999;
        float minZ = 99999999;
        float maxZ = -99999999;
        for (tango_gl::StaticMesh mesh : main_scene_.static_meshes_) {
            for (glm::vec3 vec : mesh.vertices) {
                if (minX > vec.x)
                    minX = vec.x;
                if (maxX < vec.x)
                    maxX = vec.x;
                if (minY > vec.y)
                    minY = vec.y;
                if (maxY < vec.y)
                    maxY = vec.y;
                if (minZ > vec.z)
                    minZ = vec.z;
                if (maxZ < vec.z)
                    maxZ = vec.z;
            }
        }
        float midX = (minX + maxX) * 0.5f;
        float midY = (minY + maxY) * 0.5f;
        float midZ = (minZ + maxZ) * 0.5f;
        LOGE("X: %f - %f Y: %f - %f Z: %f - %f", minX, maxX, minY, maxY, minZ, maxZ);
        return glm::vec3(midX,midY,midZ);
    }
}  // namespace tango_augmented_reality
