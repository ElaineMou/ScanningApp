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

#ifndef TANGO_AUGMENTED_REALITY_SCENE_H_
#define TANGO_AUGMENTED_REALITY_SCENE_H_

#include <jni.h>
#include <memory>
#include <math.h>

#include <android/asset_manager.h>

#include <tango_client_api.h>  // NOLINT
#include <tango-gl/camera.h>
#include <tango-gl/color.h>
#include <tango-gl/transform.h>
#include <tango-gl/util.h>
#include <tango-gl/video_overlay.h>
#include <tango-gl/texture.h>
#include <tango-gl/meshes.h>
#include <tango-gl/shaders.h>
#include <tango-gl/tango-gl.h>

#include <tango-gl/gesture_camera.h>
#include <tango-gl/grid.h>
#include <tango_3d_reconstruction_api.h>
#include "model_io.h"

namespace tango_augmented_reality {

// AugmentedRealityScene provides OpenGL drawable objects and renders them for visualization.
class AugmentedRealityScene {
 public:
  // Constructor and destructor.
  AugmentedRealityScene();
  ~AugmentedRealityScene();

  // Allocate OpenGL resources for rendering.
  void InitGLContent();

  // Release non-OpenGL resources.
  void DeleteResources();

  // Setup GL view port.
  // @param: w, width of the screen.
  // @param: h, height of the screen.
  void SetupViewport(int w, int h);

  // Clear the screen to a solid color.
  void Clear();

  // Render loop.
  void Render(bool frustum, const glm::mat4& cur_pose_transformation);

  // Get video overlay texture id.
  // @return: texture id of video overlay's texture.
  GLuint GetVideoOverlayTextureId() { return video_overlay_->GetTextureId(); }

  // @return: AR render camera's image plane ratio.
  float GetCameraImagePlaneRatio() { return camera_image_plane_ratio_; }

  // Set AR render camera's image plane ratio.
  // @param: image plane ratio.
  void SetCameraImagePlaneRatio(float ratio) {
    camera_image_plane_ratio_ = ratio;
  }

  // @return: AR render camera's image plane distance from the view point.
  float GetImagePlaneDistance() { return image_plane_distance_; }

  // Set AR render camera's image plane distance from the view point.
  // @param: distance, AR render camera's image plane distance from the view
  //         point.
  void SetImagePlaneDistance(float distance) {
    image_plane_distance_ = distance;
  }

  // Set projection matrix of the AR view (first person view)
  // @param: projection_matrix, the projection matrix.
  void SetProjectionMatrix(const glm::mat4& projection_matrix);

  // Set video overlay's orientation based on current device orientation.
  void SetVideoOverlayRotation(int display_rotation);

  void UpdateFrustum(glm::vec3 pos, float zoom);

  AAssetManager* aAssetManager;
  // Camera object that allows user to use touch input to interact with.
  tango_gl::Camera* camera_;
  tango_gl::StaticMesh frustum_;
  std::vector<unsigned int> textureMap;
  std::vector<tango_gl::StaticMesh> static_meshes_;
  std::vector<tango_gl::StaticMesh> debug_meshes_;
  std::vector<SingleDynamicMesh*> dynamic_meshes_;
  tango_gl::Material* color_vertex_shader;
  tango_gl::Material* textured_shader;
  tango_gl::Transform object_transform;

  std::vector<tango_gl::StaticMesh*> marker_meshes_;
  std::vector<tango_gl::Transform*> marker_mesh_transforms_;
  std::vector<tango_gl::Transform*> combined_marker_transforms_;
  tango_gl::Material* marker_material;
  tango_gl::Texture* marker_texture;
  tango_gl::Material* chosen_marker_material;
  tango_gl::Texture* chosen_marker_texture;
  tango_gl::StaticMesh* marker_bounding_box;
  bool showMarkers = true;
  int chosenMarkerIndex = -1;

private:
  // Video overlay drawable object to display the camera image.
  tango_gl::VideoOverlay* video_overlay_;

  // We use both camera_image_plane_ratio_ and image_plane_distance_ to compute
  // the first person AR camera's frustum, these value is derived from actual
  // physical camera instrinsics.
  // Aspect ratio of the color camera.
  float camera_image_plane_ratio_;

  // Image plane distance from camera's origin view point.
  float image_plane_distance_;

  // The projection matrix for the first person AR camera.
  glm::mat4 ar_camera_projection_matrix_;

  // Check if resources is allocated.
  bool is_content_initialized_ = false;

  int viewport_width_;
  int viewport_height_;
};
}  // namespace tango_augmented_reality

#endif  // TANGO_AUGMENTED_REALITY_SCENE_H_
