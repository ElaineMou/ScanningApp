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

#include <math.h>

#include <tango-gl/conversions.h>
#include <tango-gl/tango-gl.h>
#include <tango-gl/texture.h>
#include <tango-gl/shaders.h>
#include <tango-gl/meshes.h>

#include "tango-augmented-reality/augmented_reality_scene.h"

namespace {
// We want to represent the device properly with respect to the ground so we'll
// add an offset in z to our origin. We'll set this offset to 1.3 meters based
// on the average height of a human standing with a Tango device. This allows us
// to place a grid roughly on the ground for most users.
const glm::vec3 kHeightOffset = glm::vec3(0.0f, 0.0f, 0.0f);

// Frustum scale.
const glm::vec3 kFrustumScale = glm::vec3(0.4f, 0.3f, 0.5f);

}  // namespace

namespace tango_augmented_reality {

AugmentedRealityScene::AugmentedRealityScene() {}

AugmentedRealityScene::~AugmentedRealityScene() {}

void AugmentedRealityScene::InitGLContent() {
  glEnable(GL_CULL_FACE);
  glCullFace(GL_BACK);

  // Allocating render camera and drawable object.
  // All of these objects are for visualization purposes.
  video_overlay_ = new tango_gl::VideoOverlay();
  camera_ = new tango_gl::Camera();

  color_vertex_shader = new tango_gl::Material();
  color_vertex_shader->SetShader(tango_gl::shaders::GetColorVertexShader().c_str(),
                                 tango_gl::shaders::GetBasicFragmentShader().c_str());
  textured_shader = new tango_gl::Material();
  textured_shader->SetShader(tango_gl::shaders::GetTexturedVertexShader().c_str(),
                             tango_gl::shaders::GetTexturedFragmentShader().c_str());

  object_transform.SetPosition(glm::vec3(0.0f, 0.0f, -5.0f));
  is_content_initialized_ = true;
}

void AugmentedRealityScene::DeleteResources() {
  if (is_content_initialized_) {
    delete camera_;
    camera_ = nullptr;
    delete video_overlay_;
    video_overlay_ = nullptr;

    delete color_vertex_shader;
    color_vertex_shader = nullptr;
    delete textured_shader;
    textured_shader = nullptr;

    static_meshes_.clear();

    is_content_initialized_ = false;
  }
}

void AugmentedRealityScene::SetupViewport(int w, int h) {
  if (h <= 0 || w <= 0) {
    LOGE("Setup graphic height not valid");
    return;
  }

  viewport_width_ = w;
  viewport_height_ = h;

  camera_->SetAspectRatio(static_cast<float>(w) / static_cast<float>(h));
  glViewport(0, 0, w, h);
}

void AugmentedRealityScene::SetProjectionMatrix(const glm::mat4& projection_matrix) {
  camera_->SetProjectionMatrix(projection_matrix);
}

void AugmentedRealityScene::Clear() {
  glViewport(0, 0, viewport_width_, viewport_height_);
  glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
  glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
}

void AugmentedRealityScene::Render(bool frustum, const glm::mat4& cur_pose_transformation) {
  glViewport(0, 0, viewport_width_, viewport_height_);

  glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
  glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

  // In first person mode, we directly control camera's motion.
  camera_->SetTransformationMatrix(cur_pose_transformation);

  // If it's first person view, we will render the video overlay in full
  // screen, so we passed identity matrix as view and projection matrix.
  glDisable(GL_DEPTH_TEST);
  video_overlay_->Render(glm::mat4(1.0f), glm::mat4(1.0f));
  glEnable(GL_DEPTH_TEST);

  unsigned int lastTexture = INT_MAX;
  for (tango_gl::StaticMesh mesh : static_meshes_) {
    if (mesh.texture == -1)
      tango_gl::Render(mesh, *color_vertex_shader, object_transform, *camera_, -1);
    else {
      unsigned int texture = textureMap[mesh.texture];
      if (lastTexture != texture) {
        lastTexture = texture;
        glBindTexture(GL_TEXTURE_2D, texture);
      }
      tango_gl::Render(mesh, *textured_shader, object_transform, *camera_, -1);
    }
  }
  if (!frustum_.vertices.empty() && frustum) {
    tango_gl::Render(frustum_, *color_vertex_shader, object_transform, *camera_,
                     (const int) frustum_.indices.size());
  }
}

/*void AugmentedRealityScene::RotateYAxisForTimestamp(double timestamp,
                                    tango_gl::Transform* transform,
                                    double* last_angle,
                                    double* last_timestamp) {
  if (*last_timestamp > 0) {
    // Calculate time difference in seconds
    double delta_time = timestamp - *last_timestamp;
    // Calculate the corresponding angle movement considering
    // a total rotation time of 6 seconds
    double delta_angle = delta_time * 2 * M_PI / 6;
    // Add this angle to the last known angle
    double angle = *last_angle + delta_angle;
    *last_angle = angle;

    double w = cos(angle / 2);
    double y = sin(angle / 2);

    transform->SetRotation(glm::quat(w, 0.0f, y, 0.0f));
  }
  *last_timestamp = timestamp;
}*/

void AugmentedRealityScene::SetVideoOverlayRotation(int display_rotation) {
  if (is_content_initialized_) {
    video_overlay_->SetDisplayRotation(
        static_cast<TangoSupportRotation>(display_rotation));
  }
}

void AugmentedRealityScene::UpdateFrustum(glm::vec3 pos, float zoom) {
  if(frustum_.colors.empty()) {
    frustum_.render_mode = GL_TRIANGLES;
    frustum_.colors.push_back(0xFFFFFF00);
    frustum_.colors.push_back(0xFFFFFF00);
    frustum_.colors.push_back(0xFFFFFF00);
    frustum_.colors.push_back(0xFFFFFF00);
    frustum_.colors.push_back(0xFFFFFF00);
    frustum_.colors.push_back(0xFFFFFF00);
    frustum_.indices.push_back(0);
    frustum_.indices.push_back(1);
    frustum_.indices.push_back(2);
    frustum_.indices.push_back(0);
    frustum_.indices.push_back(1);
    frustum_.indices.push_back(3);
    frustum_.indices.push_back(0);
    frustum_.indices.push_back(1);
    frustum_.indices.push_back(4);
    frustum_.indices.push_back(0);
    frustum_.indices.push_back(1);
    frustum_.indices.push_back(5);
    frustum_.indices.push_back(2);
    frustum_.indices.push_back(3);
    frustum_.indices.push_back(4);
    frustum_.indices.push_back(2);
    frustum_.indices.push_back(3);
    frustum_.indices.push_back(5);
  }
  if(!frustum_.vertices.empty())
    frustum_.vertices.clear();
  float f = zoom * 0.005f;
  frustum_.vertices.push_back(pos + glm::vec3(f, 0, 0));
  frustum_.vertices.push_back(pos + glm::vec3(-f, 0, 0));
  frustum_.vertices.push_back(pos + glm::vec3(0, 0, f));
  frustum_.vertices.push_back(pos + glm::vec3(0, 0, -f));
  frustum_.vertices.push_back(pos + glm::vec3(0, f, 0));
  frustum_.vertices.push_back(pos + glm::vec3(0, -f, 0));
}

}  // namespace tango_augmented_reality
