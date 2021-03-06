#ifndef MESH_BUILDER_SCENE_H_
#define MESH_BUILDER_SCENE_H_

#include <memory>
#include <vector>

#include <tango_client_api.h>  // NOLINT
#include <tango-gl/gesture_camera.h>
#include <tango-gl/grid.h>
#include <tango-gl/tango-gl.h>
#include <tango-gl/util.h>
#include <tango_3d_reconstruction_api.h>
#include "model_io.h"

namespace tango_augmented_reality {

    class ViewerScene {
    public:
        ViewerScene();
        ~ViewerScene();
        void InitGLContent();
        void DeleteResources();
        void SetupViewPort(int w, int h);
        void Render(bool frustum);
        void addTransform(tango_gl::Transform * transform);
        void UpdateFrustum(glm::vec3 pos, float zoom);
        void AddDynamicMesh(SingleDynamicMesh* mesh);
        void ClearDynamicMeshes();

        AAssetManager* aAssetManager;
        tango_gl::Camera* camera_;
        tango_gl::StaticMesh frustum_;
        std::vector<unsigned int> textureMap;
        std::vector<tango_gl::StaticMesh> static_meshes_;
        std::vector<tango_gl::Transform*> static_mesh_transforms_;
        std::vector<tango_gl::StaticMesh*> marker_meshes_;
        std::vector<tango_gl::Transform*> marker_mesh_transforms_;
        std::vector<tango_gl::Material*> marker_mesh_materials_;
        std::vector<tango_gl::Texture*> marker_mesh_textures_;
        std::vector<tango_gl::StaticMesh> debug_meshes_;
        std::vector<SingleDynamicMesh*> dynamic_meshes_;
        tango_gl::Material* color_vertex_shader;
        tango_gl::Material* textured_shader;
        tango_gl::Material* marker_material;
        tango_gl::Texture* marker_texture;
        tango_gl::Material* chosen_marker_material;
        tango_gl::Texture* chosen_marker_texture;
        tango_gl::StaticMesh* marker_bounding_box;
        bool showMarkers;
        int chosenMarkerIndex = -1;
    };
}  // namespace mesh_builder

#endif  // MESH_BUILDER_SCENE_H_