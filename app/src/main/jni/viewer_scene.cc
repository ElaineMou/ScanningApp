/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

#include <tango-gl/conversions.h>
#include <tango-gl/shaders.h>
#include <tango-gl/tango-gl.h>

#include "tango-augmented-reality/viewer_scene.h"

namespace tango_augmented_reality {

    ViewerScene::ViewerScene() { }

    ViewerScene::~ViewerScene() { }

    void ViewerScene::InitGLContent() {
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        camera_ = new tango_gl::Camera();
        color_vertex_shader = new tango_gl::Material();
        color_vertex_shader->SetShader(tango_gl::shaders::GetColorVertexShader().c_str(),
                                       tango_gl::shaders::GetBasicFragmentShader().c_str());
        textured_shader = new tango_gl::Material();
        textured_shader->SetShader(tango_gl::shaders::GetTexturedVertexShader().c_str(),
                                   tango_gl::shaders::GetTexturedFragmentShader().c_str());
        marker_material = new tango_gl::Material();
        marker_texture = new tango_gl::Texture(aAssetManager, "block.png");
        marker_material->SetShader(
                tango_gl::shaders::GetTexturedVertexShader().c_str(),
                tango_gl::shaders::GetTexturedFragmentShader().c_str());
        marker_material->SetParam("texture", marker_texture);


        chosen_marker_material = new tango_gl::Material();
        chosen_marker_texture = new tango_gl::Texture(aAssetManager, "chosenblock.png");
        chosen_marker_material->SetShader(
                tango_gl::shaders::GetTexturedVertexShader().c_str(),
                tango_gl::shaders::GetTexturedFragmentShader().c_str());
        chosen_marker_material->SetParam("texture", chosen_marker_texture);
    }

    void ViewerScene::DeleteResources() {
        delete camera_;
        camera_ = nullptr;

        delete color_vertex_shader;
        color_vertex_shader = nullptr;
        delete textured_shader;
        textured_shader = nullptr;

        static_meshes_.clear();
        for(std::vector<tango_gl::Transform*>::iterator it = static_mesh_transforms_.begin();it!=static_mesh_transforms_.end();it++) {
            delete *it;
        }
        static_mesh_transforms_.clear();

        for(std::vector<tango_gl::StaticMesh*>::iterator it = marker_meshes_.begin();it!=marker_meshes_.end();it++) {
            delete *it;
        }
        marker_meshes_.clear();

        for(std::vector<tango_gl::Transform*>::iterator it = marker_mesh_transforms_.begin();it!=marker_mesh_transforms_.end();it++) {
            delete *it;
        }
        marker_mesh_transforms_.clear();

        for(std::vector<tango_gl::Material*>::iterator it = marker_mesh_materials_.begin();it!=marker_mesh_materials_.end();it++) {
            delete *it;
        }
        marker_mesh_materials_.clear();

        for(std::vector<tango_gl::Texture*>::iterator it = marker_mesh_textures_.begin();it!=marker_mesh_textures_.end();it++) {
            delete *it;
        }
        marker_mesh_textures_.clear();
        dynamic_meshes_.clear();
    }

    void ViewerScene::SetupViewPort(int w, int h) {
        if (h == 0) {
            LOGE("Setup graphic height not valid");
        } else {
            LOGE("WIDTH = %d HEIGHT = %d", w, h);
            camera_->SetAspectRatio(static_cast<float>(w) / static_cast<float>(h));
            glViewport(0, 0, w, h);
        }
    }

    void ViewerScene::Render(bool frustum) {
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        unsigned int lastTexture = INT_MAX;
        for (int i=0;i<static_meshes_.size();i++) {
            tango_gl::StaticMesh mesh = static_meshes_.at(i);
            if (mesh.texture == -1)
                tango_gl::Render(mesh, *color_vertex_shader, *(static_mesh_transforms_.at(i)), *camera_, -1);
            else {
                unsigned int texture = textureMap[mesh.texture];
                if (lastTexture != texture) {
                    lastTexture = texture;
                    glBindTexture(GL_TEXTURE_2D, texture);
                }
                tango_gl::Render(mesh, *textured_shader, *(static_mesh_transforms_.at(i)), *camera_, -1);
            }
        }
        if(showMarkers) {
            for (int i = 0; i < marker_meshes_.size(); i++) {
                tango_gl::StaticMesh *mesh = marker_meshes_.at(i);
                tango_gl::Transform *transform = marker_mesh_transforms_.at(i);
                if(i != chosenMarkerIndex) {
                    tango_gl::Render(*mesh, *marker_material, *transform, *camera_,
                                     mesh->indices.size());
                } else {
                    tango_gl::Render(*mesh, *chosen_marker_material, *transform, *camera_,
                                     mesh->indices.size());
                }
            }
        }
        for (SingleDynamicMesh *mesh : dynamic_meshes_) {
            mesh->mutex.lock();
            if (mesh->mesh.texture == -1)
                tango_gl::Render(mesh->mesh, *color_vertex_shader, tango_gl::Transform(), *camera_, mesh->size);
            else {
                unsigned int texture = textureMap[mesh->mesh.texture];
                if (lastTexture != texture) {
                    lastTexture = texture;
                    glBindTexture(GL_TEXTURE_2D, texture);
                }
                tango_gl::Render(mesh->mesh, *textured_shader, tango_gl::Transform(), *camera_, mesh->size);
            }
            mesh->mutex.unlock();
        }
        for (tango_gl::StaticMesh mesh : debug_meshes_) {
            tango_gl::Render(mesh, *color_vertex_shader, tango_gl::Transform(), *camera_, -1);
        }
        if(!frustum_.vertices.empty() && frustum)
            tango_gl::Render(frustum_, *color_vertex_shader, tango_gl::Transform(), *camera_,
                             (const int) frustum_.indices.size());
    }

    void ViewerScene::addTransform(tango_gl::Transform* transform) {
        static_mesh_transforms_.push_back(transform);
    }

    void ViewerScene::UpdateFrustum(glm::vec3 pos, float zoom) {
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

    void ViewerScene::AddDynamicMesh(SingleDynamicMesh *mesh) {
        dynamic_meshes_.push_back(mesh);
    }

    void ViewerScene::ClearDynamicMeshes() {
        for (unsigned int i = 0; i < dynamic_meshes_.size(); i++) {
            delete dynamic_meshes_[i];
        }
        dynamic_meshes_.clear();
        textureMap.clear();
    }

}  // namespace mesh_builder