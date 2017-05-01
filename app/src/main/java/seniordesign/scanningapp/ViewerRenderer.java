package seniordesign.scanningapp;

import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Elaine on 4/15/2017.
 */

public class ViewerRenderer implements GLSurfaceView.Renderer {
    private AssetManager mAssetManager;

    public ViewerRenderer(AssetManager assetManager){
        mAssetManager = assetManager;
    }

    // Render loop of the Gl context.
    public void onDrawFrame(GL10 gl) {
        JNINative.onGlSurfaceDrawFrameViewer();
    }

    // Called when the surface size changes.
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        JNINative.onGlSurfaceChangedViewer(width, height);
    }

    // Called when the surface is created or recreated.
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        JNINative.onGlSurfaceCreatedViewer(mAssetManager);
    }
}
